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
@Table(name = "TBL_PROVIDER_FIXPRICE2AGENTS")
@Entity
public class TblProviderFixPrice2Agents implements Serializable {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "ID_PROVIDER_PARTNER")
    private String idProviderPartner;

    @Column(name = "NAME")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "agent_id")
    private int agentId;
}
