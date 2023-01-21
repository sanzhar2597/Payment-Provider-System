package kz.ufo.dto.invoiceT;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kz.ufo.dto.invoiceT.DataTempl;
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
public class ServicesTempl implements Serializable {

    @JsonProperty("subServiceId")
    private String subServiceId;

    @JsonProperty("subServiceName")
    private String subServiceName;

    @JsonProperty("data")
    private DataTempl data;
}
