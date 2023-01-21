package kz.ufo.dto;

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

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"subServiceId", "lastCount", "amount"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExtraSubservices implements Serializable {

    @JsonProperty("subServiceId")
    private String subServiceId;

    @JsonProperty("lastCount")
    private String lastCount;

    @JsonProperty("amount")
    private Double amount;


}
