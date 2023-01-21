package kz.ufo.dto.extrasERC;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kz.ufo.dto.exstras.Tariff;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ServiceERC {
    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("IsCounterService")
    private String isCounterService;

    @JsonProperty("measure")
    private String measure;

    @JsonProperty("tariff")
    private String tariff ;

    @JsonProperty("debtInfo")
    private String debtInfo;

    @JsonProperty("fixSum")
    private String fixSum;

    @JsonProperty("prevCount")
    private String prevCount;

    @JsonProperty("lastCount")
    private String lastCount;


}
