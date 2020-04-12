package bezbednost.dto.request;

import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class OCSPRequest {

    private BigInteger serialNum;

    private UUID revoker;

    private String subject;

    private String issuer;
}
