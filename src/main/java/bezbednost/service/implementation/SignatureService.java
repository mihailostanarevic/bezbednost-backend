package bezbednost.service.implementation;

import bezbednost.service.ISignatureService;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.*;
import java.security.cert.X509Certificate;

@SuppressWarnings({"SpellCheckingInspection", "TryWithIdenticalCatches"})
public class SignatureService implements ISignatureService {

    @Autowired
    HashService hashService;

    @Override
    public KeyPair generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);

            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public byte[] sign(X509Certificate certificate, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);

            byte[] salt = certificate.getSerialNumber().toByteArray();
            byte[] hashedCertificate = hashService.hash(certificate.toString(), salt);
            System.out.println("HashedCertificate: " + hashedCertificate);
            //Postavljamo podatke koje potpisujemo
            sig.update(hashedCertificate);

            return sig.sign();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean verify(X509Certificate certificate, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);

            byte[] salt = certificate.getSerialNumber().toByteArray();
            byte[] hashedCertificate = hashService.hash(certificate.toString(), salt);
            sig.update(hashedCertificate);

            return sig.verify(signature);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }
}
