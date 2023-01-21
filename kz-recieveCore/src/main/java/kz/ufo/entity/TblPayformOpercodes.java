package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_PAYFORM_OPERCODES")
@Entity
public class TblPayformOpercodes implements Serializable {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "code")
    private String code;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "min_sum")
    private double minSum;

    @Column(name = "groupName")
    private String groupName;

    @Column(name = "fixedPayment")
    private boolean fixedPayment;

    @Column(name = "country")
    private String country;

    @Column(name = "pathLogo")
    private String pathLogo;


}
