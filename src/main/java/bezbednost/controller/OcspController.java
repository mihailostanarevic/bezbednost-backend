package bezbednost.controller;

import bezbednost.service.implementation.OCSPListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.util.List;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/OCSPList")
public class OcspController {

    @Autowired
    OCSPListService ocspListService;

    @GetMapping(produces = "application/json", value = "/check/{email}")
    public boolean checkValidity(@PathVariable("email") String email) {
        X509Certificate certificate = ocspListService.getEndCertificateByName(email);
        boolean ret = ocspListService.checkCertificateValidity(certificate);
        return ret;
    }

    @GetMapping
    public List<>



}
