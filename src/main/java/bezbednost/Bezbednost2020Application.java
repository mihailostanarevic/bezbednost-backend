package bezbednost;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SuppressWarnings("ALL")
@SpringBootApplication
public class Bezbednost2020Application {

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(Bezbednost2020Application.class, args);
	}

}
