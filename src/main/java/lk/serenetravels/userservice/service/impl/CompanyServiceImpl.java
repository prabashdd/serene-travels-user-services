package lk.serenetravels.userservice.service.impl;

import lk.serenetravels.userservice.dto.CompanyLookupResponse;
import lk.serenetravels.userservice.dto.CompanyResponse;
import lk.serenetravels.userservice.dto.CreateCompanyRequest;
import lk.serenetravels.userservice.dto.UpdateCompanyRequest;
import lk.serenetravels.userservice.entity.Company;
import lk.serenetravels.userservice.exception.CompanyNotFoundException;
import lk.serenetravels.userservice.repository.CompanyRepository;
import lk.serenetravels.userservice.service.CompanyService;
import lk.serenetravels.userservice.service.UserService;
import lk.serenetravels.userservice.util.CompanySpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;

    public CompanyServiceImpl(CompanyRepository companyRepository, UserService userService) {
        this.companyRepository = companyRepository;
        this.userService = userService;
    }

    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        // An admin creating a company directly has already vetted it, same as
        // an admin-created user being considered verified immediately.
        Company company = new Company(request.getName(), request.getAddress(),
                request.getRegistrationNumber(), request.getPhone(), request.getEmail(), true);
        companyRepository.save(company);
        return toResponse(company);
    }

    @Override
    public CompanyResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public Page<CompanyResponse> list(Boolean verified, Pageable pageable) {
        return companyRepository.findAll(CompanySpecifications.filter(verified), pageable).map(this::toResponse);
    }

    @Override
    public List<CompanyLookupResponse> listForLookup() {
        // Includes unverified companies on purpose: a second agent from the same
        // not-yet-verified employer should still be able to pick it, instead of
        // registering a duplicate Company row.
        return companyRepository.findAll(Sort.by("name")).stream()
                .map(company -> new CompanyLookupResponse(company.getId(), company.getName()))
                .toList();
    }

    @Override
    public CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request) {
        Company company = findOrThrow(id);
        company.setName(request.getName());
        company.setAddress(request.getAddress());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        companyRepository.save(company);
        return toResponse(company);
    }

    @Override
    public CompanyResponse verifyCompany(UUID id) {
        Company company = findOrThrow(id);
        company.setVerified(true);
        companyRepository.save(company);
        // Agents already individually verified may have been waiting only on
        // their company; re-enable them in Keycloak now that it's confirmed too.
        userService.refreshKeycloakEnabledForCompany(id);
        return toResponse(company);
    }

    private Company findOrThrow(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("No company found with id '" + id + "'"));
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getAddress(),
                company.getRegistrationNumber(), company.getPhone(), company.getEmail(),
                company.isVerified(), company.getCreatedAt(), company.getUpdatedAt());
    }
}
