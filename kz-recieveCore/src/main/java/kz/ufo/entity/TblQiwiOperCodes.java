package kz.ufo.entity;

import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_QIWI_OPERCODES")
@Entity
public class TblQiwiOperCodes {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "pathLogo")
    private String pathLogo;

    @Column(name = "crc")
    private String crc;

    @Column(name = "currId")
    private String currId;


}
