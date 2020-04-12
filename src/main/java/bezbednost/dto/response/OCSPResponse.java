package bezbednost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OCSPResponse {

    private BigInteger serialNum;

    private UUID revoker;

    private String subject;

    private String issuer;

    private UUID id;
}
