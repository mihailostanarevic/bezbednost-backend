package bezbednost.controller;

import bezbednost.dto.request.EmailRequestDTO;
import bezbednost.dto.response.CertificateResponseDTO;
import bezbednost.service.implementation.CertificateService;
import bezbednost.service.implementation.OCSPService;
import bezbednost.util.enums.CertificateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
@RestController
@RequestMapping("/certificate")
public class CertificateController {

    @Autowired
    CertificateService _certificateService;

    @Autowired
    OCSPService _ocspService;

    @GetMapping()
    public List<CertificateResponseDTO> getAllValidCertificates(){
        List<X509Certificate> endUserCertificates = _certificateService.getAllActiveEndUserCertificates();
        List<X509Certificate> CACertificates = _certificateService.getAllActiveIntermediateCertificates();
        List<X509Certificate> rootCertificates = _certificateService.getAllActiveRootCertificates();

        List<CertificateResponseDTO> retList = new ArrayList<>();
        retList.addAll(_certificateService.listToDTO(CertificateType.ROOT, rootCertificates));
        retList.addAll(_certificateService.listToDTO(CertificateType.INTERMEDIATE, CACertificates));
        retList.addAll(_certificateService.listToDTO(CertificateType.END_USER, endUserCertificates));
        return retList;
    }

    @GetMapping("/end-user")
    public List<CertificateResponseDTO> getAllEndUserCertificates(){
        List<X509Certificate> certificateList = _certificateService.getAllActiveEndUserCertificates();
        return _certificateService.listToDTO(CertificateType.END_USER, certificateList);
    }

    @GetMapping("/ca")
    public List<CertificateResponseDTO> getAllValidCACertificates(){
        List<X509Certificate> intermediateCertificates = _certificateService.getAllActiveIntermediateCertificates();
        List<X509Certificate> rootCertificates = _certificateService.getAllActiveRootCertificates();

        List<CertificateResponseDTO> retList = new ArrayList<>();
        retList.addAll(_certificateService.listToDTO(CertificateType.ROOT, rootCertificates));
        retList.addAll(_certificateService.listToDTO(CertificateType.INTERMEDIATE,intermediateCertificates));
        return retList;
    }

    @GetMapping("/root")
    public List<CertificateResponseDTO> getAllRootCertificates(){
        List<X509Certificate> certificateList = _certificateService.getAllActiveRootCertificates();
        return _certificateService.listToDTO(CertificateType.ROOT, certificateList);
    }

    @PostMapping("/download")
    public ResponseEntity<Object> downloadCertificate(@RequestBody EmailRequestDTO request){
        return _certificateService.downloadCertificate(request);
    }

    @PostMapping("/revoke")
    public ResponseEntity<HttpStatus> revokeCertificate(@RequestBody EmailRequestDTO request) {
        _ocspService.revokeCertificate(request.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
