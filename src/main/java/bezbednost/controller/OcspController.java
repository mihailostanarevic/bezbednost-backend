package bezbednost.controller;

import bezbednost.service.IOCSPService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/ocsp")
public class OcspController {

    private final IOCSPService _ocspService;

    public OcspController(IOCSPService ocspService) {
        _ocspService = ocspService;
    }
}
