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
@Table(name = "TBL_PAYFORM_PROVIDER_EXTRA")
@Entity
public class TblPayfromProviderExstra implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recId1")
    @SequenceGenerator(name = "recId1", sequenceName = "SEQ_TBL_PAYFORM_PROVIDER_EXSTRA", allocationSize = 1)
    private long recId;

    @Column(name = "idProvider")
    private String idProvider;

    @Column(name = "dispName")
    private String dispName;

    @Column(name = "header")
    private String header;
}
