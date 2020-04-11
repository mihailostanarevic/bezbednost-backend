package bezbednost.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public interface ISignatureService {

    KeyPair generateKeys();

    byte[] sign(X509Certificate certificate, PrivateKey privateKey);

    boolean verify(X509Certificate certificate, byte[] signature, PublicKey publicKey);
}
