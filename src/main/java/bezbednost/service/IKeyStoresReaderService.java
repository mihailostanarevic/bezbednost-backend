package bezbednost.service;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

public interface IKeyStoresReaderService {

    List<String> readAliases(String keyStoreFile, String keyStorePass);

    List<X509Certificate> readAllCertificate(String keyStoreFile, String keyStorePass);

    X509Certificate readCertificate(String keyStoreFile, String keyStorePass, String alias);

    PrivateKey readPrivateKey(String keyStoreFile, String keyStorePass, String alias, String pass);

}
