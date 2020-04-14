package bezbednost.service.implementation;

import bezbednost.dto.request.DownloadRequest;
import bezbednost.entity.FileRelations;
import bezbednost.repository.IFileRelationsRepository;
import bezbednost.service.ICertificateService;
import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
@Service
public class CertificateService implements ICertificateService {

    @Autowired
    KeyStoresReaderService _keyStoresReaderService;

    private final IFileRelationsRepository _fileRelationsRepository;

    @Autowired
    OCSPService _ocspListService;

    public CertificateService(IFileRelationsRepository fileRelationsRepository){
        _fileRelationsRepository = fileRelationsRepository;
    }

    @Override
    public List<X509Certificate> getAllActiveEndUserCertificates() {
        List<X509Certificate> retList = new ArrayList<>();
        List<X509Certificate> certificates = _keyStoresReaderService.readAllCertificate("keystoreEndUser.jks", "admin");
        for (X509Certificate certificate : certificates) {
            if (_ocspListService.checkCertificateValidity(certificate)) {
                retList.add(certificate);
            }
        }

        return retList;
    }

    @Override
    public List<X509Certificate> getAllActiveCACertificates() {
        List<X509Certificate> retList = new ArrayList<>();
        List<X509Certificate> CACertificates = _keyStoresReaderService.readAllCertificate("keystoreIntermediate.jks", "admin");
        for (X509Certificate certificate : CACertificates) {
            if (_ocspListService.checkCertificateValidity(certificate)) {
                retList.add(certificate);
            }
        }

        return retList;
    }

    @Override
    public List<X509Certificate> getAllActiveRootCertificates() {
        List<X509Certificate> retList = new ArrayList<>();
        List<X509Certificate> RootCertificates = _keyStoresReaderService.readAllCertificate("keystoreRoot.jks", "admin");
        for (X509Certificate certificate : RootCertificates) {
            if (_ocspListService.checkCertificateValidity(certificate)) {
                retList.add(certificate);
            }
        }

        return retList;
    }

    public ResponseEntity<Object> downloadCertificate(DownloadRequest request){
        FileRelations fr = _fileRelationsRepository.findOneByEmail(request.getEmail());
        String fileName = "certificates\\" + fr.getFileName();

        File file = new File(fileName);
        InputStreamResource resource = null;
        try {
            resource = new InputStreamResource(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(file.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename="+file.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
        return responseEntity;
    }

    public void saveCertificate(X509Certificate certificate, String extension) throws IOException {
        String fileName = certificate.getSubjectDN().getName().split(",")[0].split("=")[1].trim();
        String fn = fileName + extension;

        fileName = "certificates\\" + fileName + extension;

        StringWriter sw = new StringWriter();
        try ( JcaPEMWriter pw = new JcaPEMWriter( sw ) )
        {
            pw.writeObject(certificate);
        }

        FileWriter fw = new FileWriter(fileName);
        fw.write(sw.toString());
        fw.close();

        FileRelations fr = new FileRelations(certificate.getSubjectDN().getName().split(",")[6].split("=")[1], fn);
        _fileRelationsRepository.save(fr);
    }

    public List<String> getFileName(String email){
        FileRelations file = _fileRelationsRepository.findOneByEmail(email);
        List<String> retVal = new ArrayList<String>();
        retVal.add(file.getFileName());
        return retVal;
    }
}
