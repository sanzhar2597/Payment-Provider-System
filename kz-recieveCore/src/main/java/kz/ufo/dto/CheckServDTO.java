package kz.ufo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kz.ufo.dto.invoiceT.InvoiceTempl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;


import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class CheckServDTO implements Serializable {
    @JsonProperty("result")
    private int result;

    @JsonProperty("resultMessage")
    private String resultMessage;

    @JsonProperty("status")
    private int status;

    @JsonProperty("transactionId")
    private BigInteger transactionId;

    @JsonProperty("agentTransactionId")
    private BigInteger agentTransactionId;

    @JsonProperty("referenceNumber")
    private BigInteger referenceNumber;

    @JsonProperty("systemName")
    private String systemName;

    @JsonProperty("fatal")
    private String fatal;

    @JsonProperty("agentId")
    private int agentId;

    @JsonProperty("currencyRate")
    private String currencyRate;

    @JsonProperty("rate")
    private double rate;

    @JsonProperty("fixedPrice")
    private double fixedPrice;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("invoice")
    private InvoiceTempl invoice;

    @JsonProperty("displays")
    private Map<String,String> displays= Collections.emptyMap();
}
