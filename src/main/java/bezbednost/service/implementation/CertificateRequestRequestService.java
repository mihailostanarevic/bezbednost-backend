package bezbednost.service.implementation;

import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.response.CertificateRequestResponse;
import bezbednost.entity.CertificateRequest;
import bezbednost.repository.ICertificateRequestRepository;
import bezbednost.service.ICertificateRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateRequestRequestService implements ICertificateRequestService {

    private final ICertificateRequestRepository _certificateRequestRepository;

    public CertificateRequestRequestService(ICertificateRequestRepository certificateRequestRepository) {
        _certificateRequestRepository = certificateRequestRepository;
    }

    @Override
    public CertificateRequestResponse createCertificateRequest(CertificateRequestRequest request) throws Exception {
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setCountry(request.getCountry());
        certificateRequest.setFirstName(request.getFirstName());
        certificateRequest.setLastName(request.getLastName());
        certificateRequest.setOrganisation(request.getOrganisation());
        certificateRequest.setOrganisationUnit(request.getOrganisationUnit());
        certificateRequest.setEmail(request.getEmail());
        certificateRequest.setExtension(request.getExtension());
        certificateRequest.setCertificateAuthority(request.isCertificateAuthority());
        _certificateRequestRepository.save(certificateRequest);

        return mapCertificateRequestToCertificateRequestResponse(certificateRequest);
    }

    @Override
    public CertificateRequestResponse getCertificateRequest(UUID id) throws Exception {
        CertificateRequest certificateRequest = _certificateRequestRepository.findOneById(id);
        return mapCertificateRequestToCertificateRequestResponse(certificateRequest);
    }

    @Override
    public List<CertificateRequestResponse> getAllCertificateRequests() throws Exception {
        List<CertificateRequest> certificateRequests = _certificateRequestRepository.findAll();

        if(certificateRequests.isEmpty()){
            throw new Exception("You do not have new certificate requests");
        }

        return certificateRequests.stream()
                .map(certificateRequest -> mapCertificateRequestToCertificateRequestResponse(certificateRequest))
                .collect(Collectors.toList());
    }

    @Override
    public void approveCertificateRequest(CertificateRequestRequest request) throws Exception {
        CertificateRequest certificateRequest = _certificateRequestRepository.findOneByEmail(request.getEmail());
        _certificateRequestRepository.delete(certificateRequest);
        //generisanje sertifikata, i cuvanje u keystore
    }

    @Override
    public void denyCertificateRequest(UUID certificateId) throws Exception {
        _certificateRequestRepository.deleteById(certificateId);
    }


    private CertificateRequestResponse mapCertificateRequestToCertificateRequestResponse(CertificateRequest request){
        CertificateRequestResponse response = new CertificateRequestResponse();
        response.setId(request.getId());
        response.setCountry(request.getCountry());
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setOrganisation(request.getOrganisation());
        response.setOrganisationUnit(request.getOrganisationUnit());
        response.setExtension(request.getExtension());
        if(request.isCertificateAuthority()){
            response.setCaOrEnd("Certificate authority");
            return response;
        }

        response.setCaOrEnd("End user");
        return response;
    }
/*
    private X509Certificate generateCertificate(UserCertificate subjectData) throws CertificateException {
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
        builder = builder.setProvider("BC");

        ContentSigner contentSigner = builder.build(issuerData.getPrivateKey());

        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuerData.getX500name(),
                new BigInteger(subjectData.getSerialNumber()),
                subjectData.getStartDate(),
                subjectData.getEndDate(),
                subjectData.getX500name(),
                subjectData.getPublicKey());
        X509CertificateHolder certHolder = certGen.build(contentSigner);

        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        certConverter = certConverter.setProvider("BC");

        return certConverter.getCertificate(certHolder);
    }

    private X500NameBuilder getX500Name(UserCertificate user){
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        User u =
        String cn = user.n
        builder.addRDN(BCStyle.CN, "Goran Sladic");
        builder.addRDN(BCStyle.SURNAME, "Sladic");
        builder.addRDN(BCStyle.GIVENNAME, "Goran");
        builder.addRDN(BCStyle.O, "UNS-FTN");
        builder.addRDN(BCStyle.OU, "Katedra za informatiku");
        builder.addRDN(BCStyle.C, "RS");
        builder.addRDN(BCStyle.E, "sladicg@uns.ac.rs");
        //UID (USER ID) je ID korisnika
        builder.addRDN(BCStyle.UID, "123456");
    }
 */
}
