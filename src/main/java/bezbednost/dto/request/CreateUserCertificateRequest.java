package bezbednost.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class CreateUserCertificateRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String country;

    private String organisation;

    private String organisationUnit;

    private String serialNumber;

    private String extension;

    private boolean certificateAuthority;

    private String issuerEmail;

    private Date endDate;
}
