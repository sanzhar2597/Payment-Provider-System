package kz.duff.dto;

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
@JsonPropertyOrder({"id","idProvider", "name","header"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TblProvidersParamDTO implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("idProvider")
    private Long idProvider;

    @JsonProperty("name")
    private String name;

    @JsonProperty("header")
    private String header;

    @JsonProperty("dispType")
    private String dispType;
}
