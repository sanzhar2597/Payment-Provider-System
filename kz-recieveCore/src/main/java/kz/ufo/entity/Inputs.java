package kz.ufo.entity;

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
@JsonPropertyOrder({"name","required","title","regexp"})
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Inputs implements Serializable {
    @JsonProperty("name")
    private String name;

    @JsonProperty("required")
    private Boolean required;

    @JsonProperty("title")
    private String title;

    @JsonProperty("regexp")
    private String regexp;

}
