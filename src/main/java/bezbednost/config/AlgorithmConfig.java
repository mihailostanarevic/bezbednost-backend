package bezbednost.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmConfig {

    //parametri su definisani u properties fajlu
    @Value("${bezbednost.certificate.provider}")
    private String provider;

    @Value("${bezbednost.algorithm.signature}")
    private String signatureAlgorithm;

    @Value("${bezbednost.algorithm.key}")
    private String keyAlgorithm;

    @Value("${bezbednost.seed.algorithm}")
    private String seedAlgorithm;

    @Value("${bezbednost.seed.provider}")
    private String seedProvider;

    @Value("${bezbednost.user.keysize}")
    private String userKeysize;

    @Value("${bezbednost.ca.keysize}")
    private String caKeySize;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getSeedAlgorithm() {
        return seedAlgorithm;
    }

    public void setSeedAlgorithm(String seedAlgorithm) {
        this.seedAlgorithm = seedAlgorithm;
    }

    public String getSeedProvider() {
        return seedProvider;
    }

    public void setSeedProvider(String seedProvider) {
        this.seedProvider = seedProvider;
    }

    public int getUserKeysize() {
        return Integer.parseInt(userKeysize);
    }

    public void setUserKeysize(int userKeysize) {
        this.userKeysize = userKeysize+"";
    }

    public int getCaKeySize() {
        return Integer.parseInt(caKeySize);
    }

    public void setCaKeySize(int caKeySize) {
        this.caKeySize = caKeySize+"";
    }
}
