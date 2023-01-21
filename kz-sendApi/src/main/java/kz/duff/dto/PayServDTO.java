package kz.duff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"result","resultMessage", "transactionId","transactionDate","statusDate","agentTransactionId","currencyRate","rate","finalAmount","currency","displays"})
@Accessors(chain = true)
public class PayServDTO implements Serializable {

    @JsonProperty("result")
    private int result;

    @JsonProperty("resultMessage")
    private String resultMessage;

    @JsonProperty("transactionId")
    private BigInteger transactionId;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("statusDate")
    private String statusDate;

    @JsonProperty("agentTransactionId")
    private BigInteger agentTransactionId;

    @JsonProperty("currencyRate")
    private String currencyRate;

    @JsonProperty("rate")
    private double rate;

    @JsonProperty("fixedPrice")
    private double fixedPrice;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("displays")
    private Map<String,String> displays;
}
