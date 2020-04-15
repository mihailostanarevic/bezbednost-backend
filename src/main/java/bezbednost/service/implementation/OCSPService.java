package bezbednost.service.implementation;

import bezbednost.config.AlgorithmConfig;
import bezbednost.converter.CertificateConverter;
import bezbednost.dto.response.OCSPResponse;
import bezbednost.entity.Admin;
import bezbednost.entity.OCSPEntity;
import bezbednost.repository.IOCSPRepository;
import bezbednost.service.IAdminService;
import bezbednost.service.IOCSPService;
import bezbednost.service.ISignatureService;
import bezbednost.util.enums.RevocationStatus;
import org.springframework.beans.factory.annotation.Autowired;
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

@SuppressWarnings({"SpellCheckingInspection", "TryWithIdenticalCatches", "ConstantConditions", "unused", "Convert2MethodRef", "FieldCanBeLocal"})
@Service
public class OCSPService implements IOCSPService {

    @Autowired
    AlgorithmConfig config;

    @Autowired
    IOCSPRepository _OCSPRepository;

    @Autowired
    ISignatureService  _signatureService;

    @Autowired
    IAdminService _adminService;

    @Autowired
    KeyStoresReaderService _keyStoresReaderService;

    @Autowired
    CertificateService _certificateService;

    @Override
    public OCSPResponse getOCSPEntity(UUID id) {
        return mapOCSPtoOCSPResponse(_OCSPRepository.findOneById(id));
    }

    @Override
    public OCSPEntity getOCSPEntityBySerialNum(BigInteger serial_num) {
        return _OCSPRepository.findOneBySerialNum(serial_num);
    }

    @Override
    public List<OCSPResponse> getAll() throws Exception {
        List<OCSPEntity> ocspEntities = _OCSPRepository.findAll();

        if(ocspEntities.isEmpty()){
            throw new Exception("There are no revoked certificates;");
        }

        return ocspEntities.stream()
                .map(ocspEntity -> mapOCSPtoOCSPResponse(ocspEntity))
                .collect(Collectors.toList());
    }

