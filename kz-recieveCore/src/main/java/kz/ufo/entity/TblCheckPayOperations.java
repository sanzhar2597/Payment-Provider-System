package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_CHECK_PAY_OPERATIONS")
@Entity
public class TblCheckPayOperations  implements Serializable {
    @Id
    @Column(name = "ID_TRANSACTION")
    private BigInteger transactionId;

    @Column(name = "AMOUNT")
    private String amount;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "REFERENCE_NUMBER")
    private BigInteger idClient;

    @Column(name = "ACCOUNT")
    private String account;

    @Column(name = "ID_AGENT")
    private int idAgent;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "TRANSACTION_DATE")
    private Date transactionDate;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "ID_RESULT")
    private int idResult;

    @Column(name = "RES_MESS")
    private String resMessage;

    @Column(name = "ID_SESSION")
    private BigInteger idSession;

    @Column(name = "ID_PROVIDER")
    private long idService;

    @Column(name = "ID_PROVIDER_PARTNER")
    private String idServicePartner;

    @Column(name = "ID_COUNT")
    private int idCount;

    @Column(name = "EXTRAS")
    @Lob
    private String extras;

    @Column(name = "SENDED")
    private int sended;

    @Column(name = "SYSTEMNAME")
    private String systemName;

    @Column(name = "EXTRA_SERVICES")
    @Lob
    private String extraServices;


    @Column(name = "REAL_TRANSACTION")
    private String realTransac;

    @Column(name = "payed_agr")
    private int payedAgr;
}
