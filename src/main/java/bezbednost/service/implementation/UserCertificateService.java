package bezbednost.service.implementation;

import bezbednost.dto.request.CreateUserCertificateRequest;
import bezbednost.dto.response.UserCertificateResponse;
import bezbednost.entity.User;
import bezbednost.entity.UserCertificate;
import bezbednost.repository.IUserCertificateRepository;
import bezbednost.repository.IUserRepository;
import bezbednost.service.IKeyStoresReaderService;
import bezbednost.service.IUserCertificateService;
import bezbednost.util.enums.UserType;
import ch.qos.logback.core.net.SyslogOutputStream;
import keyStore.KeyStoreWriter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UserCertificateService implements IUserCertificateService {

    private final IUserRepository _userRepository;

    private final IUserCertificateRepository _userCertificateRepository;

    @Autowired
    private IKeyStoresReaderService _keyStoresReaderService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private KeyStoresWriterService _keyStoreWriterService;

    public UserCertificateService(IUserRepository userRepository, IUserCertificateRepository userCertificateRepository) {
        _userRepository = userRepository;
        _userCertificateRepository = userCertificateRepository;
    }

    @Override
    public ResponseEntity<String> createUserCertificate(CreateUserCertificateRequest request) throws Exception {
        User user = new User();
        Date now = new Date();
        user.setUserType(UserType.USER_CERTIFICATE);
        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setEmail(request.getEmail());
        user.setCountry(request.getCountry());
        _userRepository.save(user);
        UserCertificate userCertificate = new UserCertificate();
        userCertificate.setUser(user);
        userCertificate.setOrganisation(request.getOrganisation());
        userCertificate.setOrganisationUnit(request.getOrganisationUnit());
        userCertificate.setSerialNumber(request.getSerialNumber());
        userCertificate.setExtension(request.getExtension());
        userCertificate.setCertificateAuthority(request.isCertificateAuthority());
        userCertificate.setIssuerEmail(request.getIssuerEmail());
        userCertificate.setDate(now);
        userCertificate.setDeleted(false);
        _userCertificateRepository.save(userCertificate);

        User u = _userRepository.findOneByEmail(request.getEmail());
        UUID id = u.getId();

        X509Certificate cert = this.generateCertificate(request, id);

        return new ResponseEntity<String>("Certification for user " + user.getFirstName() + " has been created",HttpStatus.CREATED);
    }

    @Override
    public UserCertificateResponse getUserCertificate(UUID id) throws Exception {
        UserCertificate userCertificate = _userCertificateRepository.findOneById(id);
        return mapUserCertificateToUserCertificateResponse(userCertificate);
    }

    @Override
    public List<UserCertificateResponse> getAllUserCertificates() throws Exception {
        List<UserCertificate> userCertificates = _userCertificateRepository.findAllByDeleted(false);

        return userCertificates.stream()
                .map(userCertificate -> mapUserCertificateToUserCertificateResponse(userCertificate))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserCertificate(UUID id) throws Exception {
        UserCertificate userCertificate = _userCertificateRepository.findOneById(id);
        userCertificate.setDeleted(true);
        _userCertificateRepository.save(userCertificate);
    }

    private UserCertificateResponse mapUserCertificateToUserCertificateResponse(UserCertificate user){
        UserCertificateResponse userResponse = new UserCertificateResponse();
        userResponse.setId(user.getId());
        userResponse.setCountry(user.getUser().getCountry());
        userResponse.setEmail(user.getUser().getEmail());
        userResponse.setFirstName(user.getUser().getFirstName());
        userResponse.setLastName(user.getUser().getLastName());
        userResponse.setOrganisation(user.getOrganisation());
        userResponse.setOrganisationUnit(user.getOrganisationUnit());
        userResponse.setSerialNumber(user.getSerialNumber());
        userResponse.setDate(user.getDate());

        return userResponse;
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
    private X509Certificate generateCertificate(CreateUserCertificateRequest data,  UUID id) throws CertificateException {
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
        KeyPair keyPair = this.signatureService.generateKeys();

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
                new BigInteger(data.getSerialNumber()),
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

    private X500Name getX500Name(CreateUserCertificateRequest data, UUID id){
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
