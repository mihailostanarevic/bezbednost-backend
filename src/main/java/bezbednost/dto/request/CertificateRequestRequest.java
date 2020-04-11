package bezbednost.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CertificateRequestRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String country;

    private String organisation;

    private String organisationUnit;

    private String extension;

    private boolean certificateAuthority;

    private String issuerEmail;

    @JsonFormat(pattern = "yyyy/MM/dd")
    private Date endDate;
}
