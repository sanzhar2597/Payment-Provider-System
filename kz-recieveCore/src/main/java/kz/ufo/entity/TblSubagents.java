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
@Table(name = "TBL_SUBAGENTS")
@Entity
public class TblSubagents implements Serializable {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "code")
    private String code;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "iban")
    private String iban;

    @Column(name = "min_balance")
    private int minBalance;


    @Column(name = "iban_id")
    private int idIban;

    @Column(name = "id_agent")
    private int idAgent;
}
