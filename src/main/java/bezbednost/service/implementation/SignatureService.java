package bezbednost.service.implementation;

import bezbednost.config.AlgorithmConfig;
import bezbednost.service.ISignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.cert.X509Certificate;

@Service
public class SignatureService implements ISignatureService {

    @Autowired
    AlgorithmConfig config;

    private final HashService _hashService;

    public SignatureService(HashService hashService) {
        _hashService = hashService;
    }

    @Override
    public KeyPair generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstanceStrong();
            keyGen.initialize(2048, random);

            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public KeyPair generateKeys(boolean isCA) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(config.getKeyAlgorithm());
            SecureRandom random = SecureRandom.getInstanceStrong();
            if (isCA)
                keyGen.initialize(config.getCaKeySize(), random);
            else
                keyGen.initialize(config.getUserKeysize(), random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param certificate the certificate to be signed
     * @param privateKey CA private key
     * @return digital signature
     */
    @Override
    public byte[] sign(X509Certificate certificate, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);

            byte[] salt = certificate.getSerialNumber().toByteArray();
            byte[] hashedCertificate = _hashService.hash(certificate.toString(), salt);
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

    /**
     * @param certificate the certificate to be verified
     * @param signature digital signature in certificate
     * @param publicKey CA public key
     * @return true - valid signature, false - invalid signature
     */
    @Override
    public boolean verify(X509Certificate certificate, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);

            byte[] salt = certificate.getSerialNumber().toByteArray();
            byte[] hashedCertificate = _hashService.hash(certificate.toString(), salt);
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
