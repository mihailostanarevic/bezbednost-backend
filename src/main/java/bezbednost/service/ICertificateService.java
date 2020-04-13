package bezbednost.service;

import bezbednost.dto.request.DownloadRequest;
import bezbednost.dto.response.CertificateResponseDTO;
import bezbednost.util.enums.CertificateType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

public interface ICertificateService {

    List<X509Certificate> getAllActiveEndUserCertificates();

    List<X509Certificate> getAllActiveIntermediateCertificates();

    List<X509Certificate> getAllActiveRootCertificates();

    List<CertificateResponseDTO> listToDTO(CertificateType certificateType, List<X509Certificate> certificateList);

    void saveCertificate(X509Certificate certificate, String extension) throws IOException;

    ResponseEntity<Object> downloadCertificate(DownloadRequest request);
}
