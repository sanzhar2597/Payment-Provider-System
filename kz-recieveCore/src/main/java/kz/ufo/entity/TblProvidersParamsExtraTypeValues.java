package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_PROVIDERS_PARAMS_extratype_Values")
@Entity
public class TblProvidersParamsExtraTypeValues {
    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "id_provider")
    private long idProvider;

    @Column(name = "name")
    private String name;

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;
}
