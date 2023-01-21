package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_PROVIDERS_BLOB")
@Entity
public class TblProvidersBlob implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blobId")
    @SequenceGenerator(name = "blobId", sequenceName = "SEQ_TBL_PROVIDERS_BLOB", allocationSize = 1)
    private long id;

    @Column(name = "ID_PROVIDER")
    private long idProvider;

    @Column(name = "logo")
    @Lob
    private byte[] logo;

    @Column(name = "LOGO_PATH")
    private String logoPath;

    @Column(name = "ID_PARTNER_PROVIDER")
    private long idPartnerProvider;
}
