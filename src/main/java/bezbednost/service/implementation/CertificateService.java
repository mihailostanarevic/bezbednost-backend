package bezbednost.service.implementation;

import bezbednost.service.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
@Service
public class CertificateService implements ICertificateService {

    @Autowired
    KeyStoresReaderService _keyStoresReaderService;

    @Autowired
    OCSPService _ocspListService;

    @Override
    public List<X509Certificate> getAllActiveEndUserCertificates() {
        List<X509Certificate> retList = new ArrayList<>();
        List<X509Certificate> certificates = _keyStoresReaderService.readAllCertificate("keystoreEndUser.jks", "admin");
        for (X509Certificate certificate : certificates) {
            if (_ocspListService.checkCertificateValidity(certificate)) {
                retList.add(certificate);
            }
        }

        return retList;
    }

    @Override
    public List<X509Certificate> getAllActiveCACertificates() {
        List<X509Certificate> retList = new ArrayList<>();
        List<X509Certificate> CACertificates = _keyStoresReaderService.readAllCertificate("keystoreIntermediate.jks", "admin");
        for (X509Certificate certificate : CACertificates) {
            if (_ocspListService.checkCertificateValidity(certificate)) {
                retList.add(certificate);
            }
        }

        return retList;
    }

    @Override
    public List<X509Certificate> getAllActiveRootCertificates() {
        List<X509Certificate> retList = new ArrayList<>();
        List<X509Certificate> RootCertificates = _keyStoresReaderService.readAllCertificate("keystoreRoot.jks", "admin");
        for (X509Certificate certificate : RootCertificates) {
            if (_ocspListService.checkCertificateValidity(certificate)) {
                retList.add(certificate);
            }
        }

        return retList;
    }
}
