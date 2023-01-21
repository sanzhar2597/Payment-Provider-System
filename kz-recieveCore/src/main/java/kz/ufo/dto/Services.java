package kz.ufo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.experimental.Accessors;


import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"serviceId","type", "name","group", "minSum","fixedPayment", "country","inputs", "displays"})
@Accessors(chain = true)
public class Services {

    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("type")
    private int type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("group")
    private String group;

    @JsonProperty("minSum")
    private double minSum;

    @JsonProperty("fixedPayment")
    private boolean fixedPayment;

    @JsonProperty("country")
    private String country;

    @JsonProperty("inputs")
    private List<InputsPayform> inputs;


    @JsonProperty("displays")
    private List<DisplaysPayform> displays;
}
