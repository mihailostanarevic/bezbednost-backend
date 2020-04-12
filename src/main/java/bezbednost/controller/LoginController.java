package bezbednost.controller;

import bezbednost.dto.request.LoginRequest;
import bezbednost.dto.response.LoginResponse;
import bezbednost.service.ILoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final ILoginService _loginService;

    public LoginController(ILoginService loginService) {
        _loginService = loginService;
    }

    @PostMapping
    public LoginResponse login(@RequestBody LoginRequest request) throws Exception{
        return _loginService.login(request);
    }
}
