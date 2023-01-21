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

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"invoiceId","formedDate","expireDate","services"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Invoice   {
    @JsonProperty("invoiceId")
    private String invoiceId;

    @JsonProperty("formedDate")
    private String formedDate;

    @JsonProperty("expireDate")
    private String expireDate;

    @JsonProperty("services")
    private Services services;
}
