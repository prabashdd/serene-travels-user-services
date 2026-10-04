package lk.serenetravels.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.serenetravels.userservice.dto.AssignCompanyRequest;
import lk.serenetravels.userservice.dto.AssignManagerRequest;
import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.RegisterUserRequest;
import lk.serenetravels.userservice.dto.UpdateUserProfileRequest;
import lk.serenetravels.userservice.dto.UpdateUserTypeRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.UserType;
import lk.serenetravels.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "User registration, profile, and lifecycle management")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Self-register as an Agent",
            description = "Public signup, no access token required. Always creates an AGENT. The agent's "
                    + "employer is either an existing company — pass companyId, picked from "
                    + "GET /api/v1/companies/lookup — or a brand-new one, submitted inline via company; "
                    + "provide exactly one of the two. The new user always starts unverified. A brand-new "
                    + "company also starts unverified; an existing one keeps whatever verified status it "
                    + "already had. The account stays locked out of login until the user is verified "
                    + "(PATCH /{id}/verify) and, if the company isn't already verified, that too "
                    + "(PATCH /api/v1/companies/{id}/verify).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created (and, for a new company, the company too)"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or neither/both of companyId "
                    + "and company were provided", content = @Content),
            @ApiResponse(responseCode = "404", description = "companyId was provided but no such company exists",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = RegisterUserRequest.class),
            examples = {
                    @ExampleObject(name = "Existing company (pick from the lookup list)", value = """
                            {
                              "username": "kasun.silva",
                              "email": "kasun.silva@example.com",
                              "firstName": "Kasun",
                              "lastName": "Silva",
                              "password": "Secret@123",
                              "companyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                            }"""),
                    @ExampleObject(name = "New company (not in the lookup list)", value = """
                            {
                              "username": "kasun.silva",
                              "email": "kasun.silva@example.com",
                              "firstName": "Kasun",
                              "lastName": "Silva",
                              "password": "Secret@123",
                              "company": {
                                "name": "Ceylon Holidays (Pvt) Ltd",
                                "address": "42 Galle Road, Colombo 03",
                                "registrationNumber": "PV 00123456",
                                "phone": "+94 11 234 5678",
                                "email": "info@ceylonholidays.lk"
                              }
                            }""")
            }))
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerAgent(request));
    }

    @Operation(summary = "Create a user (admin)",
            description = "Creates a user of any type with an immediately usable, permanent password, verified "
                    + "and enabled right away. An AGENT must be created with an existing companyId (see "
                    + "POST /api/v1/companies); every other user type must omit it. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or companyId required/not "
                    + "applicable for this user type", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No company with that companyId", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request, false, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Verify a self-registered user (admin)",
            description = "Confirms a self-registered account, setting verified=true and re-enabling it in "
                    + "Keycloak so it can log in. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User verified"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/verify")
    public ResponseEntity<UserResponse> verify(@Parameter(description = "User profile id") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.verifyUser(id));
    }

    @Operation(summary = "Get my own profile",
            description = "Returns the profile of the currently authenticated caller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getByKeycloakUserId(jwt.getSubject()));
    }

    @Operation(summary = "Update my own profile",
            description = "Lets the currently authenticated caller update their own name/email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content)
    })
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody UpdateUserProfileRequest request) {
        UserResponse self = userService.getByKeycloakUserId(jwt.getSubject());
        return ResponseEntity.ok(userService.updateProfile(self.getId(), request));
    }

    @Operation(summary = "Get a user by id (admin)", description = "Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@Parameter(description = "User profile id") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "List/search users (admin)",
            description = "Paginated listing, optionally filtered by user type, verified status, active status, "
                    + "and company (e.g. all AGENT users at a specific partner company). Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of users returned"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(
            @Parameter(description = "Filter by user type") @RequestParam(required = false) UserType userType,
            @Parameter(description = "Filter by verified status") @RequestParam(required = false) Boolean verified,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filter by partner company id") @RequestParam(required = false) UUID companyId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.list(userType, verified, active, companyId, pageable));
    }

    @Operation(summary = "Update a user's profile (admin)", description = "Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@Parameter(description = "User profile id") @PathVariable UUID id,
                                                     @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @Operation(summary = "Assign or clear an agent's manager (admin)",
            description = "Sets which MANAGER an AGENT reports to. Pass a null managerId to clear the "
                    + "assignment. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manager assignment updated"),
            @ApiResponse(responseCode = "400", description = "Target is not an AGENT, or managerId is not a "
                    + "MANAGER", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{agentId}/manager")
    public ResponseEntity<UserResponse> assignManager(
            @Parameter(description = "Agent's user profile id") @PathVariable UUID agentId,
            @RequestBody AssignManagerRequest request) {
        return ResponseEntity.ok(userService.assignManager(agentId, request.getManagerId()));
    }

    @Operation(summary = "Assign or clear an agent's company (admin)",
            description = "Sets which partner company an AGENT works for. Only AGENTs have a company; "
                    + "ADMIN/MANAGER are internal to the operating company. Pass a null companyId to clear "
                    + "the assignment. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company assignment updated"),
            @ApiResponse(responseCode = "400", description = "Target is not an AGENT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user or company with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{agentId}/company")
    public ResponseEntity<UserResponse> assignCompany(
            @Parameter(description = "Agent's user profile id") @PathVariable UUID agentId,
            @RequestBody AssignCompanyRequest request) {
        return ResponseEntity.ok(userService.assignCompany(agentId, request.getCompanyId()));
    }

    @Operation(summary = "List a manager's team (admin)",
            description = "Lists the AGENTs reporting to the given MANAGER. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team members returned"),
            @ApiResponse(responseCode = "400", description = "Target is not a MANAGER", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{managerId}/team")
    public ResponseEntity<List<UserResponse>> getTeam(
            @Parameter(description = "Manager's user profile id") @PathVariable UUID managerId) {
        return ResponseEntity.ok(userService.getTeam(managerId));
    }

    @Operation(summary = "Deactivate a user (admin)",
            description = "Disables the account for lifecycle reasons (e.g. the person has left), independent "
                    + "of the verified flag. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deactivated"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivate(@Parameter(description = "User profile id") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    @Operation(summary = "Reactivate a user (admin)", description = "Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User reactivated"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivate(@Parameter(description = "User profile id") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.reactivate(id));
    }

    @Operation(summary = "Change a user's type/role (admin)",
            description = "Re-syncs the user's Keycloak realm role to match. If the new type is not AGENT, "
                    + "any existing manager assignment is cleared. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User type changed"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No user with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/type")
    public ResponseEntity<UserResponse> changeUserType(@Parameter(description = "User profile id") @PathVariable UUID id,
                                                         @Valid @RequestBody UpdateUserTypeRequest request) {
        return ResponseEntity.ok(userService.changeUserType(id, request.getUserType()));
    }
}
