package bezbednost.service.implementation;

import bezbednost.dto.response.OCSPResponse;
import bezbednost.entity.Admin;
import bezbednost.entity.OCSP;
import bezbednost.repository.IOCSPRepository;
import bezbednost.service.IAdminService;
import bezbednost.service.IOCSPService;
import bezbednost.service.ISignatureService;
import bezbednost.util.enums.RevocationStatus;
import org.springframework.stereotype.Service;


import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OCSPService implements IOCSPService {


    private final IOCSPRepository _OCSPRepository;

    private final ISignatureService _signatureService;

    private final IAdminService _adminService;

    private final KeyStoresReaderService _keyStoresReaderService;

    public OCSPService(IOCSPRepository OCSPRepository, SignatureService signatureService, IAdminService adminService, KeyStoresReaderService keyStoresReaderService) {
        _OCSPRepository = OCSPRepository;
        _signatureService = signatureService;
        _adminService = adminService;
        _keyStoresReaderService = keyStoresReaderService;
    }

    @Override
    public OCSPResponse getOCSPEntity(UUID id) {
        return mapOCSPtoOCSPResponse(_OCSPRepository.findOneById(id));
    }

    @Override
    public OCSP getOCSPEntityBySerialNum(BigInteger serial_num) {
        return _OCSPRepository.findOneBySerialNum(serial_num);
    }

    @Override
    public List<OCSPResponse> getAll() {
        List<OCSP> ocsps = _OCSPRepository.findAll();
        return ocsps.stream()
                .map(ocsp -> mapOCSPtoOCSPResponse(ocsp))
                .collect(Collectors.toList());
    }

    @Override
    public List<OCSP> getAllByRevoker(UUID id) {
        return _OCSPRepository.findAllByRevoker(id);
    }

    /**
     * @param certificate the certificate to be checked
     * @param issuerCert the issuer certificate
     * @return the Enum.RevocationStatus
     * @throws NullPointerException if params are null values
     * */
    @Override
    public RevocationStatus check(X509Certificate certificate, X509Certificate issuerCert) throws NullPointerException {
        OCSP revokedCert = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        String issuerName = issuerCert.getSubjectDN().getName();
        X509Certificate checkIssuer = getCACertificateByName(issuerName);

        if (revokedCert != null){
            return RevocationStatus.REVOKED;
        }
        else if (!certificate.getIssuerDN().getName().equals(issuerName) || checkIssuer == null){
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
    public RevocationStatus revoke(X509Certificate certificate, UUID id) throws NullPointerException {
        if(!checkAdmin(id)){
            return RevocationStatus.UNKNOWN;
        }

        OCSP ocspEntity = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if(ocspEntity == null){
            OCSP ocsp = new OCSP();
            ocsp.setRevoker(id);
            ocsp.setSerialNum(certificate.getSerialNumber());
            System.out.println(certificate.getIssuerDN().getName());
            String issuerEmail = getEmailFromName(certificate.getIssuerDN().getName());
            String subjectEmail = getEmailFromName(certificate.getSubjectDN().getName());
            ocsp.setIssuer(issuerEmail);
            ocsp.setSubject(subjectEmail);
            _OCSPRepository.save(ocsp);
        }

        return RevocationStatus.REVOKED;
    }

    private boolean checkAdmin(UUID id) {
        Admin admin = _adminService.findOneById(id);
        return admin != null;
    }

    /**
     * @param certificate the certificate to be revoked
     * @param id of user who revoke certificate (admin)
     * @return the Enum.RevocationStatus
     * @throws NullPointerException the certificate has a null value
     */
    @Override
    public RevocationStatus activate(X509Certificate certificate, UUID id) throws NullPointerException {
        OCSP ocsp = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if (ocsp != null  && checkAdmin(id) && ocsp.getRevoker().equals(id)){
            _OCSPRepository.deleteById(ocsp.getId());
            return RevocationStatus.GOOD;
        }
        else {
            return RevocationStatus.UNKNOWN;
        }
    }

    /**
     * @param certificate the certificate to be validate
     * @return true - valid certificate, false - invalid certificate
     * @throws RuntimeException end of recursion
     */
    @Override
    public boolean checkCertificateValidity(X509Certificate certificate) throws  RuntimeException {
        X509Certificate parentCertificate = getCACertificateByName(certificate.getIssuerDN().getName());
        RevocationStatus certStatus;
        try {
            certStatus = check(certificate, parentCertificate);
        }catch (NullPointerException e){
            System.out.println("Sertifikati imaju NULL vrednost.");
            return false;
        }

        if (checkDate(certificate, getCurrentDate())) {
            if (checkDigitalSignature(certificate, parentCertificate)) {
                if (certStatus.equals(RevocationStatus.GOOD)) {
                    if(certificate.equals(parentCertificate)){
                        throw new RuntimeException();
                    }
                    else{
                        // ako nije root, proveravaj sad njega
                        try {
                            checkCertificateValidity(parentCertificate);
                        }catch (RuntimeException e){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private OCSPResponse mapOCSPtoOCSPResponse(OCSP ocsp){
        OCSPResponse response = new OCSPResponse();
        response.setId(ocsp.getId());
        response.setIssuer(ocsp.getIssuer());
        response.setSubject(ocsp.getSubject());
        response.setRevoker(ocsp.getRevoker());
        response.setSerialNum(ocsp.getSerialNum());
        return response;
    }

    private boolean checkDate(X509Certificate certificate, String date){
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        if(certificate == null){
            return false;
        }
        try {
            Date currentDate = formatter.parse(date);
            certificate.checkValidity(currentDate);
            System.out.println("\n" + "Valid Date");
            return true;
        }catch(CertificateExpiredException e) {
            System.out.println("\n" + "Invalid Date (CertificateExpiredException)");
        }catch(CertificateNotYetValidException e) {
            System.out.println("\n" + "Invalid Date (CertificateNotYetValidException)");
        }catch (ParseException e) {
            System.out.println("\n" + "Date parse exception (ParseException)");
        }

        return false;
    }

    private boolean checkDigitalSignature(X509Certificate certificate, X509Certificate parentCertificate) {
        PublicKey publicKey = parentCertificate.getPublicKey();

        try {
            certificate.verify(publicKey);
            return true;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param name Subject Name from CA certificate
     * @return CA certificate that contains Subject Name from param
     * */
    public X509Certificate getCACertificateByName(String name) {
        String certEmail = getEmailFromName(name);

        List<X509Certificate> CACertificates = _keyStoresReaderService.readAllCertificate("keystoreIntermediate.jks", "admin");
        for (X509Certificate certificate : CACertificates) {
            String subjectName = certificate.getSubjectDN().getName();
            String CAEmail = getEmailFromName(subjectName);

            if(CAEmail.equals( certEmail )) {
                return certificate;
            }
        }

        List<X509Certificate> RootCertificates = _keyStoresReaderService.readAllCertificate("keystoreRoot.jks", "admin");
        for (X509Certificate certificate : RootCertificates) {
            String subjectName = certificate.getSubjectDN().getName();
            String RootEmail = getEmailFromName(subjectName);

            if(RootEmail.equals( certEmail )){
                return certificate;
            }
        }

        return null;
    }

    /**
     * @param name Subject Name from certificate
     * @return End-User certificate that contains Subject Name from param
     * */
    public X509Certificate getEndCertificateByName(String name) {
        String certEmail = getEmailFromName(name);
        List<X509Certificate> Certificates = _keyStoresReaderService.readAllCertificate("keystoreEndUser.jks", "admin");
        for (X509Certificate certificate : Certificates) {
            String subjectName = certificate.getSubjectDN().getName();
            String CAEmail = getEmailFromName(subjectName);

            if(CAEmail.equals( certEmail )) {
                return certificate;
            }
        }

        return null;
    }

    /**
     * @param name Subject Name from certificate
     * @return E-Mail address from Subject Name
     * */
    private String getEmailFromName(String name) {
        String[] list = name.split(",");
        for (String str : list) {
            String strTrim = str.trim();
            if(strTrim.contains("EMAILADDRESS=")){
                int indexEq = strTrim.indexOf("=");
                return strTrim.substring(indexEq+1);
            }
        }

        return null;
    }

    /**
     * @return current date and time
     */
    public String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
