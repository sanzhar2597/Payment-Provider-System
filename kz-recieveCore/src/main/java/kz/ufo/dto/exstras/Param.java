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
@JsonPropertyOrder({"parId","parName","parValue"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Param  {

    @JsonProperty("parId")
    private long parId;

    @JsonProperty("parName")
    private String parName;

    @JsonProperty("parValue")
    private Double parValue;

}
