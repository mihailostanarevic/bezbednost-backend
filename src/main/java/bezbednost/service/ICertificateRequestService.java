package bezbednost.service;

import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.request.IssuerEndDateRequest;
import bezbednost.dto.response.CertificateRequestResponse;
import bezbednost.dto.response.IssuerEndDateResponse;

import java.util.List;
import java.util.UUID;

public interface ICertificateRequestService {

    CertificateRequestResponse createCertificateRequest(CertificateRequestRequest request) throws Exception;

    CertificateRequestResponse getCertificateRequest(UUID id) throws Exception;

    List<CertificateRequestResponse> getAllCertificateRequests() throws Exception;

    void approveCertificateRequest(CertificateRequestRequest request) throws Exception;

    void denyCertificateRequest(UUID certificateId) throws Exception;

    IssuerEndDateResponse getIssuerCertificateEndDate(IssuerEndDateRequest request);
}
