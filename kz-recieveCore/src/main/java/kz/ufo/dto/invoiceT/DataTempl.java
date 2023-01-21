package kz.ufo.dto.invoiceT;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DataTempl implements Serializable {

    @JsonProperty("paySum")
    private Double paySum;

    @JsonProperty("tariff")
    private Double tariff;

    @JsonProperty("minSum")
    private Double minSum;

    @JsonProperty("maxSum")
    private Double maxSum;

    @JsonProperty("debtInfo")
    private String debtInfo;

    @JsonProperty("isMeter")
    private Boolean isMeter;

    @JsonProperty("prevCount")
    private String prevCount;

    @JsonProperty("lastCount")
    private String lastCount;

    @JsonProperty("si")
    private String si;

    @JsonProperty("comment")
    private String comment;


}
