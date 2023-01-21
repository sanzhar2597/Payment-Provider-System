package kz.ufo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import oracle.security.crypto.core.math.BigInt;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"result","resultMessage", "services"})
@Accessors(chain = true)
public class GetServicesPayformDTO  {

    @JsonProperty("result")
    private int result;

    @JsonProperty("resultMessage")
    private String resultMessage;

    @JsonProperty("services")
    private List<Services> services;


}
