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

import java.util.Collections;
import java.util.Optional;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"serviceId","serviceName","isCounterService","measure","tKoef","lossesCount","debtInfo","fixSum","prevCount","prevCountDate","lastCount","lastCountDate","tariff"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Service  {
    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("isCounterService")
    private String isCounterService;

    @JsonProperty("measure")
    private String measure;

    @JsonProperty("tKoef")
    private String tKoef;

    @JsonProperty("lossesCount")
    private String lossesCount;

    @JsonProperty("debtInfo")
    private String debtInfo;

    @JsonProperty("fixSum")
    private String fixSum;

    @JsonProperty("prevCount")
    private String prevCount;

    @JsonProperty("prevCountDate")
    private String prevCountDate;

    @JsonProperty("lastCount")
    private String lastCount;

    @JsonProperty("lastCountDate")
    private String lastCountDate;

    @JsonProperty("tariff")
    private Tariff tariff ;
}
