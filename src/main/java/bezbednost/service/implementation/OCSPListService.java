package bezbednost.service.implementation;

import bezbednost.entity.Admin;
import bezbednost.entity.OCSPEntity;
import bezbednost.repository.IOCSPListRepository;
import bezbednost.service.IOCSPListService;
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

@SuppressWarnings({"unused", "SpellCheckingInspection", "ConstantConditions", "TryWithIdenticalCatches"})
@Service
public class OCSPListService implements IOCSPListService {

    @Autowired
    IOCSPListRepository _ocspListRepository;

    @Autowired
    SignatureService _signatureService;

    @Autowired
    AdminService _adminService;

    @Autowired
    KeyStoresReaderService _keyStoresReaderService;

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

        OCSPEntity ocspEntity = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if(ocspEntity == null){
            OCSPEntity ocsp = new OCSPEntity();
            ocsp.setRevoker(id);
            ocsp.setSerialNum(certificate.getSerialNumber());
            _ocspListRepository.save(ocsp);
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
        OCSPEntity ocsp = getOCSPEntityBySerialNum(certificate.getSerialNumber());
        if (ocsp != null  && checkAdmin(id) && ocsp.getRevoker().equals(id)){
            _ocspListRepository.deleteById(ocsp.getId());
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
    private X509Certificate getEndCertificateByName(String name) {
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
