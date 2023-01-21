package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_PAYFORM_PROVIDER_PARAM")
@Entity
public class TblPayformProviderParam implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id3")
    @SequenceGenerator(name = "id3", sequenceName = "SEQ_TBL_PAYFORM_PROVIDER_PARAM", allocationSize = 1)
    private long id;

    @Column(name = "idProvider")
    private String idProvider;

    @Column(name = "name")
    private String name;

    @Column(name = "header")
    private String header;

}
