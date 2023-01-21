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
@Table(name = "TBL_RESULT_CODE")
@Entity
public class TblResultCode implements Serializable {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "text")
    private String text;

}
