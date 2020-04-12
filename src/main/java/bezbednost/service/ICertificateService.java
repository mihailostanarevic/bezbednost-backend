package bezbednost.service;

import bezbednost.dto.request.DownloadRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

public interface ICertificateService {

    List<X509Certificate> getAllActiveEndUserCertificates();

    List<X509Certificate> getAllActiveCACertificates();

    List<X509Certificate> getAllActiveRootCertificates();

    void saveCertificate(X509Certificate certificate, String extension) throws IOException;

    ResponseEntity<Object> downloadCertificate(DownloadRequest request);
}
