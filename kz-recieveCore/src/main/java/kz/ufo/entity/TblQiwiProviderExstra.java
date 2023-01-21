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
@Table(name = "TBL_QIWI_PROVIDER_EXTRA")
@Entity
public class TblQiwiProviderExstra {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recId2")
    @SequenceGenerator(name = "recId2", sequenceName = "SEQ_TBL_QIWI_PROVIDER_EXSTRA", allocationSize = 1)
    private long recId;

    @Column(name = "idProvider")
    private long idProvider;

    @Column(name = "name")
    private String name;

    @Column(name = "dispName")
    private String dispName;

    @Column(name = "header")
    private String header;
}
