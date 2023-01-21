package kz.ufo.service;

import kz.ufo.entity.*;
import kz.ufo.repository.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;

@Service
@Slf4j
public class LogMethods {

    @Autowired
    TblWSLogRepository tblWSLogRepository;


    @Autowired
    TblOperationsLogRepository tblOperationsLogRepository;

    @Autowired
    TblGetOperationRepository tblGetOperationRepository;

    @Autowired
    TblCheckPayOperationsRepository tblCheckPayOperationsRepository;

    void wsLog(int error_id, Date req_time, Date resp_time, String user_name, String request, String response) {
        TblWSLog tblQiwiWSLog = new TblWSLog();
        tblQiwiWSLog.setError_id(error_id);
        tblQiwiWSLog.setReq_time(req_time);
        tblQiwiWSLog.setResp_time(resp_time);
        tblQiwiWSLog.setUser_name(user_name);
        tblQiwiWSLog.setRequest(request);
        tblQiwiWSLog.setResponse(response);

        try {
            tblWSLogRepository.save(tblQiwiWSLog);
            log.info("Успешно сохранен WS Log ");
        }catch (Exception e){
            log.error("Ошибка при сохранении в WS Log",e);
        }

    }

    void payOperationLog(String userName, String operationName, int errId, String errMsg, BigInteger idTransaction, BigInteger idSession,
                         int status, Date transactionDate ){
        TblOperationsLog tblOperationsLog = new TblOperationsLog();
        tblOperationsLog.setOperationName(operationName);
        tblOperationsLog.setErrId(errId);
        tblOperationsLog.setErrMsg(errMsg);
        tblOperationsLog.setIdTransaction(idTransaction);
        tblOperationsLog.setIdSession(idSession);
        tblOperationsLog.setUserName(userName);
        tblOperationsLog.setStatus(status);
        tblOperationsLog.setTransactionDate(transactionDate);

        try {
            tblOperationsLogRepository.save(tblOperationsLog);
            log.info("Успешно сохранен Tbl_Operations_LOG id: " +idTransaction+" operationName: "+operationName);
        }catch (Exception e){
            log.error("Ошибка при сохранении в Tbl_Operations_LOG",e);
        }
    }

    @SneakyThrows
    void get_operation(BigInteger idtransaction, String amount,  String accountOper,
                       int idAgent, String currency, int status, Date transactionDate,  String userName,
                       int idResult, String resMessage, BigInteger idSession){

        TblGetOperation tblGetOperation = new TblGetOperation();
        tblGetOperation.setTransactionId(idtransaction);
        tblGetOperation.setAmount(amount);
        tblGetOperation.setAccount(accountOper);
        tblGetOperation.setIdAgent(idAgent);
        tblGetOperation.setCurrency(currency);
        tblGetOperation.setStatus(status);
        tblGetOperation.setTransactionDate(transactionDate);
        tblGetOperation.setUserName(userName);
        tblGetOperation.setIdResult(idResult);
        tblGetOperation.setResMessage(resMessage);
        tblGetOperation.setIdSession(idSession);
        try {
            tblGetOperationRepository.save(tblGetOperation);
            log.info("Успешно сохранен TBL_GET_PROVIDER_INFO  id: "+idtransaction +"  account: "+accountOper);
        }
        catch (SQLGrammarException e) {
            log.info("CLASS : " + e.getClass());
            throw e.getSQLException();
        }
        catch (Exception e){
            log.error("Ошибка при сохранении в TBL_GET_PROVIDER_INFO",e);
        }
    }

    void  checkPay_operation(BigInteger idtransaction, String amount,String method, BigInteger idClient,String systemName, String account,
                       int  idAgent, String currency, int status, Date transactionDate, String userName,
                       int idResult, String resMessage, BigInteger idSession,long idService,String idServicePartner, int count, String extras,
                       int sended,String extraServices, String realTransaction){



        TblCheckPayOperations tblCheckPayOperations = new TblCheckPayOperations();
        tblCheckPayOperations.setTransactionId(idtransaction);
        tblCheckPayOperations.setAmount(amount);
        tblCheckPayOperations.setMethod(method);
        tblCheckPayOperations.setIdClient(idClient);
        tblCheckPayOperations.setSystemName(systemName);
        tblCheckPayOperations.setAccount(account);
        tblCheckPayOperations.setIdAgent(idAgent);
        tblCheckPayOperations.setCurrency(currency);
        tblCheckPayOperations.setStatus(status);
        tblCheckPayOperations.setTransactionDate(transactionDate);
        tblCheckPayOperations.setUserName(userName);
        tblCheckPayOperations.setIdResult(idResult);
        tblCheckPayOperations.setResMessage(resMessage);
        tblCheckPayOperations.setIdSession(idSession);
        tblCheckPayOperations.setIdService(idService);
        tblCheckPayOperations.setIdServicePartner(idServicePartner);
        tblCheckPayOperations.setIdCount(count);
        tblCheckPayOperations.setExtras(extras);
        tblCheckPayOperations.setSended(sended);
        tblCheckPayOperations.setExtraServices(extraServices);
        tblCheckPayOperations.setRealTransac(realTransaction);
        try {
            tblCheckPayOperationsRepository.save(tblCheckPayOperations);
            log.info("Успешно сохранен TBL_CHECK_PAY_OPERATIONS id: "+idtransaction + "  account :"+account);

        }catch (Exception e){
            log.info("Ошибка при сохранении в TBL_CHECK_PAY_OPERATIONS",e);

        }
    }
}
