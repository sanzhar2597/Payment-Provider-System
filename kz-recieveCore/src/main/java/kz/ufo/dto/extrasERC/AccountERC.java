package kz.ufo.dto.extrasERC;

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
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountERC {
    @JsonProperty("id")
    private long id;

    @JsonProperty("st")
    private String st;

    @JsonProperty("address")
    private String address;

    @JsonProperty("name")
    private String name;

    @JsonProperty("invoices")
    private InvoicesERC invoices;

    @JsonProperty("params")
    private ParamsERC params;


}
