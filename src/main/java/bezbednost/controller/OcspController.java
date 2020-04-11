package bezbednost.controller;

import bezbednost.dto.response.UserCertificateResponse;
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

    @GetMapping
    public List<UserCertificateResponse> getAll() {
        // TODO (A)
        return null;
    }

}
