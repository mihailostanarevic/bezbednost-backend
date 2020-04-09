package bezbednost.service;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

public interface IKeyStoresReaderService {

    public List<String> readAliases (String keyStoreFile, String keyStorePass);

    public List<X509Certificate> readAllCertificate (String keyStoreFile, String keyStorePass);

    public X509Certificate readCertificate(String keyStoreFile, String keyStorePass, String alias);

    public PrivateKey readPrivateKey(String keyStoreFile, String keyStorePass, String alias, String pass);

}
