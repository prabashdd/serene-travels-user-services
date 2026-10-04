package lk.serenetravels.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.serenetravels.userservice.dto.CompanyLookupResponse;
import lk.serenetravels.userservice.dto.CompanyResponse;
import lk.serenetravels.userservice.dto.CreateCompanyRequest;
import lk.serenetravels.userservice.dto.UpdateCompanyRequest;
import lk.serenetravels.userservice.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "Companies", description = "Partner companies that AGENT users work for (Company A itself, where "
        + "ADMIN/MANAGER work, is not modeled here)")
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "Create a partner company (admin)", description = "Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompany(request));
    }

    @Operation(summary = "List companies for self-registration (public)",
            description = "Returns every company's id and name only — nothing sensitive — for use as a "
                    + "selection dropdown during agent self-registration, so an agent whose employer is "
                    + "already in the system (verified or not) can select it instead of submitting a "
                    + "duplicate company record. No authentication required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Companies returned")
    })
    @GetMapping("/lookup")
    public ResponseEntity<List<CompanyLookupResponse>> lookupCompanies() {
        return ResponseEntity.ok(companyService.listForLookup());
    }

    @Operation(summary = "Get a company by id (admin)", description = "Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company returned"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No company with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(@Parameter(description = "Company id") @PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @Operation(summary = "List partner companies (admin)",
            description = "Paginated listing, optionally filtered by verified status. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of companies returned"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> list(
            @Parameter(description = "Filter by verified status") @RequestParam(required = false) Boolean verified,
            Pageable pageable) {
        return ResponseEntity.ok(companyService.list(verified, pageable));
    }

    @Operation(summary = "Update a partner company (admin)", description = "Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No company with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@Parameter(description = "Company id") @PathVariable UUID id,
                                                           @Valid @RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    @Operation(summary = "Verify a partner company (admin)",
            description = "Confirms a company submitted via agent self-registration is legitimate. An agent "
                    + "at this company still can't log in until they are also individually verified (see "
                    + "PATCH /api/v1/users/{id}/verify) — both checks must pass. Requires the ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company verified"),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "No company with that id", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/verify")
    public ResponseEntity<CompanyResponse> verifyCompany(@Parameter(description = "Company id") @PathVariable UUID id) {
        return ResponseEntity.ok(companyService.verifyCompany(id));
    }
}
