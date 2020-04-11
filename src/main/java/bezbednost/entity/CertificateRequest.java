package bezbednost.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRequest extends BaseEntity {

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String country;

    private Date startAt;

    private Date entAt;

    private String organisation;

    private String organisationUnit;

    private String extension;

    private boolean isCertificateAuthority;

    private String issuerEmail;
}
