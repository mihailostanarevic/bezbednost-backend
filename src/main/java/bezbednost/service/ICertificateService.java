package bezbednost.service;

import java.security.cert.X509Certificate;
import java.util.List;

public interface ICertificateService {

    List<X509Certificate> getAllActiveEndUserCertificates();

    List<X509Certificate> getAllActiveCACertificates();

    List<X509Certificate> getAllActiveRootCertificates();

}
