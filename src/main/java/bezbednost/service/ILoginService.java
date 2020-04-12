package bezbednost.service;

import bezbednost.dto.request.LoginRequest;
import bezbednost.dto.response.LoginResponse;

public interface ILoginService {

    LoginResponse login(LoginRequest request) throws Exception;
}
