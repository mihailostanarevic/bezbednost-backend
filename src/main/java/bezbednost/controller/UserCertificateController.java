package bezbednost.controller;

import bezbednost.dto.request.CreateUserCertificateRequest;
import bezbednost.dto.response.UserCertificateResponse;
import bezbednost.service.implementation.UserCertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user-certificate")
public class UserCertificateController {

    private final UserCertificateService _userCertificateService;

    public UserCertificateController(UserCertificateService userCertificateService) {
        _userCertificateService = userCertificateService;
    }

    @PostMapping
    public ResponseEntity<String> createUserCertificate(@RequestBody CreateUserCertificateRequest request) throws Exception {
        return _userCertificateService.createUserCertificate(request);
    }

    @GetMapping
    public List<UserCertificateResponse> getAllUserCertificates() throws Exception{
        return _userCertificateService.getAllUserCertificates();
    }

    @GetMapping("/{id}/user-certficate")
    public UserCertificateResponse getUserCertificate(@PathVariable UUID id) throws Exception{
        return _userCertificateService.getUserCertificate(id);
    }

    @DeleteMapping("/{id}/user-certficate")
    public void deleteUserCertificate(@PathVariable UUID id) throws Exception{
        _userCertificateService.deleteUserCertificate(id);
    }
}
