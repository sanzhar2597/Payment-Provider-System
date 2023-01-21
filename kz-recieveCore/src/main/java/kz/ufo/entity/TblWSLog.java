package kz.ufo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TBL_WS_LOG")
@Entity
public class TblWSLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id6")
    @SequenceGenerator(name = "id6", sequenceName = "SEQ_TBL_QIWI_WS_LOG", allocationSize = 1)
    private long id;

    @Column(name = "user_name")
    private String user_name;

    @Column(name = "req_time")
    private Date req_time;

    @Column(name = "resp_time")
    private Date resp_time;

    @Column(name = "error_id")
    private int error_id;

    @Column(name = "request")
    @Lob
    private String request;

    @Column(name = "response")
    @Lob
    private String response;

}
