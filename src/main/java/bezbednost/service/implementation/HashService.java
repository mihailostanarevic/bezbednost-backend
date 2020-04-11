package bezbednost.service.implementation;

import bezbednost.service.IHashService;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Service
public class HashService implements IHashService {

    @Override
    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    @Override
    public byte[] hash(String hashEntity, byte[] salt) {
        SecretKeyFactory factory;
        try {
            KeySpec spec = new PBEKeySpec(hashEntity.toCharArray(), salt, 65536, 128);
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }
}
