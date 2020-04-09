package bezbednost.service;

import bezbednost.entity.OCSPEntity;
import bezbednost.util.enums.RevocationStatus;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

public interface IOCSPListService {

    OCSPEntity findOneById(UUID id);

    OCSPEntity findOneBySerialNum(BigInteger serial_num);

    List<OCSPEntity> findAll();

    List<OCSPEntity> findAllByRevoker(UUID id);

    RevocationStatus check(X509Certificate certificate, X509Certificate issuerCert);

    RevocationStatus revoke(X509Certificate certificate, UUID id);

    RevocationStatus activate(X509Certificate certificate, UUID id);

    Boolean checkCertificateValidity(X509Certificate certificate);

}
