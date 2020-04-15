package bezbednost.controller;

import bezbednost.service.implementation.OCSPService;
import bezbednost.util.enums.RevocationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
@RestController
@RequestMapping("/ocspList")
public class OCSPController {

    @Autowired
    OCSPService _ocspService;

    @GetMapping("/{serial_number}/check")
    public RevocationStatus getRevokedCertificates(@PathVariable("serial_number") String serialNumber) throws NumberFormatException {
        BigInteger serialNum;
        try {
             serialNum = new BigInteger(serialNumber);
        }catch (NumberFormatException e){
            throw new NumberFormatException("Number format is not valid.");
        }
        return _ocspService.checkCertificateStatus(serialNum);
    }

}
