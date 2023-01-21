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
@Table(name = "TBL_QIWI_RESULT_CODE")
@Entity
public class TblQiwiResultCode implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id5")
    @SequenceGenerator(name = "id5", sequenceName = "SEQ_TBL_QIWI_RESULT_CODE", allocationSize = 1)
    @Column(name = "id")
    private long id;

    @Column(name = "code")
    private long code;

    @Column(name = "text")
    private String text;
}
