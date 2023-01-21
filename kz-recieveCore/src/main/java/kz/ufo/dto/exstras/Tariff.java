package kz.ufo.dto.exstras;

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
@JsonPropertyOrder({"minTariffValue","minTariffThreshold","maxTariffValue","middleTariffValue","middleTariffThreshold"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Tariff  {
    @JsonProperty("minTariffValue")
    private String minTariffValue = new String();

    @JsonProperty("minTariffThreshold")
    private String minTariffThreshold = new String();

    @JsonProperty("maxTariffValue")
    private String maxTariffValue = new String();

    @JsonProperty("middleTariffValue")
    private String middleTariffValue = new String();

    @JsonProperty("middleTariffThreshold")
    private String middleTariffThreshold = new String();
}
