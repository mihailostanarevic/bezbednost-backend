package bezbednost.controller;

import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.request.EmailRequestDTO;
import bezbednost.dto.request.IssuerEndDateRequest;
import bezbednost.dto.request.UUIDRequestDTO;
import bezbednost.dto.response.CertificateRequestResponse;
import bezbednost.dto.response.IssuerEndDateResponse;
import bezbednost.dto.response.PossibleExtensionsResponse;
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

    @PostMapping("/deny")
    public void denyCertificateRequest(@RequestBody UUIDRequestDTO request) throws Exception{
        _certificateRequestService.denyCertificateRequest(request.getUuid());
    }

    @PostMapping("/issuer")
    public IssuerEndDateResponse getIssuerEndDate(@RequestBody IssuerEndDateRequest request){
        return _certificateRequestService.getIssuerCertificateEndDate(request);
    }

    @PostMapping("/possible-extensions")
    public PossibleExtensionsResponse getPossibleExtensions(@RequestBody EmailRequestDTO request){
        return _certificateRequestService.getPossibleExtensions(request);
    }
}
