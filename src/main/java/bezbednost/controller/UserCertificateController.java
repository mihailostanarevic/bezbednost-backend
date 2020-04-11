package bezbednost.controller;

import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.response.CertificateRequestResponse;
import bezbednost.service.implementation.CertificateRequestRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/certificate-requests")
public class UserCertificateController {

    private final CertificateRequestRequestService _CertificateRequestService;

    public UserCertificateController(CertificateRequestRequestService certificateRequestService) {
        _CertificateRequestService = certificateRequestService;
    }

    @PostMapping
    public CertificateRequestResponse createCertificateRequest(@RequestBody CertificateRequestRequest request) throws Exception {
        return _CertificateRequestService.createCertificateRequest(request);
    }

    @GetMapping
    public List<CertificateRequestResponse> getAllCertificateRequests() throws Exception{
        return _CertificateRequestService.getAllCertificateRequests();
    }

    @GetMapping("/{id}/certificate-request")
    public CertificateRequestResponse getCertificateRequest(@PathVariable UUID id) throws Exception{
        return _CertificateRequestService.getCertificateRequest(id);
    }
}
