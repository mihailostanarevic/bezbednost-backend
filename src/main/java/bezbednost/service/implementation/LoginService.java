package bezbednost.service.implementation;

import bezbednost.dto.request.LoginRequest;
import bezbednost.dto.response.AdminResponse;
import bezbednost.dto.response.LoginResponse;
import bezbednost.entity.Admin;
import bezbednost.repository.IAdminRepository;
import bezbednost.service.ILoginService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements ILoginService {

    private final IAdminRepository _adminRepository;

    private final PasswordEncoder _passwordEncoder;

    public LoginService(IAdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        _adminRepository = adminRepository;
        _passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) throws Exception {
        Admin admin = _adminRepository.findOneByEmail(request.getEmail());

        if (admin == null) {
            throw new Exception(String.format("Bad credentials."));
        }

        if (!_passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new Exception("Bad credentials.");
        }

        LoginResponse loginResponse = new LoginResponse();
        AdminResponse adminResponse = new AdminResponse();
        adminResponse.setId(admin.getId());
        adminResponse.setEmail(admin.getEmail());
        loginResponse.setAdmin(adminResponse);

        return loginResponse;
    }
}
