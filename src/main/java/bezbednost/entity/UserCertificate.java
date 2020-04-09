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
public class UserCertificate extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private Date date;

    private String organisation;

    private String organisationUnit;

    @Column(unique = true)
    private String serialNumber;

    private String extension;

    private boolean isCertificateAuthority;

    private String issuerEmail;

    private boolean deleted;
}
