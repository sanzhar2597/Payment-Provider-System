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
@Table(name = "TBL_OPERATIONS_LOG")
@Entity
public class TblOperationsLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idLog")
    @SequenceGenerator(name = "idLog", sequenceName = "SEQ_TBL_OPERATIOS_LOG", allocationSize = 1)
    private long id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "OPERATION_NAME")
    private String operationName;

    @Column(name = "ERR_ID")
    private int errId;

    @Column(name = "ERR_MSG")
    private String errMsg;

    @Column(name = "ID_TRANSACTION")
    private BigInteger idTransaction;

    @Column(name = "ID_SESSION")
    private BigInteger idSession;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "TRANSACTION_DATE")
    private Date transactionDate;
}
