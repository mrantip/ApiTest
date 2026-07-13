package api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transactions extends BaseModel {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private String timestampAsString;
    private long relatedAccountId;
    private long amountAsDouble;
}