package bezbednost.controller;

import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.request.IssuerEndDateRequest;
import bezbednost.dto.response.CertificateRequestResponse;
import bezbednost.dto.response.IssuerEndDateResponse;
import bezbednost.service.implementation.CertificateRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/certificate-requests")
public class CertificateRequestController {

    private final CertificateRequestService _certificateRequestService;

    public CertificateRequestController(CertificateRequestService certificateRequestService) {
        _certificateRequestService = certificateRequestService;
    }

    @PostMapping
    public CertificateRequestResponse createCertificateRequest(@RequestBody CertificateRequestRequest request) throws Exception {
        return _certificateRequestService.createCertificateRequest(request);
    }

    @GetMapping
    public List<CertificateRequestResponse> getAllCertificateRequests() throws Exception{
        return _certificateRequestService.getAllCertificateRequests();
    }

    @GetMapping("/{id}/certificate-request")
    public CertificateRequestResponse getCertificateRequest(@PathVariable UUID id) throws Exception{
        return _certificateRequestService.getCertificateRequest(id);
    }

    @PostMapping("/approve")
    public void approveCertificateRequest(@RequestBody CertificateRequestRequest request) throws Exception{
        _certificateRequestService.approveCertificateRequest(request);
    }

    @DeleteMapping("/deny/{id}/request")
    public void denyCertificateRequest(@PathVariable UUID id) throws Exception{
        _certificateRequestService.denyCertificateRequest(id);
    }

    @PostMapping("/issuer")
    public IssuerEndDateResponse getIssuerEndDate(@RequestBody IssuerEndDateRequest request){
        return _certificateRequestService.getIssuerCertificateEndDate(request);
    }
}
