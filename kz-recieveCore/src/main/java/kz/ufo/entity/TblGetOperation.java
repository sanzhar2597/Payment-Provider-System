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
import java.math.BigInteger;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_GET_PROVIDER_INFO")
@Entity
public class TblGetOperation  implements Serializable {
    @Id
    @Column(name = "ID_TRANSACTION")
    private BigInteger transactionId;

    @Column(name = "AMOUNT")
    private String amount;

    @Column(name = "ACCOUNT_OPER")
    private String account;

    @Column(name = "ID_AGENT")
    private int idAgent;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "TRANSACTION_DATE")
    private Date transactionDate;

    @Column(name = "ID_USER")
    private int idUser;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "ID_RESULT")
    private int idResult;

    @Column(name = "RES_MESS")
    private String resMessage;

    @Column(name = "ID_SESSION")
    private BigInteger idSession;
}
