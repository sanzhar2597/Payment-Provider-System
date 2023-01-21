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
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class TblProvidersAllDTO implements Serializable {
    @JsonProperty("id")
    private long id;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("commission")
    private String commission;

    @JsonProperty("active")
    private int active;

    @JsonProperty("currid")
    private String currid;

    @JsonProperty("complex")
    private int complex;

    @JsonProperty("knp")
    private int knp;

    @JsonProperty("REGIONID")
    private int regId;

    @JsonProperty("MINSUM")
    private int minSum;

    @JsonProperty("MAXSUM")
    private int maxSum;

    @JsonProperty("FEE")
    private int fee;

    @JsonProperty("groupId")
    private int groupId;

    @JsonProperty("fixPrice")
    private int fixPrice;

    @JsonProperty("mainParams")
    private List<TblProvidersParamDTO> mainParams;

    @JsonProperty("dispParams")
    private List<TblProviderDisplaysDTO> dispParams;

    @JsonProperty("extraKeyValues")
    private List<TblProvidersParamsExtraTypeValuesDTO> extraKeyValues;
}
