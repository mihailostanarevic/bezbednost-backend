package bezbednost.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigInteger;
import java.util.UUID;

@SuppressWarnings({"unused"})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OCSP extends BaseEntity {

    @Column(name = "serial_num")
    private BigInteger serialNum;

    private UUID revoker;

    private String subject;

    private String issuer;
}
