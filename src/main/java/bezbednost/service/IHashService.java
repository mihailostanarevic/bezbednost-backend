package bezbednost.service;

public interface IHashService {

    byte[] generateSalt();

    byte[] hash(String hashEntity, byte[] salt);

}
