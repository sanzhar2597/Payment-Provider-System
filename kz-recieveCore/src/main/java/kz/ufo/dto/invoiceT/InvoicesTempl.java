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
import java.util.List;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InvoicesTempl implements Serializable {
    @JsonProperty("invoiceId")
    private String invoiceId;

    @JsonProperty("formedDate")
    private String formedDate;

    @JsonProperty("expireDate")
    private String expireDate;

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("clientAddress")
    private String clientAddress;

    @JsonProperty("services")
    private List<ServicesTempl> services;
}
