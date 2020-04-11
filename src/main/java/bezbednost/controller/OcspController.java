package bezbednost.controller;

import bezbednost.service.IOCSPService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/ocsp")
public class OcspController {

    @GetMapping
    public List<UserCertificateResponse> getAll() {
        // TODO (A)
        return null;
    }

}
