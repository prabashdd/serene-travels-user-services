package lk.serenetravels.userservice.service.impl;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.RegisterUserRequest;
import lk.serenetravels.userservice.dto.UpdateUserProfileRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.Company;
import lk.serenetravels.userservice.entity.UserProfile;
import lk.serenetravels.userservice.entity.UserType;
import lk.serenetravels.userservice.exception.CompanyNotFoundException;
import lk.serenetravels.userservice.exception.InvalidCompanyAssignmentException;
import lk.serenetravels.userservice.exception.InvalidManagerAssignmentException;
import lk.serenetravels.userservice.exception.UserNotFoundException;
import lk.serenetravels.userservice.repository.CompanyRepository;
import lk.serenetravels.userservice.repository.UserProfileRepository;
import lk.serenetravels.userservice.service.KeycloakUserService;
import lk.serenetravels.userservice.service.UserService;
import lk.serenetravels.userservice.util.UserProfileSpecifications;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final KeycloakUserService keycloakUserService;
    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;

    public UserServiceImpl(KeycloakUserService keycloakUserService, UserProfileRepository userProfileRepository,
                            CompanyRepository companyRepository) {
        this.keycloakUserService = keycloakUserService;
        this.userProfileRepository = userProfileRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request, boolean temporaryPassword, boolean verified) {
        validateCompanyAssignment(request.getUserType(), request.getCompanyId());

        String keycloakUserId = keycloakUserService.createUser(request, temporaryPassword, verified);

        try {
            keycloakUserService.assignRealmRole(keycloakUserId, request.getUserType().name());

            UserProfile profile = new UserProfile(keycloakUserId, request.getUserType(), verified);
            profile.setCompanyId(request.getCompanyId());
            userProfileRepository.save(profile);

            return new UserResponse(profile.getId(), keycloakUserId, request.getUsername(), request.getEmail(),
                    request.getFirstName(), request.getLastName(), request.getUserType(), profile.isVerified(),
                    profile.isActive(), profile.getManagerId(), profile.getCompanyId());
        } catch (RuntimeException ex) {
            // Keycloak and Postgres aren't in the same transaction, so a failure past this
            // point would otherwise leave an orphaned Keycloak user with no local profile.
            rollbackKeycloakUser(keycloakUserId);
            throw ex;
        }
    }

    @Override
    @Transactional
    public UserResponse registerAgent(RegisterUserRequest request) {
        UUID companyId = resolveRegistrationCompanyId(request);

        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername(request.getUsername());
        createRequest.setEmail(request.getEmail());
        createRequest.setFirstName(request.getFirstName());
        createRequest.setLastName(request.getLastName());
        createRequest.setPassword(request.getPassword());
        createRequest.setUserType(UserType.AGENT);
        createRequest.setCompanyId(companyId);

        return createUser(createRequest, false, false);
    }

    // Exactly one of companyId (an existing company, picked from the lookup list)
    // or company (a brand-new one, created fresh and unverified) must be given;
    // @Transactional on registerAgent rolls this new row back too if anything
    // past this point fails, rather than leaving it orphaned and unlinked.
    private UUID resolveRegistrationCompanyId(RegisterUserRequest request) {
        boolean hasCompanyId = request.getCompanyId() != null;
        boolean hasNewCompany = request.getCompany() != null;

        if (hasCompanyId == hasNewCompany) {
            throw new InvalidCompanyAssignmentException(
                    "Provide exactly one of companyId (an existing company) or company (a new one), not both or neither");
        }

        if (hasCompanyId) {
            if (!companyRepository.existsById(request.getCompanyId())) {
                throw new CompanyNotFoundException("No company found with id '" + request.getCompanyId() + "'");
            }
            return request.getCompanyId();
        }

        Company company = new Company(request.getCompany().getName(), request.getCompany().getAddress(),
                request.getCompany().getRegistrationNumber(), request.getCompany().getPhone(),
                request.getCompany().getEmail(), false);
        companyRepository.save(company);
        return company.getId();
    }

    @Override
    public UserResponse verifyUser(UUID id) {
        UserProfile profile = findProfileOrThrow(id);
        profile.setVerified(true);
        userProfileRepository.save(profile);
        syncKeycloakEnabled(profile);
        return toResponse(profile);
    }

    @Override
    public UserResponse getById(UUID id) {
        return toResponse(findProfileOrThrow(id));
    }

    @Override
    public UserResponse getByKeycloakUserId(String keycloakUserId) {
        UserProfile profile = userProfileRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException(
                        "No user profile found for the authenticated account"));
        return toResponse(profile);
    }

    @Override
    public Page<UserResponse> list(UserType userType, Boolean verified, Boolean active, UUID companyId,
            Pageable pageable) {
        return userProfileRepository
                .findAll(UserProfileSpecifications.filter(userType, verified, active, companyId), pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse updateProfile(UUID id, UpdateUserProfileRequest request) {
        UserProfile profile = findProfileOrThrow(id);
        keycloakUserService.updateProfile(profile.getKeycloakUserId(), request.getEmail(),
                request.getFirstName(), request.getLastName());
        return toResponse(profile);
    }

    @Override
    public UserResponse assignManager(UUID agentId, UUID managerId) {
        UserProfile agent = findProfileOrThrow(agentId);
        if (agent.getUserType() != UserType.AGENT) {
            throw new InvalidManagerAssignmentException("Only AGENT users can be assigned a manager");
        }

        if (managerId != null) {
            UserProfile manager = findProfileOrThrow(managerId);
            if (manager.getUserType() != UserType.MANAGER) {
                throw new InvalidManagerAssignmentException(
                        "User '" + managerId + "' is not a MANAGER and cannot be assigned as one");
            }
        }

        agent.setManagerId(managerId);
        userProfileRepository.save(agent);
        return toResponse(agent);
    }

    @Override
    public UserResponse assignCompany(UUID agentId, UUID companyId) {
        UserProfile agent = findProfileOrThrow(agentId);
        if (agent.getUserType() != UserType.AGENT) {
            throw new InvalidCompanyAssignmentException(
                    "Only AGENT users work for a partner company; ADMIN/MANAGER are internal to the operating company");
        }

        if (companyId != null && !companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("No company found with id '" + companyId + "'");
        }

        agent.setCompanyId(companyId);
        userProfileRepository.save(agent);
        return toResponse(agent);
    }

    @Override
    public List<UserResponse> getTeam(UUID managerId) {
        UserProfile manager = findProfileOrThrow(managerId);
        if (manager.getUserType() != UserType.MANAGER) {
            throw new InvalidManagerAssignmentException("User '" + managerId + "' is not a MANAGER");
        }
        return userProfileRepository.findByManagerId(managerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse deactivate(UUID id) {
        UserProfile profile = findProfileOrThrow(id);
        profile.setActive(false);
        userProfileRepository.save(profile);
        syncKeycloakEnabled(profile);
        return toResponse(profile);
    }

    @Override
    public UserResponse reactivate(UUID id) {
        UserProfile profile = findProfileOrThrow(id);
        profile.setActive(true);
        userProfileRepository.save(profile);
        syncKeycloakEnabled(profile);
        return toResponse(profile);
    }

    @Override
    public UserResponse changeUserType(UUID id, UserType newType) {
        UserProfile profile = findProfileOrThrow(id);
        UserType oldType = profile.getUserType();

        if (oldType != newType) {
            keycloakUserService.removeRealmRole(profile.getKeycloakUserId(), oldType.name());
            keycloakUserService.assignRealmRole(profile.getKeycloakUserId(), newType.name());
        }

        profile.setUserType(newType);
        if (newType != UserType.AGENT) {
            // A manager assignment and a partner company only make sense for an AGENT.
            profile.setManagerId(null);
            profile.setCompanyId(null);
        }
        userProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Override
    public void refreshKeycloakEnabledForCompany(UUID companyId) {
        userProfileRepository.findByCompanyId(companyId).stream()
                .filter(p -> p.isVerified() && p.isActive())
                .forEach(this::syncKeycloakEnabled);
    }

    private void validateCompanyAssignment(UserType userType, UUID companyId) {
        if (userType == UserType.AGENT) {
            if (companyId == null) {
                throw new InvalidCompanyAssignmentException("AGENT users must be created with a companyId");
            }
            if (!companyRepository.existsById(companyId)) {
                throw new CompanyNotFoundException("No company found with id '" + companyId + "'");
            }
        } else if (companyId != null) {
            throw new InvalidCompanyAssignmentException("companyId is only applicable to AGENT users");
        }
    }

    private UserProfile findProfileOrThrow(UUID id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No user profile found with id '" + id + "'"));
    }

    // Keycloak's enabled flag always mirrors verified && active && the agent's
    // company (if any) being verified: a self-registered user needs admin
    // verification, their submitted company needs separate admin verification,
    // and any user can be deactivated for lifecycle reasons independent of both.
    private void syncKeycloakEnabled(UserProfile profile) {
        boolean companyOk = profile.getCompanyId() == null
                || companyRepository.findById(profile.getCompanyId()).map(Company::isVerified).orElse(false);
        keycloakUserService.setEnabled(profile.getKeycloakUserId(),
                profile.isVerified() && profile.isActive() && companyOk);
    }

    private UserResponse toResponse(UserProfile profile) {
        UserRepresentation keycloakUser = keycloakUserService.getUser(profile.getKeycloakUserId());
        return new UserResponse(profile.getId(), profile.getKeycloakUserId(), keycloakUser.getUsername(),
                keycloakUser.getEmail(), keycloakUser.getFirstName(), keycloakUser.getLastName(),
                profile.getUserType(), profile.isVerified(), profile.isActive(), profile.getManagerId(),
                profile.getCompanyId());
    }

    private void rollbackKeycloakUser(String keycloakUserId) {
        try {
            keycloakUserService.deleteUser(keycloakUserId);
        } catch (RuntimeException rollbackEx) {
            log.error("Failed to roll back orphaned Keycloak user {} after a failed create-user request",
                    keycloakUserId, rollbackEx);
        }
    }
}
