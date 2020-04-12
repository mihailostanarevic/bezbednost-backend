package bezbednost.controller;

import bezbednost.dto.response.OCSPResponse;
import bezbednost.service.IOCSPService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/ocsp")
public class OCSPController {

    private final IOCSPService _ocspService;

    public OCSPController(IOCSPService ocspService) {
        _ocspService = ocspService;
    }

    @GetMapping
    public List<OCSPResponse> getAllOCSPs() { return _ocspService.getAll(); }
}
