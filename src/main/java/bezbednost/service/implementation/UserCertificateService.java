package bezbednost.service.implementation;

import bezbednost.dto.request.CreateUserCertificateRequest;
import bezbednost.dto.response.UserCertificateResponse;
import bezbednost.entity.User;
import bezbednost.entity.UserCertificate;
import bezbednost.repository.IUserCertificateRepository;
import bezbednost.repository.IUserRepository;
import bezbednost.service.IUserCertificateService;
import bezbednost.util.enums.UserType;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserCertificateService implements IUserCertificateService {

    private final IUserRepository _userRepository;

    private final IUserCertificateRepository _userCertificateRepository;

    public UserCertificateService(IUserRepository userRepository, IUserCertificateRepository userCertificateRepository) {
        _userRepository = userRepository;
        _userCertificateRepository = userCertificateRepository;
    }

    @Override
    public UserCertificateResponse createUserCertificate(CreateUserCertificateRequest request) throws Exception {
        User user = new User();
        Date now = new Date();
        user.setUserType(UserType.USER_CERTIFICATE);
        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setEmail(request.getEmail());
        user.setCountry(request.getCountry());
        _userRepository.save(user);
        UserCertificate userCertificate = new UserCertificate();
        userCertificate.setUser(user);
        userCertificate.setOrganisation(request.getOrganisation());
        userCertificate.setOrganisationUnit(request.getOrganisationUnit());
        userCertificate.setSerialNumber(request.getSerialNumber());
        userCertificate.setExtension(request.getExtension());
        userCertificate.setCertificateAuthority(request.isCertificateAuthority());
        userCertificate.setIssuerEmail(request.getIssuerEmail());
        userCertificate.setDate(now);
        userCertificate.setDeleted(false);
        _userCertificateRepository.save(userCertificate);

        return mapUserCertificateToUserCertificateResponse(userCertificate);
    }

    @Override
    public UserCertificateResponse getUserCertificate(UUID id) throws Exception {
        UserCertificate userCertificate = _userCertificateRepository.findOneById(id);
        return mapUserCertificateToUserCertificateResponse(userCertificate);
    }

    @Override
    public List<UserCertificateResponse> getAllUserCertificates() throws Exception {
        List<UserCertificate> userCertificates = _userCertificateRepository.findAllByDeleted(false);

        return userCertificates.stream()
                .map(userCertificate -> mapUserCertificateToUserCertificateResponse(userCertificate))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserCertificate(UUID id) throws Exception {
        UserCertificate userCertificate = _userCertificateRepository.findOneById(id);
        userCertificate.setDeleted(true);
        _userCertificateRepository.save(userCertificate);
    }

    private UserCertificateResponse mapUserCertificateToUserCertificateResponse(UserCertificate user){
        UserCertificateResponse userResponse = new UserCertificateResponse();
        userResponse.setId(user.getId());
        userResponse.setCountry(user.getUser().getCountry());
        userResponse.setEmail(user.getUser().getEmail());
        userResponse.setFirstName(user.getUser().getFirstName());
        userResponse.setLastName(user.getUser().getLastName());
        userResponse.setOrganisation(user.getOrganisation());
        userResponse.setOrganisationUnit(user.getOrganisationUnit());
        userResponse.setSerialNumber(user.getSerialNumber());
        userResponse.setDate(user.getDate());

        return userResponse;
    }
}
