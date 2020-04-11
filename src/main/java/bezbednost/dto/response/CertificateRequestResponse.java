package bezbednost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRequestResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String country;

    private String organisation;

    private String organisationUnit;

    private String extension;

    private String caOrEnd;
}
