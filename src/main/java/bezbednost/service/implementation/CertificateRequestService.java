package bezbednost.service.implementation;

import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.response.CertificateRequestResponse;
import bezbednost.entity.CertificateRequest;
import bezbednost.repository.ICertificateRequestRepository;
import bezbednost.service.ICertificateRequestService;
import bezbednost.service.IKeyStoresReaderService;
import bezbednost.service.IKeyStoresWriterService;
import bezbednost.service.ISignatureService;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateRequestService implements ICertificateRequestService {

    private final ICertificateRequestRepository _certificateRequestRepository;

    private final IKeyStoresReaderService _keyStoresReaderService;

    private final ISignatureService _signatureService;

    private final IKeyStoresWriterService _keyStoreWriterService;

    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository, IKeyStoresReaderService keyStoresReaderService, ISignatureService signatureService, IKeyStoresWriterService keyStoresWriterService) {
        _certificateRequestRepository = certificateRequestRepository;
        _keyStoresReaderService = keyStoresReaderService;
        _signatureService = signatureService;
        _keyStoreWriterService = keyStoresWriterService;
    }

    @Override
    public CertificateRequestResponse createCertificateRequest(CertificateRequestRequest request) throws Exception {
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setCountry(request.getCountry());
        certificateRequest.setFirstName(request.getFirstName());
        certificateRequest.setLastName(request.getLastName());
        certificateRequest.setOrganisation(request.getOrganisation());
        certificateRequest.setOrganisationUnit(request.getOrganisationUnit());
        certificateRequest.setEmail(request.getEmail());
        certificateRequest.setExtension(request.getExtension());
        certificateRequest.setCertificateAuthority(request.isCertificateAuthority());
        _certificateRequestRepository.save(certificateRequest);

        return mapCertificateRequestToCertificateRequestResponse(certificateRequest);
    }

    @Override
    public CertificateRequestResponse getCertificateRequest(UUID id) throws Exception {
        CertificateRequest certificateRequest = _certificateRequestRepository.findOneById(id);
        return mapCertificateRequestToCertificateRequestResponse(certificateRequest);
    }

    @Override
    public List<CertificateRequestResponse> getAllCertificateRequests() throws Exception {
        List<CertificateRequest> certificateRequests = _certificateRequestRepository.findAll();

        if(certificateRequests.isEmpty()){
            throw new Exception("You do not have new certificate requests");
        }

        return certificateRequests.stream()
                .map(certificateRequest -> mapCertificateRequestToCertificateRequestResponse(certificateRequest))
                .collect(Collectors.toList());
    }

    @Override
    public void approveCertificateRequest(CertificateRequestRequest request) throws Exception {
        CertificateRequest certificateRequest = _certificateRequestRepository.findOneByEmail(request.getEmail());
        _certificateRequestRepository.delete(certificateRequest);
        //generisanje sertifikata, i cuvanje u keystore
    }

    @Override
    public void denyCertificateRequest(UUID certificateId) throws Exception {
        _certificateRequestRepository.deleteById(certificateId);
    }


    private CertificateRequestResponse mapCertificateRequestToCertificateRequestResponse(CertificateRequest request){
        CertificateRequestResponse response = new CertificateRequestResponse();
        response.setId(request.getId());
        response.setCountry(request.getCountry());
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setOrganisation(request.getOrganisation());
        response.setOrganisationUnit(request.getOrganisationUnit());
        response.setExtension(request.getExtension());
        if(request.isCertificateAuthority()){
            response.setCaOrEnd("Certificate authority");
            return response;
        }

        response.setCaOrEnd("End user");
        return response;
    }

    /**
     * @param email of the issuer
     * @return the end date of issuers certificate
     * */
    private Date getIssuerEndDate(String email){
        X509Certificate issuerCert = this._keyStoresReaderService.readCertificate("keystoreRoot.jks", "admin", email);
        if(issuerCert == null){
            issuerCert = this._keyStoresReaderService.readCertificate("keystoreIntermediate.jks", "admin", email);
        }

        return issuerCert.getNotAfter();
    }

    /**
     * @param data the data for the certificate to be passed from request
     * @return the X509Certificate of the subject
     * */
    private X509Certificate generateCertificate(CertificateRequestRequest data,  UUID id) throws CertificateException {
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
        builder = builder.setProvider("BC");

        //Citam ko mu je izdavac sert
        X509Certificate issuerCert = this._keyStoresReaderService.readCertificate("keystoreRoot.jks", "admin", data.getIssuerEmail());
        if(issuerCert == null){
            issuerCert = this._keyStoresReaderService.readCertificate("keystoreIntermediate.jks", "admin", data.getIssuerEmail());
        }
        /*
         if(issuerCert.getNotAfter() < data.getRequestDate()){
            return null;
         }
*/
        KeyPair keyPair = this._signatureService.generateKeys();

        //Citam privatni kljuc izdavacu
        PrivateKey privKey = this._keyStoresReaderService.readPrivateKey("keystoreRoot.jks", "admin", data.getIssuerEmail(), "admin");
        if(privKey == null){
            privKey = this._keyStoresReaderService.readPrivateKey("keystoreIntermediate.jks", "admin", data.getIssuerEmail(), "admin");
        }

        //Za potpisivanje hesiranog sert
        ContentSigner contentSigner = null;
        try {
            contentSigner = builder.build(privKey);
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }

        //Pravljenje identiteta na sertifikatu
        X500Name x500Name = this.getX500Name(data, id);

        //build-ovanje sertifikata
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(new JcaX509CertificateHolder(issuerCert).getSubject(),
                new BigInteger("123"),
                new Date(),
                data.getEndDate(),
                x500Name,
                keyPair.getPublic());

        //Dodavanje ekstenzije da je CA ukoliko je to uneto u formi
        if(data.isCertificateAuthority()){
            try {
                certGen.addExtension(X509Extensions.BasicConstraints, true,
                        new BasicConstraints(true));
            } catch (CertIOException e) {
                e.printStackTrace();
            }
        }
        X509CertificateHolder certHolder = certGen.build(contentSigner);

        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        certConverter = certConverter.setProvider("BC");

        if(data.isCertificateAuthority()){
            _keyStoreWriterService.write(data.getEmail(), keyPair.getPrivate(), "keystoreIntermediate.jks", "admin", certConverter.getCertificate(certHolder));
        }else {
            _keyStoreWriterService.write(data.getEmail(), keyPair.getPrivate(), "keystoreEndUser.jks", "admin", certConverter.getCertificate(certHolder));
        }

        return certConverter.getCertificate(certHolder);
    }

    private X500Name getX500Name(CertificateRequestRequest data, UUID id){
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        String cn = data.getFirstName() + ' ' + data.getLastName();
        builder.addRDN(BCStyle.CN, cn);
        builder.addRDN(BCStyle.SURNAME, data.getLastName());
        builder.addRDN(BCStyle.GIVENNAME, data.getFirstName());
        builder.addRDN(BCStyle.O, data.getOrganisation());
        builder.addRDN(BCStyle.OU, data.getOrganisationUnit());
        builder.addRDN(BCStyle.C, data.getCountry());
        builder.addRDN(BCStyle.E, data.getEmail());
        //UID (USER ID) je ID korisnika
        builder.addRDN(BCStyle.UID, String.valueOf(id));
        return builder.build();
    }
}