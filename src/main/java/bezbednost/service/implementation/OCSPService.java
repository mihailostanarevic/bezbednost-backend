package bezbednost.service.implementation;

import bezbednost.entity.OCSPEntity;
import bezbednost.repository.IOCSPRepository;
import bezbednost.service.IOCSPService;
import bezbednost.util.enums.RevocationStatus;
import org.springframework.stereotype.Service;


import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OCSPService implements IOCSPService {

    private String currentDate = java.time.LocalDate.now().toString();

    private final IOCSPRepository _ocspListRepository;

    private final SignatureService _signatureService;

    public OCSPService(IOCSPRepository ocspListRepository, SignatureService signatureService) {
        _ocspListRepository = ocspListRepository;
        _signatureService = signatureService;
    }

    @Override
    public OCSPEntity getOCSPEntity(UUID id) {
        return _ocspListRepository.findOneById(id);
    }

    @Override
    public OCSPEntity getOCSPEntityBySerialNum(BigInteger serial_num) {
        return _ocspListRepository.findOneBySerialNum(serial_num);
    }

    @Override
    public List<OCSPEntity> getAll() {
        return _ocspListRepository.findAll();
    }

    @Override
    public List<OCSPEntity> getAllByRevoker(UUID id) {
        return _ocspListRepository.findAllByRevoker(id);
    }

    /**
     * @param certificate the certificate to be checked
     * @param issuerCert the issuer certificate
     * @return the Enum.RevocationStatus
     * @throws NullPointerException if params are null values
     * */
    @Override
    public RevocationStatus check(X509Certificate certificate, X509Certificate issuerCert) throws NullPointerException {
        OCSPEntity revokedCert = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        String issuerName = issuerCert.getSubjectDN().getName();
        if (revokedCert != null){
            return RevocationStatus.REVOKED;
        }
        // TODO (A) proveriti i jos da li issuerCert stvarno postoji kao CA u sistemu
        else if (!certificate.getIssuerDN().getName().equals(issuerName)){
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
     * @throws NullPointerException the certificate has a null value
     */
    @Override
    // TODO (A) proveriti da li je id stvarno administratorov
    public RevocationStatus revoke(X509Certificate certificate, UUID id) throws NullPointerException {
        if(false){
            // provera da li NIJE admin
            return RevocationStatus.UNKNOWN;
        }

        if(getOCSPEntityBySerialNum(certificate.getSerialNumber()) == null){
            OCSPEntity ocsp = new OCSPEntity();
            ocsp.setRevoker(id);
            ocsp.setSerialNum(certificate.getSerialNumber());
            _ocspListRepository.save(ocsp);
        }

        return RevocationStatus.REVOKED;
    }

    /**
     * @param certificate the certificate to be revoked
     * @param id of user who revoke certificate (admin)
     * @return the Enum.RevocationStatus
     * @throws NullPointerException the certificate has a null value
     */
    @Override
    public RevocationStatus activate(X509Certificate certificate, UUID id) throws NullPointerException {
        OCSPEntity ocsp = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if (ocsp != null && ocsp.getRevoker().equals(id)){
            _ocspListRepository.deleteById(ocsp.getId());
            return RevocationStatus.GOOD;
        }
        else {
            return RevocationStatus.UNKNOWN;
        }
    }

    /**
     * @param certificate the certificate to be revoked
     * @return true - valid certificate, false - invalid certificate
     * @throws NullPointerException the certificate has a null value
     */
    @Override
    public Boolean checkCertificateValidity(X509Certificate certificate) throws NullPointerException {
        X509Certificate parentCertificate = getCertificateByName(certificate.getIssuerDN().getName());
        RevocationStatus certStatus;
        try {
            certStatus = check(certificate, parentCertificate);
        }catch (NullPointerException e){
            System.out.println("Sertifikati imaju NULL vrednost.");
            return false;
        }

        if (checkDate(certificate, this.currentDate)) {
            if (checkDigitalSignature(certificate, parentCertificate)) {
                if (certStatus.equals(RevocationStatus.GOOD)) {
                    if(certificate.equals(parentCertificate)){
                        // sertifikat je validan
                        return true;
                    }
                    else{
                        // ako nije root, proveravaj sad njega
                       checkCertificateValidity(parentCertificate);
                    }
                }
            }
        }

        return false;
    }

    private boolean checkDate(X509Certificate certificate, String date){
        SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");

        if(certificate == null){
            return false;
        }
        try {
            Date currentDate = iso8601Formater.parse(date);
            certificate.checkValidity(currentDate);
            System.out.println("\n" + "VALIDAN JE");
            return true;
        }catch(CertificateExpiredException e) {
            System.out.println("\n" + "NIJE VALIDAN (CertificateExpiredException)");
        }catch(CertificateNotYetValidException e) {
            System.out.println("\n" + "NIJE VALIDAN (CertificateNotYetValidException)");
        }catch (ParseException e) {
            System.out.println("\n" + "DATUM NIJE DOBRO PARSIRAN (ParseException)");
        }

        return false;
    }

    private boolean checkDigitalSignature(X509Certificate certificate, X509Certificate parentCertificate) {
        byte[] signature = certificate.getSignature();
        PublicKey publicKey = parentCertificate.getPublicKey();

        return _signatureService.verify(certificate, signature, publicKey);
    }

    // TODO (A) preuzeti sertifikat na osnovu imena
    //  1) splitovati po ","
    //  2) uzeti deo sa E=...
    //  3) uzeti samo mail ( substring(2) )
    //  4) uzeti taj sertifikat iz keystore (alias == mail)
    private X509Certificate getCertificateByName(String name) {
        System.out.println("Name of certificate" + name);
        return null;
    }

}
