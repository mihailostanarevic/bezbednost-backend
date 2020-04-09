package bezbednost.config;

import bezbednost.entity.Admin;
import bezbednost.entity.User;
import bezbednost.repository.IAdminRepository;
import bezbednost.repository.IUserRepository;
import bezbednost.util.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private IAdminRepository adminRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        User user = new User();
        user.setCountry(String.format("Serbia"));
        user.setEmail(String.format("admin@gmail.com"));
        user.setFirstName(String.format("Predef"));
        user.setLastName(String.format("Admin"));
        user.setUserType(UserType.ADMIN);
        UUID adminId = UUID.fromString("a351022a-cc7a-4a17-a79b-8e0cf258db07");
        Admin admin = adminRepository.findOneById(adminId);
        userRepository.save(user);
        admin.setUser(user);
        adminRepository.save(admin);
    }
}
