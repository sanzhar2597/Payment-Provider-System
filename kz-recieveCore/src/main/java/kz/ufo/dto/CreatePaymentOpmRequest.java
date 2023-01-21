package kz.ufo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreatePaymentOpmRequest implements Serializable {

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("ibanId")
    private int ibanId;

    @JsonProperty("templateCode")
    private String templateCode;

    @JsonProperty("systemName")
    private String systemName;

    @JsonProperty("idSeq")
    private Integer idSeq;

    @JsonProperty("datePay")
    private String datePay;

    @JsonProperty("textPurpose")
    private String textPurpose;

    @JsonProperty("knp")
    private String knp;


}
