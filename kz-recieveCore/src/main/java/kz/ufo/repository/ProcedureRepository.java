package kz.ufo.repository;


import kz.ufo.config.DBConfig;
import kz.ufo.dto.CreatePaymentOpmRequest;
import kz.ufo.dto.ProcedureResult;
import kz.ufo.service.EmailServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ProcedureRepository {

    @Autowired
    EntityManager entityManager;

    @Autowired
    TblPaymentQiwiBalanceRepository tblPaymentQiwiBalanceRepository;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    DBConfig dbConfig;

    @SneakyThrows
    public ProcedureResult createPaymentOpmToObs(CreatePaymentOpmRequest createPaymentOpmRequest) {
        ProcedureResult procedureResult = new ProcedureResult();
        AtomicInteger obsId = new AtomicInteger();

        try {
            entityManager.unwrap(Session.class).doWork(connection -> {





                CallableStatement callableStatement = connection.prepareCall("declare \n" +
                        "p_dataitems dm.pkg_opm_api.t_data_item_tab@dm.hcb.kz; \n" +
                        "v_terminal_id  NUMBER;\n" +
                        "obsId   number;\n" +
                        "begin \n" +
                        "v_terminal_id:=0;\n" +
                        "   p_dataitems := dm.pkg_opm_api.t_data_item_tab@dm.hcb.kz;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'REFERENCEDOCNUM';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'STRING';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := 'PPM'||"+createPaymentOpmRequest.getIdSeq()+" ;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'DEBITACCID';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'NUMBER';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := ? ;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'DEBITACCCODE';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'STRING';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := ? ;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'SYSTEMNAME';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'STRING';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := ? ;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'DATESPAY';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'STRING';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := ? ;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'TEXT_PURPOSE';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'STRING';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := ? ;\n" +
                        "    p_dataitems.extend;\n" +
                        "    p_dataitems(p_dataitems.last).attr_name := 'KNP';\n" +
                        "    p_dataitems(p_dataitems.last).attr_type := 'STRING';\n" +
                        "    p_dataitems(p_dataitems.last).attr_value := ? ;\n" +

                        "dm.pkg_opm_api.payment@dm.hcb.kz(p_system          => 'PPM',\n" +
                        "                                     p_paymenttype     => ?,\n" +
                        "                                     p_amount          => ?,\n" +
                        "                                     p_currency        => 'KZT',\n" +
                        "                                     p_transactiondate => case when '"+createPaymentOpmRequest.getTemplateCode()+"'='PPM_PAYFORM_MT100' then trunc(SYSDATE-1) else trunc(SYSDATE) end,\n" +
                        "                                     p_idterminal      => v_terminal_id,\n" +
                        "                                     p_dataitems       => p_dataitems,\n" +
                        "                                     p_obs_doc_id      => ?);\n" +
                        "                                     \n" +
                        "       dbms_output.put_line(obsId);                              \n" +
                        "end ;");

                callableStatement.setInt(1, createPaymentOpmRequest.getIbanId());
                callableStatement.setString(2,createPaymentOpmRequest.getIban());
                callableStatement.setString(3,createPaymentOpmRequest.getSystemName());
                callableStatement.setString(4,createPaymentOpmRequest.getDatePay());
                callableStatement.setString(5,createPaymentOpmRequest.getTextPurpose());
                callableStatement.setString(6,createPaymentOpmRequest.getKnp());
                callableStatement.setString(7,createPaymentOpmRequest.getTemplateCode());
                callableStatement.setDouble(8, createPaymentOpmRequest.getAmount());
                callableStatement.registerOutParameter(9, Types.VARCHAR);

                callableStatement.execute();

                obsId.set(callableStatement.getInt(9));

            });

            procedureResult.setDocId(obsId.get());
            procedureResult.setIban(createPaymentOpmRequest.getIban());
        }catch (Exception e){
            //log.error("Error in CreatePaymentToObs ",e.getCause());
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in CreatePaymentToObs DB:"+dbConfig.getDbName()
                            ," IBAN: "+createPaymentOpmRequest.getIban()+
                            "\n TEMPLATE: "+createPaymentOpmRequest.getTemplateCode()+
                           "\n SystemName : "+createPaymentOpmRequest.getSystemName()+
                            "\n\n Причина Ошибки :\n\n"+e.getCause());
        }

        return procedureResult ;
    }


}