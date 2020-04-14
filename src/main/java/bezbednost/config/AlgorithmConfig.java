package bezbednost.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
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

    @Value("${bezbednost.root.filename}")
    private String rootFileName;

    @Value("${bezbednost.ca.filename}")
    private String CAFileName;

    @Value("${bezbednost.enduser.filename}")
    private String end_userFileName;

    @Value("${bezbednost.password}")
    private String ksPassword;

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

    public String getRootFileName() {
        return rootFileName;
    }

    public void setRootFileName(String rootFileName) {
        this.rootFileName = rootFileName;
    }

    public String getCAFileName() {
        return CAFileName;
    }

    public void setCAFileName(String CAFileName) {
        this.CAFileName = CAFileName;
    }

    public String getEnd_userFileName() {
        return end_userFileName;
    }

    public void setEnd_userFileName(String end_userFileName) {
        this.end_userFileName = end_userFileName;
    }

    public String getKsPassword() {
        return ksPassword;
    }

    public void setKsPassword(String ksPassword) {
        this.ksPassword = ksPassword;
    }
}
