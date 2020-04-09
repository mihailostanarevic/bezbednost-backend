package bezbednost.service;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public interface IKeyStoresWriterService {

    public void write(String alias, PrivateKey privateKey, String fileName, String password, Certificate certificate);

}
