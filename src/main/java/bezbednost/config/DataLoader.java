package bezbednost.config;

import bezbednost.entity.Admin;
import bezbednost.entity.User;
import bezbednost.repository.IAdminRepository;
import bezbednost.repository.IUserRepository;
import bezbednost.service.implementation.KeyStoresReaderService;
import bezbednost.service.implementation.KeyStoresWriterService;
import bezbednost.service.implementation.SignatureService;
import bezbednost.util.enums.UserType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.hibernate.annotations.NaturalId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private IAdminRepository adminRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private KeyStoresWriterService keyStoresWriterService;

    @Autowired
    private SignatureService signatureService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        User user = new User();
        user.setCountry(String.format("Serbia"));
        user.setEmail(String.format("admin@gmail.com"));
        user.setFirstName(String.format("Predef"));
        user.setLastName(String.format("Admin"));
        user.setUserType(UserType.ADMIN);
        UUID adminId = UUID.fromString("a351022a-cc7a-4a17-a79b-8e0cf258db07");
        Admin admin = adminRepository.findOneById(adminId);
        userRepository.save(user);
        admin.setUser(user);
        adminRepository.save(admin);

        KeyPair keyPair = signatureService.generateKeys();
        X509Certificate cert = this.createInitialRootCertificate(keyPair);
        keyStoresWriterService.write("rootCA@gmail.com", keyPair.getPrivate(), "keystoreRoot.jks", "admin", cert);
    }

    private X509Certificate createInitialRootCertificate(KeyPair keyPair){
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
        builder = builder.setProvider("BC");

        ContentSigner contentSigner = null;
        try {
            contentSigner = builder.build(keyPair.getPrivate());
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }

        X500Name x500Name = this.getX500Name();

        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, 10);

        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(x500Name,
                new BigInteger("123456789"),
                new Date(),
                c.getTime(),
                x500Name,
                keyPair.getPublic());
        try {
            certGen.addExtension(X509Extensions.BasicConstraints, true,
                    new BasicConstraints(true));
        } catch (CertIOException e) {
            e.printStackTrace();
        }

        X509CertificateHolder certHolder = certGen.build(contentSigner);

        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        certConverter = certConverter.setProvider("BC");

        try {
            return certConverter.getCertificate(certHolder);
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return null;
    }

    private X500Name getX500Name(){
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        String cn = "Goran Sladic";
        builder.addRDN(BCStyle.CN, cn);
        builder.addRDN(BCStyle.SURNAME, "Sladic");
        builder.addRDN(BCStyle.GIVENNAME, "Goran");
        builder.addRDN(BCStyle.O, "FTN");
        builder.addRDN(BCStyle.OU, "UNS");
        builder.addRDN(BCStyle.C, "RS");
        builder.addRDN(BCStyle.E, "rootCA@gmail.com");
        //UID (USER ID) je ID korisnika
        builder.addRDN(BCStyle.UID, String.valueOf("a5af6079-5511-4eec-845e-c8b62539978d"));
        return builder.build();
    }
}
