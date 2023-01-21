package kz.duff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckRequestDTO implements Serializable {
    @JsonProperty("id")
    private BigInteger id;

    @NotNull(message = "Поле обязательно к заполненнию! Прошу заполнить")
    @JsonProperty("referenceNumber")
    private BigInteger referenceNumber;


    @JsonProperty("systemName")
    private String systemName;

    @JsonProperty("currency")
    private String currency;

    @NotNull(message = "Поле обязательно к заполненнию! Прошу заполнить")
    @JsonProperty("amount")
    private String amount;

    @NotNull(message = "Поле обязательно к заполненнию! Прошу заполнить")
    @JsonProperty("service")
    private String service;

    @JsonProperty("agentId")
    private int agentId;

    @NotNull(message = "Поле обязательно к заполненнию! Прошу заполнить")
    @JsonProperty("account")
    private String account;

    @JsonProperty("date")
    private Date date;

    @NotNull(message = "Поле обязательно к заполненнию! Прошу заполнить")
    @JsonProperty("realTransaction")
    private String realTransaction;

    @JsonProperty("extras")
    private Map<String, String> extras = Collections.emptyMap();

    @JsonProperty("extraServices")
    private List<ExtraSubservices> extraServices = Collections.emptyList();
}
