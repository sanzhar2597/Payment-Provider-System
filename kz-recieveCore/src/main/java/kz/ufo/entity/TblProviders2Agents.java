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
@Table(name = "TBL_PROVIDERS2AGENTS")
@Entity
public class TblProviders2Agents implements Serializable {
    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "code")
    private String code;

    @Column(name = "id_provider")
    private long idProvider;

    @Column(name = "commission")
    private String commission;

    @Column(name = "active")
    private int active;

    @Column(name = "agent_id")
    private int agentId;

    @Column(name = "priority")
    private int priority;

    @Column(name = "currid")
    private String currId;

    @Column(name = "COMPLEX")
    private int complex;

    @Column(name = "KNP")
    private int knp;

    @Column(name = "REGION_ID")
    private int regId;

    @Column(name = "MIN_SUM")
    private int minSum;

    @Column(name = "MAX_SUM")
    private int maxSum;

    @Column(name = "FEE")
    private int fee;

    @Column(name = "GROUP_ID")
    private int groupId;

    @Column(name = "FIXPRICE")
    private int fixPrice;

}
