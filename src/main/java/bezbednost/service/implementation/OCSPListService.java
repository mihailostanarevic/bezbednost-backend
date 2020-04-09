package bezbednost.service.implementation;

import bezbednost.entity.OCSPEntity;
import bezbednost.repository.IOCSPListRepository;
import bezbednost.service.IOCSPListService;
import bezbednost.util.enums.RevocationStatus;
import org.springframework.beans.factory.annotation.Autowired;


import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ALL")
public class OCSPListService implements IOCSPListService {

    @Autowired
    IOCSPListRepository iocspListRepository;

    @Override
    public OCSPEntity findOneById(UUID id) {
        return iocspListRepository.findOneById(id);
    }

    @Override
    public OCSPEntity findOneBySerialNum(BigInteger serial_num) {
        return iocspListRepository.findOneBySerialNum(serial_num);
    }

    @Override
    public List<OCSPEntity> findAll() {
        return iocspListRepository.findAll();
    }

    @Override
    public List<OCSPEntity> findAllByRevoker(UUID id) {
        return findAllByRevoker(id);
    }

    /**
     * @param certificate the certificate to be checked
     * @param issuer the issuer certificate
     * @return the Enum.RevocationStatus
     * */
    @Override
    public RevocationStatus check(X509Certificate certificate, X509Certificate issuerCert) {
        OCSPEntity revokedCert = findOneBySerialNum(certificate.getSerialNumber());
        String issuerName = issuerCert.getSubjectDN().getName().toString();
        if (revokedCert != null){
            return RevocationStatus.REVOKED;
        }
        // TODO (A) proveriti i jos da li issuerCert stvarno postoji kao CA u sistemu
        else if (!certificate.getIssuerDN().getName().toString().equals(issuerName)){
            return RevocationStatus.UNKNOWN;
        }
        else {
            return RevocationStatus.GOOD;
        }
    }

    /**
     * @param certificate the certificate to be revoked
     * @param id of user who revoke certificate (admin)
     * @return the Enum.RevocationStatus
     */
    @Override
    // TODO (A) proveriti da li je id stvarno administratorov
    public RevocationStatus revoke(X509Certificate certificate, UUID id) {
        if(false){
            // provera da li NIJE admin
            return RevocationStatus.UNKNOWN;
        }

        if(findOneBySerialNum(certificate.getSerialNumber()) == null){
            OCSPEntity ocsp = new OCSPEntity();
            ocsp.setRevoker(id);
            ocsp.setSerialNum(certificate.getSerialNumber());
            iocspListRepository.save(ocsp);
        }

        return RevocationStatus.REVOKED;
    }

    /**
     * @param certificate the certificate to be revoked
     * @param id of user who revoke certificate (admin)
     * @return the Enum.RevocationStatus
     */
    @Override
    public RevocationStatus activate(X509Certificate certificate, UUID id) {
        OCSPEntity ocsp = findOneBySerialNum(certificate.getSerialNumber());
        if (ocsp != null && ocsp.getRevoker().equals(id)){
            iocspListRepository.deleteById(ocsp.getId());
            return RevocationStatus.GOOD;
        }
        else {
            return RevocationStatus.UNKNOWN;
        }
    }


}
