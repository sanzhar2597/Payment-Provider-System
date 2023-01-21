package kz.ufo.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"serviceId", "account", "agentTransactionId", "agentTransactionDate", "amountTo", "amountFrom","extras","extraServices"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PayFTemplDTO implements Serializable {

    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("account")
    private String account;

    @JsonProperty("agentTransactionId")
    private BigInteger agentTransactionId;

    @JsonProperty("idClient")
    private BigInteger idClient;

    @JsonProperty("agentTransactionDate")
    private String agentTransactionDate;

    @JsonProperty("amountTo")
    private String amountTo;

    @JsonProperty("amountFrom")
    private String amountFrom;

    @JsonProperty("extras")
    private Map<String, String> extras = Collections.emptyMap();

    @JsonProperty("extraServices")
    private List<ExtraSubservices> extraServices = Collections.emptyList();
}
