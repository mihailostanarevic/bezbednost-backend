package bezbednost.service;

import bezbednost.dto.request.CreateUserCertificateRequest;
import bezbednost.dto.response.UserCertificateResponse;

import java.util.List;
import java.util.UUID;

public interface IUserCertificateService {

    UserCertificateResponse createUserCertificate(CreateUserCertificateRequest request) throws Exception;

    UserCertificateResponse getUserCertificate(UUID id) throws Exception;

    List<UserCertificateResponse> getAllUserCertificates() throws Exception;

    void deleteUserCertificate(UUID id) throws Exception;
}
