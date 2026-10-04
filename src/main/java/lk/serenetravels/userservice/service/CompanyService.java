package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.CompanyLookupResponse;
import lk.serenetravels.userservice.dto.CompanyResponse;
import lk.serenetravels.userservice.dto.CreateCompanyRequest;
import lk.serenetravels.userservice.dto.UpdateCompanyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getById(UUID id);

    Page<CompanyResponse> list(Boolean verified, Pageable pageable);

    // Public, minimal-field listing for the self-registration company dropdown.
    List<CompanyLookupResponse> listForLookup();

    CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request);

    CompanyResponse verifyCompany(UUID id);
}
