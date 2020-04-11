package bezbednost.controller;

import bezbednost.converter.CertificateConverter;
import bezbednost.dto.response.CertificateResponseDTO;
import bezbednost.service.implementation.CertificateService;
import bezbednost.util.enums.CertificateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/certificate")
public class CertificateController {

    @Autowired
    CertificateService _certificateService;

    @GetMapping
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

}
