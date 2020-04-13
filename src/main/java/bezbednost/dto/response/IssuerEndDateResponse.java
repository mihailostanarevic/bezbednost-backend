package bezbednost.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuerEndDateResponse {

    @JsonFormat(pattern = "yyyy/MM/dd")
    private Date endDate;

}
