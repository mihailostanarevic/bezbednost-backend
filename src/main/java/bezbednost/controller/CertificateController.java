package bezbednost.controller;

import bezbednost.converter.CertificateConverter;
import bezbednost.dto.request.CertificateRequestRequest;
import bezbednost.dto.request.DownloadRequest;
import bezbednost.dto.response.CertificateResponseDTO;
import bezbednost.service.implementation.CertificateService;
import bezbednost.util.enums.CertificateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sun.misc.Request;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/certificate")
public class CertificateController {

    @Autowired
    CertificateService _certificateService;

    @GetMapping()
    public List<CertificateResponseDTO> getAllValidCertificates(){
        List<CertificateResponseDTO> retList = new ArrayList<>();
        List<X509Certificate> endUserCertificates = _certificateService.getAllActiveEndUserCertificates();
        List<X509Certificate> CACertificates = _certificateService.getAllActiveCACertificates();
        List<X509Certificate> rootCertificates = _certificateService.getAllActiveRootCertificates();

        for (X509Certificate certificate : endUserCertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.END_USER);
            retList.add(certificateResponseDTO);
        }
        for (X509Certificate certificate : CACertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.INTERMEDIATE);
            retList.add(certificateResponseDTO);
        }
        for (X509Certificate certificate : rootCertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.ROOT);
            retList.add(certificateResponseDTO);
        }

        return retList;
    }

    @GetMapping("/end-user")
    public List<CertificateResponseDTO> getAllEndUserCertificates(){
        List<CertificateResponseDTO> retList = new ArrayList<>();
        List<X509Certificate> endUserCertificates = _certificateService.getAllActiveEndUserCertificates();

        for (X509Certificate certificate : endUserCertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.END_USER);
            retList.add(certificateResponseDTO);
        }

        return retList;
    }

    @GetMapping(value = "/ca")
    public List<CertificateResponseDTO> getAllValidCACertificates(){
        List<CertificateResponseDTO> retList = new ArrayList<>();
        List<X509Certificate> CACertificates = _certificateService.getAllActiveCACertificates();
        List<X509Certificate> rootCertificates = _certificateService.getAllActiveRootCertificates();

        for (X509Certificate certificate : CACertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.INTERMEDIATE);
            retList.add(certificateResponseDTO);
        }
        for (X509Certificate certificate : rootCertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.ROOT);
            retList.add(certificateResponseDTO);
        }

        return retList;
    }

    @GetMapping("/root")
    public List<CertificateResponseDTO> getAllRootCertificates(){
        List<CertificateResponseDTO> retList = new ArrayList<>();
        List<X509Certificate> rootCertificates = _certificateService.getAllActiveRootCertificates();

        for (X509Certificate certificate : rootCertificates) {
            CertificateResponseDTO certificateResponseDTO = CertificateConverter.toCertificateResponseDTO(certificate);
            certificateResponseDTO.setCertificateType(CertificateType.ROOT);
            retList.add(certificateResponseDTO);
        }

        return retList;
    }

    @PostMapping("/download")
    public ResponseEntity<Object> downloadCertificate(@RequestBody DownloadRequest request){
        return _certificateService.downloadCertificate(request);
    }

    @PostMapping("/file-name")
    public List<String> getFileName(@RequestBody DownloadRequest request){
        return _certificateService.getFileName(request.getEmail());
    }
}