    @Override
    public List<OCSPEntity> getAllByRevoker(UUID id) {
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
        OCSPEntity revokedCert = getOCSPEntityBySerialNum(certificate.getSerialNumber());
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

    @Override
    public RevocationStatus checkCertificateStatus(BigInteger serialNumber) {
        X509Certificate certificate = getCertificateBySerialNumber(serialNumber);
        if(certificate != null){
            String parentName = certificate.getIssuerDN().getName();
            return check(certificate, getCACertificateByName(parentName));
        }
        else{
            return RevocationStatus.UNKNOWN;
        }
    }

    private X509Certificate getCertificateBySerialNumber(BigInteger serialNumber) {
        List<X509Certificate> root_certificates = _certificateService.getAllActiveRootCertificates();
        List<X509Certificate> inter_certificates = _certificateService.getAllActiveIntermediateCertificates();
        List<X509Certificate> end_certificates = _certificateService.getAllActiveEndUserCertificates();

        if(root_certificates.isEmpty() && inter_certificates.isEmpty() && end_certificates.isEmpty()){
            return null;
        }

        for (X509Certificate certificate : root_certificates) {
            if(certificate.getSerialNumber().equals(serialNumber)){
                return certificate;
            }
        }
        for (X509Certificate certificate : inter_certificates) {
            if(certificate.getSerialNumber().equals(serialNumber)){
                return certificate;
            }
        }
        for (X509Certificate certificate : end_certificates) {
            if(certificate.getSerialNumber().equals(serialNumber)){
                return certificate;
            }
        }

        return null;
    }

    @Override
    public boolean revokeCertificate(String email) {
        List<X509Certificate> root_certificates = _certificateService.getAllActiveRootCertificates();
        List<X509Certificate> inter_certificates = _certificateService.getAllActiveIntermediateCertificates();
        List<X509Certificate> end_certificates = _certificateService.getAllActiveEndUserCertificates();

        if(root_certificates.isEmpty() && inter_certificates.isEmpty() && end_certificates.isEmpty()){
            return false;
        }

        UUID id = _adminService.findAll().get(0).getId();
        if(checkListCertificates(root_certificates, email, id)){
            return true;
        } else if(checkListCertificates(inter_certificates, email, id)) {
            return true;
        } else
            return checkListCertificates(end_certificates, email, id);

    }

    private boolean checkListCertificates (List<X509Certificate> certificateList, String email, UUID admin_id) {
        for (X509Certificate certificate : certificateList) {
            String cert_mail = getEmailFromName(certificate.getSubjectDN().getName());
            if (email.equals(cert_mail)) {
                revoke(certificate, admin_id);
                return true;
            }
        }

        return false;
    }

    /**
     * @param certificate the certificate to be revoked
     * @param id of user who revoke certificate (admin)
     * @return the Enum.RevocationStatus
     * @throws NullPointerException the certificate has a null value
     */
    public RevocationStatus revoke(X509Certificate certificate, UUID id) throws NullPointerException {
        if(!checkAdmin(id)){
            return RevocationStatus.UNKNOWN;
        }

        OCSPEntity ocspEntity = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if(ocspEntity == null){
            OCSPEntity ocsp = new OCSPEntity();
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
        OCSPEntity ocspEntity = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if (ocspEntity != null  && checkAdmin(id) && ocspEntity.getRevoker().equals(id)){
            _OCSPRepository.deleteById(ocspEntity.getId());
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
    public boolean checkCertificateValidity(X509Certificate certificate) {
        try {
            checkCertificate(certificate);
            return false;
        }catch (RuntimeException e){
            return true;
        }
    }

    private void checkCertificate(X509Certificate certificate) throws RuntimeException {
        X509Certificate parentCertificate = getCACertificateByName(certificate.getIssuerDN().getName());
        RevocationStatus certStatus;
        try {
            certStatus = check(certificate, parentCertificate);
        }catch (NullPointerException e){
            System.out.println("Sertifikati imaju NULL vrednost.");
            return;
        }

        if (checkDate(certificate, getCurrentDate())) {
            if (checkDigitalSignature(certificate, parentCertificate)) {
                if (certStatus.equals(RevocationStatus.GOOD)) {
                    if(certificate.equals(parentCertificate)) {
                        throw new RuntimeException();
                    }
                    else{
                        checkCertificate(parentCertificate);
                    }
                }
            }
        }
    }

    private OCSPResponse mapOCSPtoOCSPResponse(OCSPEntity ocspEntity){
        OCSPResponse response = new OCSPResponse();
        response.setId(ocspEntity.getId());
        response.setIssuer(ocspEntity.getIssuer());
        response.setSubject(ocspEntity.getSubject());
        response.setRevoker(ocspEntity.getRevoker());
        response.setSerialNum(ocspEntity.getSerialNum());
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
    private X509Certificate getCACertificateByName(String name) {
        String certEmail = getEmailFromName(name);

        List<X509Certificate> CACertificates = _keyStoresReaderService.readAllCertificate(config.getCAFileName(), config.getKsPassword());
        for (X509Certificate certificate : CACertificates) {
            String subjectName = certificate.getSubjectDN().getName();
            String CAEmail = getEmailFromName(subjectName);

            if(CAEmail.equals( certEmail )) {
                return certificate;
            }
        }

        List<X509Certificate> RootCertificates = _keyStoresReaderService.readAllCertificate(config.getRootFileName(), config.getKsPassword());
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
    private X509Certificate getEndCertificateByName(String name) {
        String certEmail = getEmailFromName(name);
        List<X509Certificate> Certificates = _keyStoresReaderService.readAllCertificate(config.getEnd_userFileName(), config.getKsPassword());
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
