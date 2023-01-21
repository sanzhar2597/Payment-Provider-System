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
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"serviceId","type", "name", "group","minSum","fixedPayment","country"})
@Accessors(chain = true)
public class ServicesDTO implements Serializable {
    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("type")
    private int type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("group")
    private String group;

    @JsonProperty("minSum")
    private int minSum;

    @JsonProperty("fixedPayment")
    private Boolean fixedPayment;

    @JsonProperty("country")
    private String country;
}
