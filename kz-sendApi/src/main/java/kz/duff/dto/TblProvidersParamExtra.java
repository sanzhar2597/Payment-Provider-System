package kz.duff.dto;

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
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class TblProvidersParamExtra implements Serializable {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("idProvider")
    private Long idProvider;

    @JsonProperty("name")
    private String name;

    @JsonProperty("header")
    private String header;
}
