package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_QIWI_PROVIDER_PARAM")
@Entity
public class TblQiwiProviderParam {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id4")
    @SequenceGenerator(name = "id4", sequenceName = "SEQ_TBL_QIWI_PROVIDER_PARAM", allocationSize = 1)
    private long id;

    @Column(name = "idProvider")
    private long idProvider;

    @Column(name = "name")
    private String name;

    @Column(name = "header")
    private String header;
}
