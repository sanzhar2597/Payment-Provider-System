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
@Table(name = "TBL_RESULT_CODE2AGENTS")
@Entity
public class TblResultCode2Agents implements Serializable {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "id_result")
    private int idResult;

    @Column(name = "code")
    private int code;

    @Column(name = "fatal")
    private int fatal;

    @Column(name = "agent_id")
    private int agentId;

}
