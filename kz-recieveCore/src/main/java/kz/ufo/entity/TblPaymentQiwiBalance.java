package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_PAYMENT_QIWI_BALANCE")
@Entity
public class TblPaymentQiwiBalance implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idBalPay")
    @SequenceGenerator(name = "idBalPay", sequenceName = "SEQ_TBL_PAYMENT_BALANCETOAGREGATOR", allocationSize = 1)
    private Integer id;

    @Column(name = "balance")
    private double balance;

    @Column(name = "PAYMENTAMOUNT")
    private BigDecimal payAmount;

    @Column(name = "DATETIME")
    private Date dateTime;

    @Column(name = "DOCUMENT_ID")
    private long docId;

    @Column(name = "IBAN")
    private String iban;

    @Column(name = "id_agent")
    private int idAgent;

    @Column(name = "systemName")
    private String systemName;


}
