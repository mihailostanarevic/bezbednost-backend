package bezbednost.service.implementation;

import bezbednost.service.IKeyStoresWriterService;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

@Service
public class KeyStoresWriterService implements IKeyStoresWriterService {

    private KeyStore keyStore;

    public KeyStoresWriterService() {
        try {
            keyStore = KeyStore.getInstance("JKS", "SUN");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

   /* public void createKeyStoreFile(String fileName, String password) {
        this.loadKeyStore(null, password.toCharArray());
        this.saveKeyStore(fileName,password.toCharArray());
    }*/


   //ucitavam iz file keyStore u objekat keyStore
    public void loadKeyStore(String fileName, char[] password) {
        try {
            if(fileName != null) {
                keyStore.load(new FileInputStream(fileName), password);
            } else {
                //Ako je cilj kreirati novi KeyStore poziva se i dalje load, pri cemu je prvi parametar null
                keyStore.load(null, password);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //poziva se nakon svakog osvezavanja keyStora
    public void saveKeyStore(String fileName, char[] password) {
        try {
            keyStore.store(new FileOutputStream(fileName), password);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //ubacivanje sertifikata u keystore
    //alias - alias putem kog se identifikuje sertifikat izdavaoca
    //privateKey - privatni kljuc sertifikata koji se ubacuje u keyStore
    //password - lozinka koja je neophodna da se otvori key store
    @Override
    public void write(String alias, PrivateKey privateKey, String fileName,String password, Certificate certificate) {
        try {
            this.loadKeyStore(fileName,password.toCharArray());
            if(!keyStore.containsAlias(alias)) {
                //keyStore.setCertificateEntry(alias, certificate);
                keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), new Certificate[]{certificate});
                this.saveKeyStore(fileName,password.toCharArray());
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }



}
