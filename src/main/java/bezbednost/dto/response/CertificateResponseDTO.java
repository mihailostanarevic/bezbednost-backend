package bezbednost.dto.response;

import bezbednost.util.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@SuppressWarnings("SpellCheckingInspection")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateResponseDTO {

    private String firstName;

    private String lastName;

    private String email;

    private Date expireDate;

    private String CAemail;

    private CertificateType certificateType;
}
