package kz.ufo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.ufo.config.DBConfig;
import kz.ufo.config.ProxyConfig;
import kz.ufo.config.RabbitMQConfig;
import kz.ufo.dto.*;
import kz.ufo.entity.*;
import kz.ufo.repository.*;
import kz.ufo.service.opm.*;
import kz.ufo.service.soap.SoapRequest;
import kz.ufo.service.soap.SoapResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
@Slf4j
public class CoreLogicMethodsImpl implements CoreLogicMethods {
    @Autowired
    QiwiSoapServiceImpl qiwiSoapService;

    @Autowired
    PayFServiceImpl payFService;

    @Autowired
    TblProviders2AgentsRepository tblProviders2AgentsRepository;

    @Autowired
    TblGetOperationRepository tblGetOperationRepository;

    @Autowired
    TblCheckPayOperationsRepository tblCheckPayOperationsRepository;

    @Autowired
    LogMethods logMethods;

    @Autowired
    ProxyConfig proxyConfig;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    TblOperationsLogRepository tblOperationsLogRepository;

    @Autowired
    TblQiwiOperCodesRepository tblQiwiOperCodesRepository;

    @Autowired
    TblPayformOpercodesRepository tblPayformOpercodesRepository;

    @Autowired
    TblPayformProviderParamRepository tblPayformProviderParamRepository;

    @Autowired
    TblProviderDisplaysRepository tblProviderDisplaysRepository;

    @Autowired
    TblResultCode2AgentsRepository tblResultCode2AgentsRepository;

    @Autowired
    RabbitMQConfig rabbitMQConfig;

    @Autowired
    TblSubagentsRepository tblSubagentsRepository;

  //  @Autowired
  //  ProcedureRepository procedureRepository;

    @Autowired
    TblPaymentQiwiBalanceRepository tblPaymentQiwiBalanceRepository;

    @Autowired
    EmailServiceImpl emailService;
    @Autowired
    DBConfig dbConfig;

   //@Autowired
   // WebServiceTemplate webServiceTemplate;

    @Autowired
    SoapRestServ soapRestServ;

    @Override
    @Transactional
    @SneakyThrows
    public CheckServDTO getProvidersInfo(PaymentDTO paymentDTO) {
        CheckServDTO checkServDTO = new CheckServDTO();
        try {

            List<TblProviders2Agents> listById = tblProviders2AgentsRepository.findListByIdProviderAndActive(Long.parseLong(paymentDTO.getService()),1);

            if(listById.size()==0){
                throw new RuntimeException(" Provider Not Active !!!");
            } else {

                TblProviders2Agents tblProviders2Agents = listById
                        .stream()
                        .min(Comparator
                                .comparingInt(TblProviders2Agents::getFee)
                                .reversed()
                                .thenComparingDouble((TblProviders2Agents x) -> Double.parseDouble(x.getCommission()))
                                .thenComparingInt(TblProviders2Agents::getPriority))
                        .orElse(null);

                if (tblProviders2Agents == null) {
                    throw new RuntimeException("No provider found for service id: " + paymentDTO.getService());
                }

                    paymentDTO.setDate(new Date());
                    paymentDTO.setAmount("100.00");
                   // paymentDTO.setSystemName("UFO");
                    paymentDTO.setCurrency(tblProviders2Agents.getCurrId());
                    paymentDTO.setId(BigInteger.valueOf(Long.parseLong(rabbitMQConfig.getSprValueByCode("SEQ_COEF")+tblGetOperationRepository.getSeqId())));
                    int agentId = tblProviders2Agents.getAgentId();
                    paymentDTO.setService(tblProviders2Agents.getCode());

                    if (agentId==1) {
                        if (paymentDTO.getExtras().values().stream().anyMatch(x -> x != null)) {

                            checkServDTO = qiwiSoapService.checkQiwiExtrasPayments(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkQiwiExtrasPayments", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());
                        } else {
                            checkServDTO = qiwiSoapService.checkQiwiPayments(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkQiwiPayments", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());
                        }

                        logMethods.get_operation(paymentDTO.getId(), paymentDTO.getAmount(), paymentDTO.getAccount(), agentId,
                                paymentDTO.getCurrency(), 1, paymentDTO.getDate(),  System.getProperty("user.name"),
                                checkServDTO.getResult(), checkServDTO.getResultMessage(), checkServDTO.getTransactionId());

                        checkServDTO.setAgentId(agentId);

                    } else if (agentId == 2) {
                        PayFTemplDTO payFTemplDTO = new PayFTemplDTO();
                        payFTemplDTO.setServiceId(paymentDTO.getService());
                        payFTemplDTO.setAccount(paymentDTO.getAccount());
                        payFTemplDTO.setAgentTransactionId(paymentDTO.getId());
                        payFTemplDTO.setAmountFrom(paymentDTO.getAmount());
                        payFTemplDTO.setAmountTo(paymentDTO.getAmount());
                        payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd").format(paymentDTO.getDate()));
                        if (paymentDTO.getExtras().values().stream().anyMatch(x -> x != null)) {
                            payFTemplDTO.setExtras(paymentDTO.getExtras());
                            checkServDTO = payFService.checkService(payFTemplDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkServiceExtras", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());
                        } else {
                            checkServDTO = payFService.checkService(payFTemplDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkService", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());
                        }


                        logMethods.get_operation(paymentDTO.getId(), paymentDTO.getAmount(), paymentDTO.getAccount(), agentId,
                                paymentDTO.getCurrency(), 1, paymentDTO.getDate(),  System.getProperty("user.name"),
                                checkServDTO.getResult(), checkServDTO.getResultMessage(), checkServDTO.getTransactionId());

                        checkServDTO.setAgentId(agentId);
                    }

            }

            return checkServDTO;

        } catch (Exception e) {
            log.error("getProvidersInfo method Error ", e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in getProvidersInfo method  DB: "+dbConfig.getDbName()
                    ,"Service Id:"+paymentDTO.getService()+"\n"+
                          "Account : "+paymentDTO.getAccount()+"\n"+
                          "SystemName : "+paymentDTO.getSystemName()+
                            "\n Причина Ошибки :\n\n"+"\n"+e);
            return  null;
        }

    }


    @Override
    @Transactional
    @SneakyThrows
    public CheckServDTO check(PaymentDTO paymentDTO)    {
        CheckServDTO checkServDTO  = new CheckServDTO();
        try {

            List<TblProviders2Agents> listById = tblProviders2AgentsRepository.findListByIdProviderAndActive(Long.parseLong(paymentDTO.getService()),1);

            if(listById.size()==0){
                throw new RuntimeException(" Provider Not Active !!!");
            } else {

                TblProviders2Agents tblProviders2Agents = listById
                        .stream()
                        .min(Comparator
                                .comparingInt(TblProviders2Agents::getFee)
                                .reversed()
                                .thenComparingDouble((TblProviders2Agents x) -> Double.parseDouble(x.getCommission()))
                                .thenComparingInt(TblProviders2Agents::getPriority))
                        .orElse(null);

                if (tblProviders2Agents == null) {
                    throw new RuntimeException("No provider found for service id: " + paymentDTO.getService());
                }
                    int agentId = tblProviders2Agents.getAgentId();
                    paymentDTO.setService(tblProviders2Agents.getCode());
                    paymentDTO.setDate(new Date());
                    paymentDTO.setCurrency(tblProviders2Agents.getCurrId());
                    paymentDTO.setId(BigInteger.valueOf(Long.parseLong(rabbitMQConfig.getSprValueByCode("SEQ_COEF")+tblGetOperationRepository.getSeqId()))); //for test suffix 2 for prod 1

                    if (agentId ==1) {
                        if (paymentDTO.getExtras().values().stream().anyMatch(x -> x != null)) {
                            // paymentDTO.setDate(null);
                            checkServDTO = qiwiSoapService.checkQiwiExtrasPayments(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkQiwiExtrasPayments", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());


                        } else {
                            checkServDTO = qiwiSoapService.checkQiwiPayments(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkQiwiPayments", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());

                        }

                        // используется, чтобы знать Чек с какого агента было
                        checkServDTO.setAgentId(agentId);
                        checkServDTO.setReferenceNumber(paymentDTO.getReferenceNumber());



                        if(checkServDTO.getResult()==0){
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "check", paymentDTO.getReferenceNumber(), paymentDTO.getSystemName(), paymentDTO.getAccount(), agentId,
                                    paymentDTO.getCurrency(), 1, paymentDTO.getDate(), System.getProperty("user.name"),
                                    checkServDTO.getResult(), checkServDTO.getResultMessage(), checkServDTO.getTransactionId(), tblProviders2Agents.getIdProvider(), paymentDTO.getService(),
                                    0, paymentDTO.getExtras().isEmpty() ? null : paymentDTO.getExtras().toString(), 0,paymentDTO.getExtraServices().isEmpty() ? null : paymentDTO.getExtraServices().toString(),paymentDTO.getRealTransaction());
                        }
                        else {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "check", paymentDTO.getReferenceNumber(), paymentDTO.getSystemName(), paymentDTO.getAccount(), agentId,
                                    paymentDTO.getCurrency(), -1, paymentDTO.getDate(),  System.getProperty("user.name"),
                                    checkServDTO.getResult(), checkServDTO.getResultMessage(), checkServDTO.getTransactionId(), tblProviders2Agents.getIdProvider(), paymentDTO.getService(),
                                    0, paymentDTO.getExtras().isEmpty() ? null : paymentDTO.getExtras().toString(), 0,paymentDTO.getExtraServices().isEmpty() ? null : paymentDTO.getExtraServices().toString(),paymentDTO.getRealTransaction());
                        }

                    } else if (agentId == 2) {
                        PayFTemplDTO payFTemplDTO = new PayFTemplDTO();
                        payFTemplDTO.setServiceId(paymentDTO.getService());
                        payFTemplDTO.setAccount(paymentDTO.getAccount());
                        payFTemplDTO.setAgentTransactionId(paymentDTO.getId());
                        payFTemplDTO.setAmountFrom(paymentDTO.getAmount());
                        payFTemplDTO.setAmountTo(paymentDTO.getAmount());
                        payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd").format(paymentDTO.getDate()));
                        if (paymentDTO.getExtras().values().stream().anyMatch(x -> x != null)) {
                            payFTemplDTO.setExtras(paymentDTO.getExtras());
                            if(paymentDTO.getExtraServices()!= null){
                                payFTemplDTO.setExtraServices(paymentDTO.getExtraServices());
                            }
                            checkServDTO = payFService.checkService(payFTemplDTO);

                            logMethods.payOperationLog(System.getProperty("user.name"), "checkServiceExtras", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());
                        } else {
                            checkServDTO = payFService.checkService(payFTemplDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "checkService", checkServDTO.getResult(), checkServDTO.getResultMessage()
                                    , checkServDTO.getAgentTransactionId(), checkServDTO.getTransactionId(), checkServDTO.getStatus(), new Date());
                        }

                        // используется, чтобы знать Чек с какого агента было
                        checkServDTO.setAgentId(agentId);
                        checkServDTO.setReferenceNumber(paymentDTO.getReferenceNumber());

                        if(checkServDTO.getResult()==0){
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "check", paymentDTO.getReferenceNumber(), paymentDTO.getSystemName(), paymentDTO.getAccount(), agentId,
                                    paymentDTO.getCurrency(), 1, paymentDTO.getDate(),  System.getProperty("user.name"),
                                    checkServDTO.getResult(), checkServDTO.getResultMessage(), checkServDTO.getTransactionId(), tblProviders2Agents.getIdProvider(), paymentDTO.getService(),
                                    0, paymentDTO.getExtras().isEmpty() ? null : paymentDTO.getExtras().toString(), 0,paymentDTO.getExtraServices().isEmpty() ? null : paymentDTO.getExtraServices().toString(),paymentDTO.getRealTransaction());
                        }
                        else {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "check", paymentDTO.getReferenceNumber(), paymentDTO.getSystemName(), paymentDTO.getAccount(), agentId,
                                    paymentDTO.getCurrency(), -1, paymentDTO.getDate(),  System.getProperty("user.name"),
                                    checkServDTO.getResult(), checkServDTO.getResultMessage(), checkServDTO.getTransactionId(), tblProviders2Agents.getIdProvider(), paymentDTO.getService(),
                                    0, paymentDTO.getExtras().isEmpty() ? null : paymentDTO.getExtras().toString(), 0,paymentDTO.getExtraServices().isEmpty() ? null : paymentDTO.getExtraServices().toString(),paymentDTO.getRealTransaction());
                        }
                    }

            }
            return checkServDTO;
        } catch (Exception e) {
            log.error("Check method Error ", e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in Check method  DB: "+dbConfig.getDbName()
                    ,"Service Id:"+paymentDTO.getService()+"\n" +
                            "REFERENCENUMBER :"+paymentDTO.getReferenceNumber()+"\n"+
                            "SYSTEMNAME :"+paymentDTO.getSystemName()+"\n"+
                            "\n Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }


    @Override
    @RabbitListener(queues = "ppm.payment.queue")
    public void pay(PaymentDTO paymentDTO) throws AmqpRejectAndDontRequeueException {

        int count ;

        try {

            PayServDTO payServDTO = new PayServDTO();
            PayFTemplDTO payFTemplDTO = new PayFTemplDTO();
            TblCheckPayOperations tblCheckPayOperations = tblCheckPayOperationsRepository.findByTransactionIdAndMethodAndStatus(paymentDTO.getId(), "check", 1);

            if (tblCheckPayOperations == null) {
                //throw new RuntimeException("Check not found with transaction id : " + paymentDTO.getId());
                throw new AmqpRejectAndDontRequeueException("Check method not found");
            }
            TblProviders2Agents tblProviders2Agents = tblProviders2AgentsRepository.findByCode(tblCheckPayOperations.getIdServicePartner());

            paymentDTO.setService(tblCheckPayOperations.getIdServicePartner());
            paymentDTO.setAccount(tblCheckPayOperations.getAccount());
            paymentDTO.setDate(new Date());
            paymentDTO.setCurrency(tblCheckPayOperations.getCurrency());
            paymentDTO.setAmount(tblCheckPayOperations.getAmount());
            paymentDTO.setId(paymentDTO.getId());
            paymentDTO.setSystemName(tblCheckPayOperations.getSystemName());
            paymentDTO.setRealTransaction(tblCheckPayOperations.getRealTransac());

            if(tblCheckPayOperations.getExtras()!=null) {
                paymentDTO.setExtras(getExtrasLogic(tblCheckPayOperations.getExtras()));
            }
            if(tblCheckPayOperations.getExtraServices()!=null) {
                paymentDTO.setExtraServices(getExtraSubservicesLogic(tblCheckPayOperations.getExtraServices()));
            }
            //Для рассчета пробы проведение Пэй
            count = tblCheckPayOperations.getIdCount();
            if (tblCheckPayOperations.getRealTransac().equals("true")){
            // Пэй если по Киви
            if (tblCheckPayOperations.getIdAgent() == 1) {
                // Если платеж сложный
                if (paymentDTO.getExtras().values().stream().anyMatch(x -> x != null)) {

                    if (tblProviders2Agents.getComplex() == 1) {
                        payServDTO = qiwiSoapService.addQiwiOfflinePayment(paymentDTO);
                        logMethods.payOperationLog(System.getProperty("user.name"), "addQiwiOfflinePayment", payServDTO.getResult(), payServDTO.getResultMessage()
                                , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());
                        count = count+1;
                        if ((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)) {

                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "addQiwiOfflinePayment", tblCheckPayOperations.getIdClient()
                                    , tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                    paymentDTO.getCurrency(), 5, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, paymentDTO.getExtraServices().toString()
                                    ,  paymentDTO.getRealTransaction());
                        } else {

                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                            if (tblResultCode2Agents.getFatal() == 0) {
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "addQiwiOfflinePayment", tblCheckPayOperations.getIdClient(),
                                        tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                        paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                        paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, paymentDTO.getExtraServices().toString()
                                        , paymentDTO.getRealTransaction());
                            } else {

                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "addQiwiOfflinePayment", tblCheckPayOperations.getIdClient(),
                                        tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                        paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                        paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, paymentDTO.getExtraServices().toString()
                                        , paymentDTO.getRealTransaction());
                            }
                        }

                    } else {

                        payServDTO = qiwiSoapService.authQiwiExtrasPayment(paymentDTO);
                        logMethods.payOperationLog(System.getProperty("user.name"), "authQiwiExtrasPayment", payServDTO.getResult(), payServDTO.getResultMessage()
                                , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());
                        count = count+1;
                        if (payServDTO.getResult() == 0 && payServDTO.getStatus()==3) {

                            payServDTO = qiwiSoapService.confirmQiwiPayment(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "confirmQiwiExtraPayment", payServDTO.getResult(), payServDTO.getResultMessage()
                                    , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());

                            if ((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                    || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)) {
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                        tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 5, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                        paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, null
                                        ,  paymentDTO.getRealTransaction());
                            } else {

                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    // Пэйформ payextraService
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                            tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                                            , paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, null
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                            tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                                            , paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, null
                                            , paymentDTO.getRealTransaction());
                                }
                            }

                        } else {
                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                            if (tblResultCode2Agents.getFatal() == 0) {
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiExtrasPayment",
                                        tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                        paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, null
                                        , paymentDTO.getRealTransaction());
                            } else {
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiExtrasPayment",
                                        tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                        paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0, null
                                        , paymentDTO.getRealTransaction());
                            }
                        }

                    }

                    //Несложные платежи по Киви
                } else {

                    payServDTO = qiwiSoapService.authQiwiPayment(paymentDTO);
                    logMethods.payOperationLog(System.getProperty("user.name"), "authQiwiPayment", payServDTO.getResult(), payServDTO.getResultMessage()
                            , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());
                    count = count+1;
                    if (payServDTO.getResult() == 0 && payServDTO.getStatus()==3) {

                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiPayment",
                                tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 5, paymentDTO.getDate(), System.getProperty("user.name"),
                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                                , paymentDTO.getService(), count, null, 0, null
                                , paymentDTO.getRealTransaction());


                        payServDTO = qiwiSoapService.confirmQiwiPayment(paymentDTO);
                        logMethods.payOperationLog(System.getProperty("user.name"), "confirmQiwiPayment", payServDTO.getResult(), payServDTO.getResultMessage()
                                , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());
                        if ((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)) {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                    tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount()
                                    , tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 5, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                                    , paymentDTO.getService(), count, null, 0, null
                                    , paymentDTO.getRealTransaction());

                        } else {
                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                            if (tblResultCode2Agents.getFatal() == 0 ) {
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                        tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                                        , paymentDTO.getService(), count, null, 0, null
                                        , paymentDTO.getRealTransaction());
                            } else {
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                        tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                                        , paymentDTO.getService(), count, null, 0, null
                                        , paymentDTO.getRealTransaction());
                            }
                        }
                    } else {
                        TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                        if (tblResultCode2Agents.getFatal() == 0) {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiPayment",
                                    tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                    tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, null, 0, null
                                    , paymentDTO.getRealTransaction());
                        } else {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiPayment",
                                    tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(),
                                    tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, null, 0, null
                                    , paymentDTO.getRealTransaction());
                        }
                    }
                }

            }
            //Если выбран Пэйформ
            else if (tblCheckPayOperations.getIdAgent() == 2) {
                //Сложный платеж
                if (paymentDTO.getExtras().values().stream().anyMatch(x -> x != null)) {
                    payFTemplDTO.setServiceId(paymentDTO.getService());
                    payFTemplDTO.setAccount(paymentDTO.getAccount());
                    payFTemplDTO.setAgentTransactionId(paymentDTO.getId());
                    payFTemplDTO.setAmountFrom(paymentDTO.getAmount());
                    payFTemplDTO.setAmountTo(paymentDTO.getAmount());
                    payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(paymentDTO.getDate()));
                    payFTemplDTO.setExtras(paymentDTO.getExtras());
                    payFTemplDTO.setExtraServices(paymentDTO.getExtraServices());
                    payServDTO = payFService.payService(payFTemplDTO);
                    logMethods.payOperationLog(System.getProperty("user.name"), "payExtraService", payServDTO.getResult(), payServDTO.getResultMessage()
                            , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());
                    count = count+1;
                    if (payServDTO.getResult() == 0) {

                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService", tblCheckPayOperations.getIdClient(),
                                tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                paymentDTO.getCurrency(), 6, paymentDTO.getDate(), System.getProperty("user.name"),
                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0,
                                tblProviders2Agents.getComplex() == 1 ? paymentDTO.getExtraServices().toString() : ""
                                , paymentDTO.getRealTransaction());

                    } else if (payServDTO.getResult()==14){
                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService", tblCheckPayOperations.getIdClient(),
                                tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                paymentDTO.getCurrency(), 5, paymentDTO.getDate(), System.getProperty("user.name"),
                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0,
                                tblProviders2Agents.getComplex() == 1 ? paymentDTO.getExtraServices().toString() : ""
                                , paymentDTO.getRealTransaction());
                    }
                    else {
                        TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 2);

                        if (tblResultCode2Agents.getFatal() == 0) {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService", tblCheckPayOperations.getIdClient(),
                                    tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                    paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0,
                                    tblProviders2Agents.getComplex() == 1 ? paymentDTO.getExtraServices().toString() : ""
                                    , paymentDTO.getRealTransaction());
                        } else {

                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService", tblCheckPayOperations.getIdClient(),
                                    tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                    paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, paymentDTO.getExtras().toString(), 0,
                                    tblProviders2Agents.getComplex() == 1 ? paymentDTO.getExtraServices().toString() : ""
                                    , paymentDTO.getRealTransaction());
                        }
                    }
                }
                //Несложный платеж
                else {
                    payFTemplDTO.setServiceId(paymentDTO.getService());
                    payFTemplDTO.setAccount(paymentDTO.getAccount());
                    payFTemplDTO.setAgentTransactionId(paymentDTO.getId());
                    payFTemplDTO.setAmountFrom(paymentDTO.getAmount());
                    payFTemplDTO.setAmountTo(paymentDTO.getAmount());
                    payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(paymentDTO.getDate()));

                    payServDTO = payFService.payService(payFTemplDTO);
                    count = count+1;
                    logMethods.payOperationLog(System.getProperty("user.name"), "payService", payServDTO.getResult(), payServDTO.getResultMessage()
                            , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());
                    if (payServDTO.getResult() == 0) {
                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payService", tblCheckPayOperations.getIdClient(),
                                tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                paymentDTO.getCurrency(), 6, paymentDTO.getDate(), System.getProperty("user.name"),
                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                paymentDTO.getService(), count, null, 0, null
                                , paymentDTO.getRealTransaction());

                    } else if (payServDTO.getResult()==14){
                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payService", tblCheckPayOperations.getIdClient(),
                                tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                paymentDTO.getCurrency(), 5, paymentDTO.getDate(), System.getProperty("user.name"),
                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                paymentDTO.getService(), count, null, 0, null
                                , paymentDTO.getRealTransaction());
                    }

                    else {
                        TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 2);

                        if (tblResultCode2Agents.getFatal() == 0) {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payService", tblCheckPayOperations.getIdClient(),
                                    tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                    paymentDTO.getCurrency(), 2, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, null, 0, null
                                    , paymentDTO.getRealTransaction());
                        } else {
                            logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payService", tblCheckPayOperations.getIdClient(),
                                    tblCheckPayOperations.getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.getIdAgent(),
                                    paymentDTO.getCurrency(), -1, paymentDTO.getDate(), System.getProperty("user.name"),
                                    payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService(),
                                    paymentDTO.getService(), count, null, 0, null
                                    , paymentDTO.getRealTransaction());
                        }
                    }
                }

            }
            // Если выдан Партнер ид кроме 1 и 2
            else {
                // throw new RuntimeException("Провайдер не найден!");
                throw new AmqpRejectAndDontRequeueException("Provider not found!");
            }

        } //Если тестовый платеж, то просто логируем
            else {
                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "TestPay",
                        tblCheckPayOperations.getIdClient(), tblCheckPayOperations.getSystemName(), paymentDTO.getAccount()
                        , tblCheckPayOperations.getIdAgent(), paymentDTO.getCurrency(), 6, paymentDTO.getDate(), System.getProperty("user.name"),
                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.getIdService()
                        , paymentDTO.getService(), 1, null, 0, null
                        , paymentDTO.getRealTransaction());
            }

        } catch (Exception e) {
            log.error("Error Pay", e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in Pay method  DB: "+dbConfig.getDbName()
                    ," Id:"+paymentDTO.getId()+"\n" +
                          "SystemName : "+paymentDTO.getSystemName()+
                            "\n Причина Ошибки :\n\n"+"\n"+e);
        }

    }


    public void checkAndUpdateStatus(){
            // IB Transactions check status and update
            try {
                List<TblCheckPayOperations> tblCheckPayOperationsIB = tblCheckPayOperationsRepository.findbyStatusMethodDateIB();

                if(tblCheckPayOperationsIB.size()>0) {
                    PaymentDTO paymentDTO = new PaymentDTO();
                    paymentDTO.setSystemName("IB");
                    List<GetQiwiPaymentStatusDTO>  status = qiwiSoapService.getQiwiPaymentStatus(tblCheckPayOperationsIB,paymentDTO);

                    for (int i = 0; i < tblCheckPayOperationsIB.size(); i++) {

                        if(status.get(i).getResult() ==0 && status.get(i).getStatus() == 2) {
                                tblCheckPayOperationsIB.get(i).setStatus(6);
                                tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                            } else if (status.get(i).getResult() ==0 && status.get(i).getStatus() == 0) {
                                tblCheckPayOperationsIB.get(i).setStatus(2);
                                tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                            }
                            else {
                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(status.get(i).getResult(), 1);
                          if( status.get(i).getStatus() != 1) {
                              if (tblResultCode2Agents.getFatal() == 0) {
                                  tblCheckPayOperationsIB.get(i).setStatus(2);
                                  tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                                  tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                                  tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                              } else {
                                  tblCheckPayOperationsIB.get(i).setStatus(-1);
                                  tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                                  tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                                  tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                              }
                          }
                        }
                    }
                }
            } catch (Exception e){
                log.error("Error in IB transactions Update status",e);
                emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in IB transactions Update status  DB: "+dbConfig.getDbName()
                        , "\n Причина Ошибки :\n\n"+"\n"+e);
            }

            // TNT Transactions check status and update
            try {
                List<TblCheckPayOperations> tblCheckPayOperationsTNT = tblCheckPayOperationsRepository.findbyStatusMethodDateTNT();

                if(tblCheckPayOperationsTNT.size()>0) {
                    PaymentDTO paymentDTO = new PaymentDTO();
                    paymentDTO.setSystemName("TNT");
                    List<GetQiwiPaymentStatusDTO>  status = qiwiSoapService.getQiwiPaymentStatus(tblCheckPayOperationsTNT,paymentDTO);
                    for (int i = 0; i < tblCheckPayOperationsTNT.size(); i++) {

                        if(status.get(i).getResult()==0) {
                            if (status.get(i).getStatus() == 2) {
                                tblCheckPayOperationsTNT.get(i).setStatus(6);
                                tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                            } else if (status.get(i).getStatus() == 0) {
                                tblCheckPayOperationsTNT.get(i).setStatus(2);
                                tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                            }
                        } else {
                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(status.get(i).getResult(), 1);

                            if(tblResultCode2Agents.getFatal()==0 && tblResultCode2Agents.getIdResult()!=90){
                                tblCheckPayOperationsTNT.get(i).setStatus(2);
                                tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                            } else {
                                if (tblResultCode2Agents.getIdResult() != 90) {
                                    tblCheckPayOperationsTNT.get(i).setStatus(-1);
                                    tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                                    tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                                    tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e){
                log.error("Error in TNT transactions Update status",e);
                emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in TNT transactions Update status  DB: "+dbConfig.getDbName()
                        , "\n Причина Ошибки :\n\n"+"\n"+e);
            }

            // IBCNP Transactions check status and update
            try {
                List<TblCheckPayOperations> tblCheckPayOperationsIBCNP = tblCheckPayOperationsRepository.findbyStatusMethodDateIBCNP();

                if(tblCheckPayOperationsIBCNP.size()>0) {
                    PaymentDTO paymentDTO = new PaymentDTO();
                    paymentDTO.setSystemName("IBCNP");
                    List<GetQiwiPaymentStatusDTO>  status = qiwiSoapService.getQiwiPaymentStatus(tblCheckPayOperationsIBCNP,paymentDTO);
                    for (int i = 0; i < tblCheckPayOperationsIBCNP.size(); i++) {

                        if(status.get(i).getResult()==0) {
                            if (status.get(i).getStatus() == 2) {
                                tblCheckPayOperationsIBCNP.get(i).setStatus(6);
                                tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                            } else if (status.get(i).getStatus() == 0) {
                                tblCheckPayOperationsIBCNP.get(i).setStatus(2);
                                tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                            }
                        } else {
                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(status.get(i).getResult(), 1);

                            if(tblResultCode2Agents.getFatal()==0 &&tblResultCode2Agents.getIdResult()!=90){
                                tblCheckPayOperationsIBCNP.get(i).setStatus(2);
                                tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                            } else {
                                if (tblResultCode2Agents.getIdResult() != 90) {
                                    tblCheckPayOperationsIBCNP.get(i).setStatus(-1);
                                    tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                                    tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                                    tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e){
                log.error("Error in IBCNP transactions Update status",e);
                emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in IBCNP transactions Update status  DB: "+dbConfig.getDbName()
                        , "\n Причина Ошибки :\n\n"+"\n"+e);
            }


    }

    public void checkAndUpdateStatusComplex(){
        // IB Transactions check status and update
        try {
            List<TblCheckPayOperations> tblCheckPayOperationsIB = tblCheckPayOperationsRepository.findbyStatusMethodDateIBComplex();

            if(tblCheckPayOperationsIB.size()>0) {
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setSystemName("IB");
                List<GetQiwiPaymentStatusDTO>  status = qiwiSoapService.getQiwiPaymentStatus(tblCheckPayOperationsIB,paymentDTO);

                for (int i = 0; i < tblCheckPayOperationsIB.size(); i++) {

                    if(status.get(i).getResult() ==0 && status.get(i).getStatus() == 2) {
                        tblCheckPayOperationsIB.get(i).setStatus(6);
                        tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                        tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                        tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                    } else if (status.get(i).getResult() ==0 && status.get(i).getStatus() == 0) {
                        tblCheckPayOperationsIB.get(i).setStatus(2);
                        tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                        tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                        tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                    }
                    else {
                        TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(status.get(i).getResult(), 1);
                        if( status.get(i).getStatus() != 1) {
                            if (tblResultCode2Agents.getFatal() == 0) {
                                tblCheckPayOperationsIB.get(i).setStatus(2);
                                tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                            } else {
                                tblCheckPayOperationsIB.get(i).setStatus(-1);
                                tblCheckPayOperationsIB.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIB.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIB.get(i));
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            log.error("Error in IB transactions Update status Complex",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in IB transactions Update status Complex  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }

        // TNT Transactions check status and update
        try {
            List<TblCheckPayOperations> tblCheckPayOperationsTNT = tblCheckPayOperationsRepository.findbyStatusMethodDateTNTComplex();
            if(tblCheckPayOperationsTNT.size()>0) {
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setSystemName("TNT");
                List<GetQiwiPaymentStatusDTO>  status = qiwiSoapService.getQiwiPaymentStatus(tblCheckPayOperationsTNT,paymentDTO);
                for (int i = 0; i < tblCheckPayOperationsTNT.size(); i++) {

                    if(status.get(i).getResult()==0) {
                        if (status.get(i).getStatus() == 2) {
                            tblCheckPayOperationsTNT.get(i).setStatus(6);
                            tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                            tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                            tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                        } else if (status.get(i).getStatus() == 0) {
                            tblCheckPayOperationsTNT.get(i).setStatus(2);
                            tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                            tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                            tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                        }
                    } else {
                        TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(status.get(i).getResult(), 1);

                        if(tblResultCode2Agents.getFatal()==0 && tblResultCode2Agents.getIdResult()!=90){
                            tblCheckPayOperationsTNT.get(i).setStatus(2);
                            tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                            tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                            tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                        } else {
                            if (tblResultCode2Agents.getIdResult() != 90) {
                                tblCheckPayOperationsTNT.get(i).setStatus(-1);
                                tblCheckPayOperationsTNT.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsTNT.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsTNT.get(i));
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            log.error("Error in TNT transactions Update status Complex",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in TNT transactions Update status Complex  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }

        // IBCNP Transactions check status and update
        try {
            List<TblCheckPayOperations> tblCheckPayOperationsIBCNP = tblCheckPayOperationsRepository.findbyStatusMethodDateIBCNPComplex();

            if(tblCheckPayOperationsIBCNP.size()>0) {
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setSystemName("IBCNP");
                List<GetQiwiPaymentStatusDTO>  status = qiwiSoapService.getQiwiPaymentStatus(tblCheckPayOperationsIBCNP,paymentDTO);
                for (int i = 0; i < tblCheckPayOperationsIBCNP.size(); i++) {

                    if(status.get(i).getResult()==0) {
                        if (status.get(i).getStatus() == 2) {
                            tblCheckPayOperationsIBCNP.get(i).setStatus(6);
                            tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                            tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                            tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                        } else if (status.get(i).getStatus() == 0) {
                            tblCheckPayOperationsIBCNP.get(i).setStatus(2);
                            tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                            tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                            tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                        }
                    } else {
                        TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(status.get(i).getResult(), 1);

                        if(tblResultCode2Agents.getFatal()==0 &&tblResultCode2Agents.getIdResult()!=90){
                            tblCheckPayOperationsIBCNP.get(i).setStatus(2);
                            tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                            tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                            tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                        } else {
                            if (tblResultCode2Agents.getIdResult() != 90) {
                                tblCheckPayOperationsIBCNP.get(i).setStatus(-1);
                                tblCheckPayOperationsIBCNP.get(i).setIdResult(status.get(i).getResult());
                                tblCheckPayOperationsIBCNP.get(i).setResMessage(status.get(i).getResultMessage());
                                tblCheckPayOperationsRepository.save(tblCheckPayOperationsIBCNP.get(i));
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            log.error("Error in IBCNP transactions Update status Complex",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in IBCNP transactions Update status Complex DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }


    }

    @SneakyThrows
    public Message messageParse(GetStatusPayDTO getStatusPayDTO){
        String orderJson = objectMapper.writeValueAsString(getStatusPayDTO);
        Message message = MessageBuilder
                .withBody(orderJson.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        return message;
    }




    public void sendToQueueStatusPay(){
        int status ;
        int count;

        try {
            List<TblCheckPayOperations> tblCheckPayOperations = tblCheckPayOperationsRepository.findByMethodDateSended();

            if(tblCheckPayOperations.size() > 0) {

                for (int i = 0; i < tblCheckPayOperations.size(); i++) {

                    status = tblCheckPayOperations.get(i).getStatus();

                    if( status == 6 || status==-1 ){

                        GetStatusPayDTO getStatusPayDTO = new GetStatusPayDTO();

                        getStatusPayDTO.setIdAgent(tblCheckPayOperations.get(i).getIdAgent());
                        getStatusPayDTO.setStatus(status);
                        getStatusPayDTO.setId(tblCheckPayOperations.get(i).getTransactionId());
                        getStatusPayDTO.setErrCode(tblCheckPayOperations.get(i).getIdResult());

                        try {
                            rabbitTemplate.convertAndSend("ppm.status.exchange","paymentStatus", messageParse(getStatusPayDTO));
                            log.info("Status отправлен в очередь ppm.paymentStatus.queue :  "+messageParse(getStatusPayDTO));
                            tblCheckPayOperations.get(i).setSended(1);
                            tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));
                        }catch (Exception e){
                            log.error("Ошибка при отправке в очередь ppm.paymentStatus.queue",e);
                        }

                    }

                }
            }
        }catch (Exception e){
            log.error("Error in sendToQueueStatusPay" ,e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in sendToQueueStatusPay  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }


    }

    public void sendToQueueStatusPayTNT(){
        int status ;
        int count;

        try {
            List<TblCheckPayOperations> tblCheckPayOperations = tblCheckPayOperationsRepository.findByMethodDateSendedTNT();

            if(tblCheckPayOperations.size() > 0) {

                for (int i = 0; i < tblCheckPayOperations.size(); i++) {

                    status = tblCheckPayOperations.get(i).getStatus();

                    if( status == 6 || status==-1 ){

                        GetStatusPayDTO getStatusPayDTO = new GetStatusPayDTO();

                        getStatusPayDTO.setIdAgent(tblCheckPayOperations.get(i).getIdAgent());
                        getStatusPayDTO.setStatus(status);
                        getStatusPayDTO.setId(tblCheckPayOperations.get(i).getTransactionId());
                        getStatusPayDTO.setErrCode(tblCheckPayOperations.get(i).getIdResult());

                        try {
                            rabbitTemplate.convertAndSend("ppm.statusTNT.exchange","paymentStatusTNT", messageParse(getStatusPayDTO));
                            log.info("Status отправлен в очередь ppm.paymentStatusTNT.queue :  "+messageParse(getStatusPayDTO));
                            tblCheckPayOperations.get(i).setSended(1);
                            tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));
                        }catch (Exception e){
                            log.error("Ошибка при отправке в очередь ppm.paymentStatusTNT.queue",e);
                        }

                    }

                }
            }
        }catch (Exception e){
            log.error("Error in sendToQueueStatusPayTNT" ,e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in sendToQueueStatusPayTNT  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }


    }

    public Map<String, String> getExtrasLogic(String checkExtras){
        Map<String, String> extras = new HashMap<>();

        String extra = checkExtras.replaceAll("[{+}]", "");
        for (String s1 : extra.split(",")) {
            String[] s2 = s1.split("=");
            extras.put(s2[0], s2[1]);

        }
        return  extras;
    }

    public List<ExtraSubservices> getExtraSubservicesLogic(String getSubservice){
        String replaceSymbols = getSubservice.replace("[", "")
                                             .replace("]", "")
                                             .replace("ExtraSubservices(", "")
                                             .replace(")", "");

        List<ExtraSubservices> extraServ = new ArrayList<>();

        List<String> list1 = Arrays.asList(replaceSymbols.split(","));
        ExtraSubservices subservices = null;
        for (int j = 0; j < list1.size(); j++) {
            String[] s2 = list1.get(j).split("=");

            if(j%3 == 0 ||  j == list1.size()){
                subservices = new ExtraSubservices();
                if (subservices != null) {
                    extraServ.add(subservices);
                }
            }

            if (j % 3 == 0) {
                subservices.setSubServiceId(s2[1]);
            } else if (j % 3 == 1) {
                subservices.setLastCount("0");
            } else {
                subservices.setAmount(Double.parseDouble(s2[1]));
            }

        }

        return  extraServ;
    }

    @Transactional
    public void rePay() {
        int count;

        try {

            List<TblCheckPayOperations> tblCheckPayOperations = tblCheckPayOperationsRepository.findErrorPays(Integer.parseInt(rabbitMQConfig.getSprValueByCode("LOOP_REPAY")));


            if (tblCheckPayOperations.size() > 0) {

                for (int i = 0; i < tblCheckPayOperations.size(); i++) {

                    TblProviders2Agents tblProviders2Agents = tblProviders2AgentsRepository.findByCode(tblCheckPayOperations.get(i).getIdServicePartner());


                    List<TblProviders2Agents> listProviders = tblProviders2AgentsRepository.findByIdProviderAndActive(tblCheckPayOperations.get(i).getIdService(),1);

                    if( listProviders.size()>1) {

                        //Если уже пробовали по первому агенту 2 раза меняем ид Агента и имя метода
                      //  if (tblCheckPayOperations.get(i).getIdCount() == 2) {



                            if (tblCheckPayOperations.get(i).getIdAgent() == 1) {
                                if (tblProviders2Agents.getComplex() == 1) {
                                    tblCheckPayOperations.get(i).setMethod("payExtraService");
                                } else {
                                    if (tblCheckPayOperations.get(i).getMethod().equals("authQiwiPayment")
                                            || tblCheckPayOperations.get(i).getMethod().equals("confirmQiwiPayment")) {
                                        tblCheckPayOperations.get(i).setMethod("payService");
                                    } else if (tblCheckPayOperations.get(i).getMethod().equals("authQiwiExtrasPayment")
                                            || tblCheckPayOperations.get(i).getMethod().equals("confirmQiwiExtraPayment")) {
                                        tblCheckPayOperations.get(i).setMethod("payExtraService");
                                    }
                                }
                                TblProviders2Agents providers2AgentsPF = tblProviders2AgentsRepository.findByIdProviderAndActiveAndAgentId(
                                        tblCheckPayOperations.get(i).getIdService(),1,2);
                                tblCheckPayOperations.get(i).setIdServicePartner(providers2AgentsPF.getCode());
                                tblCheckPayOperations.get(i).setIdAgent(2);


                            } else if (tblCheckPayOperations.get(i).getIdAgent() == 2) {

                                if (tblProviders2Agents.getComplex() == 1) {
                                    tblCheckPayOperations.get(i).setMethod("addQiwiOfflinePayment");
                                } else {

                                    if (tblCheckPayOperations.get(i).getMethod().equals("payService")) {
                                        tblCheckPayOperations.get(i).setMethod("authQiwiPayment");
                                    } else if (tblCheckPayOperations.get(i).getMethod().equals("payExtraService")) {
                                        tblCheckPayOperations.get(i).setMethod("authQiwiExtrasPayment");
                                    }
                                }
                                TblProviders2Agents providers2AgentsQiwi = tblProviders2AgentsRepository.findByIdProviderAndActiveAndAgentId(
                                        tblCheckPayOperations.get(i).getIdService(),1,1);
                                tblCheckPayOperations.get(i).setIdServicePartner(providers2AgentsQiwi.getCode());
                                tblCheckPayOperations.get(i).setIdAgent(1);

                            }

                            tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));

                       // }
                    }
                    PaymentDTO paymentDTO = new PaymentDTO();
                    PayServDTO payServDTO ;
                    PayFTemplDTO payFTemplDTO = new PayFTemplDTO();

                    paymentDTO.setService(tblCheckPayOperations.get(i).getIdServicePartner());
                    paymentDTO.setAccount(tblCheckPayOperations.get(i).getAccount());
                    paymentDTO.setDate(new Date());
                    paymentDTO.setCurrency(tblCheckPayOperations.get(i).getCurrency());
                    paymentDTO.setAmount(tblCheckPayOperations.get(i).getAmount());
                    paymentDTO.setId(tblCheckPayOperations.get(i).getTransactionId());
                    paymentDTO.setSystemName(tblCheckPayOperations.get(i).getSystemName());
                    paymentDTO.setRealTransaction(tblCheckPayOperations.get(i).getRealTransac());
                    count = tblCheckPayOperations.get(i).getIdCount();


                    if (tblCheckPayOperations.get(i).getIdAgent() == 1) {

                        // Если упал в авторизации простого платежа
                        if (tblCheckPayOperations.get(i).getMethod().equals("authQiwiPayment")) {
                            count = count+1;
                            payServDTO = qiwiSoapService.authQiwiPayment(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"),"authQiwiPayment",payServDTO.getResult(),payServDTO.getResultMessage()
                                    ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());

                            if (payServDTO.getResult() == 0 && payServDTO.getStatus()==3) {

                                logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"authQiwiPayment",
                                        tblCheckPayOperations.get(i).getIdClient(),paymentDTO.getSystemName(),paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                        payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),tblCheckPayOperations.get(i).getIdService(),
                                        paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());


                                payServDTO = qiwiSoapService.confirmQiwiPayment(paymentDTO);
                                logMethods.payOperationLog(System.getProperty("user.name"),"confirmQiwiPayment",payServDTO.getResult(),payServDTO.getResultMessage()
                                        ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());
                                if ((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                        || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)) {
                                    logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"confirmQiwiPayment",
                                            tblCheckPayOperations.get(i).getIdClient(),paymentDTO.getSystemName(),paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                            payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),
                                            tblCheckPayOperations.get(i).getIdService(),paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());

                                } else {
                                    TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                    if (tblResultCode2Agents.getFatal() == 0) {
                                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                                tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                                tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(),
                                                tblCheckPayOperations.get(i).getIdService(), paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                                , paymentDTO.getRealTransaction());
                                    } else {
                                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                                tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                                tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                                payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(),
                                                tblCheckPayOperations.get(i).getIdService(), paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                                , paymentDTO.getRealTransaction());
                                    }
                                }
                            } else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(),
                                            tblCheckPayOperations.get(i).getIdService(), paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(),
                                            tblCheckPayOperations.get(i).getIdService(), paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }
                        }
                        // Если упал в авторизации сложного платежа
                        else if (tblCheckPayOperations.get(i).getMethod().equals("authQiwiExtrasPayment")) {
                            count = count+1;
                            paymentDTO.setExtras(getExtrasLogic(tblCheckPayOperations.get(i).getExtras()));

                            payServDTO = qiwiSoapService.authQiwiExtrasPayment(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"), "authQiwiExtrasPayment", payServDTO.getResult(), payServDTO.getResultMessage()
                                    , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());

                            if (payServDTO.getResult() == 0 && payServDTO.getStatus()==3) {

                                payServDTO = qiwiSoapService.confirmQiwiPayment(paymentDTO);
                                logMethods.payOperationLog(System.getProperty("user.name"), "confirmQiwiExtraPayment", payServDTO.getResult(), payServDTO.getResultMessage()
                                        , payServDTO.getAgentTransactionId(), payServDTO.getTransactionId(), payServDTO.getStatus(), new Date());

                                if((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                        || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)){
                                    logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"confirmQiwiExtraPayment",
                                            tblCheckPayOperations.get(i).getIdClient(),tblCheckPayOperations.get(i).getSystemName(),paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                            payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),
                                            tblCheckPayOperations.get(i).getIdService(),paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),
                                            0,tblCheckPayOperations.get(i).getExtraServices(),paymentDTO.getRealTransaction());
                                }else {
                                    TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                    if (tblResultCode2Agents.getFatal() == 0) {
                                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                                tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                                tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(),
                                                System.getProperty("user.name"), payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(),
                                                tblCheckPayOperations.get(i).getIdService(), paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(),
                                                0, tblCheckPayOperations.get(i).getExtraServices(), paymentDTO.getRealTransaction());
                                    } else {
                                        logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                                tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                                tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(),
                                                System.getProperty("user.name"), payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(),
                                                tblCheckPayOperations.get(i).getIdService(), paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(),
                                                0, tblCheckPayOperations.get(i).getExtraServices(), paymentDTO.getRealTransaction());
                                    }
                                }

                            } else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiExtrasPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "authQiwiExtrasPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }

                            // Если при подтверждении платежа
                        } else if (tblCheckPayOperations.get(i).getMethod().equals("confirmQiwiPayment")) {
                            count = count+1;
                            payServDTO = qiwiSoapService.confirmQiwiPayment(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"),"confirmQiwiPayment",payServDTO.getResult(),payServDTO.getResultMessage()
                                    ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());
                            if ((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                    || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)) {
                                logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"confirmQiwiPayment",
                                        tblCheckPayOperations.get(i).getIdClient(),paymentDTO.getSystemName(),paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                        payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),tblCheckPayOperations.get(i).getIdService(),
                                        paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());

                            } else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }

                        }
                        else if (tblCheckPayOperations.get(i).getMethod().equals("confirmQiwiExtraPayment")) {
                            count = count+1;
                            payServDTO = qiwiSoapService.confirmQiwiPayment(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"),"confirmQiwiExtraPayment",payServDTO.getResult(),payServDTO.getResultMessage()
                                    ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());
                            if ((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                    || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)) {
                                logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"confirmQiwiExtraPayment",
                                        tblCheckPayOperations.get(i).getIdClient(),paymentDTO.getSystemName(),paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                        payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),tblCheckPayOperations.get(i).getIdService(),
                                        paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());

                            } else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "confirmQiwiExtraPayment",
                                            tblCheckPayOperations.get(i).getIdClient(), paymentDTO.getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }
                            //Если проведение комплексных платежей
                        }
                        else if (tblCheckPayOperations.get(i).getMethod().equals("addQiwiOfflinePayment")) {
                            count = count+1;
                            paymentDTO.setExtras(getExtrasLogic(tblCheckPayOperations.get(i).getExtras()));
                            paymentDTO.setExtraServices(getExtraSubservicesLogic(tblCheckPayOperations.get(i).getExtraServices()));

                            payServDTO = qiwiSoapService.addQiwiOfflinePayment(paymentDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"),"addQiwiOfflinePayment",payServDTO.getResult(),payServDTO.getResultMessage()
                                    ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());

                            if((payServDTO.getResult() == 0 && (payServDTO.getStatus()==1 || payServDTO.getStatus()==2))
                                    || (payServDTO.getResult()!=0 && payServDTO.getStatus()==1)){

                                logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"addQiwiOfflinePayment",
                                        tblCheckPayOperations.get(i).getIdClient(),tblCheckPayOperations.get(i).getSystemName(),paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                        payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),tblCheckPayOperations.get(i).getIdService(),
                                        paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());
                            }
                            else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 1);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "addQiwiOfflinePayment",
                                            tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "addQiwiOfflinePayment",
                                            tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }

                        }

                    } else if (tblCheckPayOperations.get(i).getIdAgent() == 2) {
                        payFTemplDTO.setServiceId(paymentDTO.getService());
                        payFTemplDTO.setAccount(paymentDTO.getAccount());
                        payFTemplDTO.setAgentTransactionId(paymentDTO.getId());
                        payFTemplDTO.setAmountFrom(paymentDTO.getAmount());
                        payFTemplDTO.setAmountTo(paymentDTO.getAmount());
                        payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(paymentDTO.getDate()));
                        if(tblCheckPayOperations.get(i).getMethod().equals("payExtraService")) {
                            count = count+1;
                            payFTemplDTO.setExtras(getExtrasLogic(tblCheckPayOperations.get(i).getExtras()));
                            payFTemplDTO.setExtraServices(getExtraSubservicesLogic(tblCheckPayOperations.get(i).getExtraServices()));

                            payServDTO = payFService.payService(payFTemplDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"),"payExtraService",payServDTO.getResult(),payServDTO.getResultMessage()
                                    ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());
                            if(payServDTO.getResult()==0){

                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService",
                                        tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 6, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                        paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0,
                                        tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());

                            } else if(payServDTO.getResult()==14){
                                logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService",
                                        tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 5, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                        payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                        paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0,
                                        tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());
                            }
                            else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 2);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService",
                                            tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0,
                                            tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payExtraService",
                                            tblCheckPayOperations.get(i).getIdClient(), tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(),
                                            tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0,
                                            tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }

                        } else if (tblCheckPayOperations.get(i).getMethod().equals("payService")){
                            count = count+1;
                            payServDTO = payFService.payService(payFTemplDTO);
                            logMethods.payOperationLog(System.getProperty("user.name"),"payService",payServDTO.getResult(),payServDTO.getResultMessage()
                                    ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());
                            if(payServDTO.getResult()==0){
                                logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"payService",
                                        tblCheckPayOperations.get(i).getIdClient(),tblCheckPayOperations.get(i).getSystemName(),paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),6,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                        payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),
                                        tblCheckPayOperations.get(i).getIdService(),paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());

                            } else if(payServDTO.getResult()==14){
                                logMethods.checkPay_operation(paymentDTO.getId(),paymentDTO.getAmount(),"payService",
                                        tblCheckPayOperations.get(i).getIdClient(),tblCheckPayOperations.get(i).getSystemName(),paymentDTO.getAccount(),
                                        tblCheckPayOperations.get(i).getIdAgent(), paymentDTO.getCurrency(),5,tblCheckPayOperations.get(i).getTransactionDate(),System.getProperty("user.name"),
                                        payServDTO.getResult(),payServDTO.getResultMessage(),payServDTO.getTransactionId(),
                                        tblCheckPayOperations.get(i).getIdService(),paymentDTO.getService(),count,tblCheckPayOperations.get(i).getExtras(),0,tblCheckPayOperations.get(i).getExtraServices()
                                        ,paymentDTO.getRealTransaction());
                            }
                            else {
                                TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 2);

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payService", tblCheckPayOperations.get(i).getIdClient(),
                                            tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.get(i).getIdAgent(),
                                            paymentDTO.getCurrency(), 2, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                } else {
                                    logMethods.checkPay_operation(paymentDTO.getId(), paymentDTO.getAmount(), "payService", tblCheckPayOperations.get(i).getIdClient(),
                                            tblCheckPayOperations.get(i).getSystemName(), paymentDTO.getAccount(), tblCheckPayOperations.get(i).getIdAgent(),
                                            paymentDTO.getCurrency(), -1, tblCheckPayOperations.get(i).getTransactionDate(), System.getProperty("user.name"),
                                            payServDTO.getResult(), payServDTO.getResultMessage(), payServDTO.getTransactionId(), tblCheckPayOperations.get(i).getIdService(),
                                            paymentDTO.getService(), count, tblCheckPayOperations.get(i).getExtras(), 0, tblCheckPayOperations.get(i).getExtraServices()
                                            , paymentDTO.getRealTransaction());
                                }
                            }
                        }


                    }

                    // Если попытки закончились, то обновлять статус на окончательный
                    if (tblCheckPayOperations.get(i).getStatus()==2){
                        if ((tblCheckPayOperations.get(i).getIdCount() / listProviders.size()) % Integer.parseInt(rabbitMQConfig.getSprValueByCode("LOOP_REPAY"))==0){
                                tblCheckPayOperations.get(i).setStatus(-1);
                                tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));
                        }
                    }

                }
            }
        }catch (Exception e){
            log.error("Error in rePay",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in rePay  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }

    }

    @Transactional
    public void inProcessingTransactionsRepay(){

        try {
            List<TblCheckPayOperations> tblCheckPayOperations = tblCheckPayOperationsRepository.findInProcessingTransactions();

            if(tblCheckPayOperations.size()>0){

                for (int i = 0; i <tblCheckPayOperations.size() ; i++) {

                    PayFTemplDTO payFTemplDTO = new PayFTemplDTO();

                    payFTemplDTO.setServiceId(tblCheckPayOperations.get(i).getIdServicePartner());
                    payFTemplDTO.setAccount(tblCheckPayOperations.get(i).getAccount());
                    payFTemplDTO.setAgentTransactionId(tblCheckPayOperations.get(i).getTransactionId());
                    payFTemplDTO.setAmountFrom(tblCheckPayOperations.get(i).getAmount());
                    payFTemplDTO.setAmountTo(tblCheckPayOperations.get(i).getAmount());

                    payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                    PayServDTO payServDTO ;

                    if(tblCheckPayOperations.get(i).getMethod().equals("payService")){
                        payServDTO = payFService.payService(payFTemplDTO);
                        logMethods.payOperationLog(System.getProperty("user.name"),"payService",payServDTO.getResult(),payServDTO.getResultMessage()
                                ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());
                        if(payServDTO.getResult()==0){

                            tblCheckPayOperations.get(i).setIdResult(0);
                            tblCheckPayOperations.get(i).setResMessage(payServDTO.getResultMessage());
                            tblCheckPayOperations.get(i).setStatus(6);

                            tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));

                        }  else{
                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(),2);

                            if(payServDTO.getResult()!=14) {
                                if (tblResultCode2Agents.getFatal() == 0) {

                                    tblCheckPayOperations.get(i).setStatus(2);
                                } else {
                                    tblCheckPayOperations.get(i).setStatus(-1);
                                }

                                tblCheckPayOperations.get(i).setIdResult(payServDTO.getResult());
                                tblCheckPayOperations.get(i).setResMessage(payServDTO.getResultMessage());


                                tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));
                            }
                        }

                    }
                    else if(tblCheckPayOperations.get(i).getMethod().equals("payExtraService")){
                        if(tblCheckPayOperations.get(i).getExtras()!=null){
                            payFTemplDTO.setExtras(getExtrasLogic(tblCheckPayOperations.get(i).getExtras()));
                        }
                      //  payFTemplDTO.setExtraServices(getExtraSubservicesLogic(tblCheckPayOperations.get(i).getExtraServices()));
                        payServDTO = payFService.payService(payFTemplDTO);
                        logMethods.payOperationLog(System.getProperty("user.name"),"payExtraService",payServDTO.getResult(),payServDTO.getResultMessage()
                                ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());

                        if(payServDTO.getResult()==0){
                            tblCheckPayOperations.get(i).setIdResult(0);
                            tblCheckPayOperations.get(i).setResMessage(payServDTO.getResultMessage());
                            tblCheckPayOperations.get(i).setStatus(6);

                            tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));

                        }
                        else {

                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 2);

                            if (payServDTO.getResult() != 14) {

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    tblCheckPayOperations.get(i).setStatus(2);
                                } else {
                                    tblCheckPayOperations.get(i).setStatus(-1);
                                }

                                tblCheckPayOperations.get(i).setIdResult(payServDTO.getResult());
                                tblCheckPayOperations.get(i).setResMessage(payServDTO.getResultMessage());

                                tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));
                            }
                        }

                    }

                }

            }
        }catch (Exception e){
            log.error("Error in inProcessingTransactionsRepay" ,e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in inProcessingTransactionsRepay  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);

        }

    }

    @Transactional
    public void inProcessingTransactionsRepayComplex(){

        try {
            List<TblCheckPayOperations> tblCheckPayOperations = tblCheckPayOperationsRepository.findInProcessingTransactionsComplex();

            if(tblCheckPayOperations.size()>0){

                for (int i = 0; i <tblCheckPayOperations.size() ; i++) {

                    PayFTemplDTO payFTemplDTO = new PayFTemplDTO();

                    payFTemplDTO.setServiceId(tblCheckPayOperations.get(i).getIdServicePartner());
                    payFTemplDTO.setAccount(tblCheckPayOperations.get(i).getAccount());
                    payFTemplDTO.setAgentTransactionId(tblCheckPayOperations.get(i).getTransactionId());
                    payFTemplDTO.setAmountFrom(tblCheckPayOperations.get(i).getAmount());
                    payFTemplDTO.setAmountTo(tblCheckPayOperations.get(i).getAmount());


                    payFTemplDTO.setAgentTransactionDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                    PayServDTO payServDTO ;

                     if(tblCheckPayOperations.get(i).getMethod().equals("payExtraService")){
                        if(tblCheckPayOperations.get(i).getExtras()!=null){
                            payFTemplDTO.setExtras(getExtrasLogic(tblCheckPayOperations.get(i).getExtras()));
                        }
                        if(tblCheckPayOperations.get(i).getExtraServices()!=null){
                            payFTemplDTO.setExtraServices(getExtraSubservicesLogic(tblCheckPayOperations.get(i).getExtraServices()));
                        }
                        payServDTO = payFService.payService(payFTemplDTO);
                        logMethods.payOperationLog(System.getProperty("user.name"),"payExtraService",payServDTO.getResult(),payServDTO.getResultMessage()
                                ,payServDTO.getAgentTransactionId(),payServDTO.getTransactionId(),payServDTO.getStatus(),new Date());

                        if(payServDTO.getResult()==0){
                            tblCheckPayOperations.get(i).setIdResult(0);
                            tblCheckPayOperations.get(i).setResMessage(payServDTO.getResultMessage());
                            tblCheckPayOperations.get(i).setStatus(6);

                            tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));

                        }
                        else {

                            TblResultCode2Agents tblResultCode2Agents = tblResultCode2AgentsRepository.findFatal(payServDTO.getResult(), 2);

                            if (payServDTO.getResult() != 14) {

                                if (tblResultCode2Agents.getFatal() == 0) {
                                    tblCheckPayOperations.get(i).setStatus(2);
                                } else {
                                    tblCheckPayOperations.get(i).setStatus(-1);
                                }

                                tblCheckPayOperations.get(i).setIdResult(payServDTO.getResult());
                                tblCheckPayOperations.get(i).setResMessage(payServDTO.getResultMessage());

                                tblCheckPayOperationsRepository.save(tblCheckPayOperations.get(i));
                            }
                        }

                    }

                }

            }
        }catch (Exception e){
            log.error("Error in inProcessingTransactionsRepayComplex" ,e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in inProcessingTransactionsRepayComplex  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);

        }

    }


    public void updateActiveProviders(){

        try {
            //для Киви
            List<TblProviders2Agents> qiwiAgents = tblProviders2AgentsRepository.findQiwiProvidersByIdAgent();

            for (int i = 0; i <qiwiAgents.size() ; i++) {

                Optional<TblQiwiOperCodes> tblQiwiOperCodes = tblQiwiOperCodesRepository.findById(Long.valueOf(qiwiAgents.get(i).getCode()));

                if (tblQiwiOperCodes.isPresent()) {
                    qiwiAgents.get(i).setActive(1);
                    tblProviders2AgentsRepository.save(qiwiAgents.get(i));
                    log.info("Провайдер существует id: "+tblQiwiOperCodes.get().getId());
                }else{
                    qiwiAgents.get(i).setActive(0);
                    tblProviders2AgentsRepository.save(qiwiAgents.get(i));
                }

            }

            // для Пэйформ
            List<TblProviders2Agents> payFormAgents = tblProviders2AgentsRepository.findPayformProvidersByIdAgent();

            for (int i = 0; i <payFormAgents.size() ; i++) {

                Optional<TblPayformOpercodes> tblPayformOpercodes = tblPayformOpercodesRepository.findById(payFormAgents.get(i).getCode());

                if (tblPayformOpercodes.isPresent()) {
                    payFormAgents.get(i).setActive(1);
                    tblProviders2AgentsRepository.save(payFormAgents.get(i));
                    log.info("Провайдер существует id: "+tblPayformOpercodes.get().getId());
                }else{
                    payFormAgents.get(i).setActive(0);
                    tblProviders2AgentsRepository.save(payFormAgents.get(i));
                }

            }
            log.info("Обновление Автивности провайдеров закончился (updateActiveProviders)");
        }catch (Exception e){
            log.error("Error in updateActiveProviders", e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in updateActiveProviders  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }

    }

    public String getSerializedObject(Object env, Class... classesToBeBound) {
        String result;
        try {
            Marshaller m = JAXBContext.newInstance(classesToBeBound).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            m.marshal(env, sw);
            result = sw.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public Object getDeserializedObject(String strXml, Class... classesToBeBound) {
        Object obj;
        try {
            obj = JAXBContext.newInstance(classesToBeBound).createUnmarshaller().unmarshal(new StringReader(strXml));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    public  void checkBalanceSubAgents(){

        try {
            List<TblSubagents> listAgents = tblSubagentsRepository.findByIdAgent(1);
            double minBalance = 0;
            double balanceQiwi;
            double paySum = 0;


            for (int i = 0; i < listAgents.size(); i++) {
                GetBalanceDTO getBalanceDTO = new GetBalanceDTO() ;
                ProcedureResult procedureResult = new ProcedureResult() ;
                String systemName;
                String code;
                TblPaymentQiwiBalance tblPaymentQiwiBalance = new TblPaymentQiwiBalance();
                // вытащить часы тек дня
                Calendar rightNow = Calendar.getInstance();
                int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                int seqId = tblGetOperationRepository.getBalanceSeqId();
                Integer count = 1;
                Integer workDayToday = tblSubagentsRepository.checkWorkingDayToday();
                Integer workDayTomorrow = tblSubagentsRepository.checkWorkingDayTomorrow(count);

                //Если рабочий день
                if(workDayToday!=0) {

                    if (workDayToday == 1) {
                        minBalance = listAgents.get(i).getMinBalance();
                    }

                    if (hour > 13) {
                        if (workDayTomorrow == 0) {
                            count =0 ;
                            while (workDayTomorrow != 1){
                                count = count+1;
                                workDayTomorrow = tblSubagentsRepository.checkWorkingDayTomorrow(count);


                            }
                            minBalance = listAgents.get(i).getMinBalance() * (count-1);
                        }
                    }
                    code = listAgents.get(i).getCode();

                    getBalanceDTO = qiwiSoapService.getBalance(code);
                    balanceQiwi = getBalanceDTO.getBalance() == null ? 0 : getBalanceDTO.getBalance();

                    if (balanceQiwi < minBalance) {

                        if (balanceQiwi < 0) {
                            paySum =  Math.round(Math.abs(balanceQiwi) + minBalance);
                        } else {
                            paySum = Math.round(minBalance - balanceQiwi);
                        }



                    if(listAgents.get(i).getCode().equals("TNT_AUTH")){
                        systemName = "TNT";
                    }else if(listAgents.get(i).getCode().equals("IB_AUTH")){
                        systemName = "IB";
                    }else {
                        systemName = "IBCNP";
                    }

                        EnvelopePaymentTwo envelopePaymentTwo = new EnvelopePaymentTwo();

                        BodyPaymentTwoRequest bodyPaymentTwo = new BodyPaymentTwoRequest();
                        PaymentTwoRequest paymentTwoRequest = new PaymentTwoRequest();
                        bodyPaymentTwo.setPaymentTwoRequest(paymentTwoRequest);
                        envelopePaymentTwo.setHeader(new Header());
                        envelopePaymentTwo.setBody(bodyPaymentTwo);
                        paymentTwoRequest.setSystem("PPM");
                        paymentTwoRequest.setPaymentType("PPM_QIWI_MT100");
                        paymentTwoRequest.setCurrency("KZT");
                        paymentTwoRequest.setAmount(BigDecimal.valueOf(paySum));
                        paymentTwoRequest.setIdTerminal(0L);
                        XMLGregorianCalendar xmlDate = null;
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(new Date());
                        try {
                            xmlDate = DatatypeFactory.newInstance()
                                    .newXMLGregorianCalendar(gc);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(xmlDate!=null) {
                            paymentTwoRequest.setTransactionDate(xmlDate);
                        }

                        DataItem dataItem1 = new DataItem();
                        dataItem1.setAttrName("REFERENCEDOCNUM");
                        dataItem1.setAttrType("STRING");
                        dataItem1.setAttrValue("PPM"+seqId);

                        paymentTwoRequest.getDataItems().add(dataItem1);

                        DataItem dataItem2 = new DataItem();
                        dataItem2.setAttrName("DEBITACCID");
                        dataItem2.setAttrType("NUMBER");
                        dataItem2.setAttrValue(String.valueOf(listAgents.get(i).getIdIban()));

                        paymentTwoRequest.getDataItems().add(dataItem2);

                        DataItem dataItem3 = new DataItem();
                        dataItem3.setAttrName("DEBITACCCODE");
                        dataItem3.setAttrType("STRING");
                        dataItem3.setAttrValue(listAgents.get(i).getIban());

                        paymentTwoRequest.getDataItems().add(dataItem3);

                        DataItem dataItem4 = new DataItem();
                        dataItem4.setAttrName("SYSTEMNAME");
                        dataItem4.setAttrType("STRING");
                        dataItem4.setAttrValue(systemName);

                        paymentTwoRequest.getDataItems().add(dataItem4);

                        DataItem dataItem5 = new DataItem();
                        dataItem5.setAttrName("DATESPAY");
                        dataItem5.setAttrType("STRING");
                        dataItem5.setAttrValue("");

                        paymentTwoRequest.getDataItems().add(dataItem5);

                        DataItem dataItem6 = new DataItem();
                        dataItem6.setAttrName("TEXT_PURPOSE");
                        dataItem6.setAttrType("STRING");



                        DataItem dataItem7 = new DataItem();
                        dataItem7.setAttrName("KNP");
                        dataItem7.setAttrType("STRING");

                        if (systemName.equals("IBCNP")){
                            dataItem7.setAttrValue("390");
                            dataItem6.setAttrValue("Пополнение лимита для приема платежей от плательщиков в пользу " +
                                    "оператора через систему Qiwi согласно Договора о приеме платежей № 2992/09-06-2016 от 09.06.2016г. ");

                        } else {
                            Calendar c = Calendar.getInstance();
                            dataItem7.setAttrValue("890");
                            dataItem6.setAttrValue("Перевод авансового платежа для приема платежей от " +
                                    "плательщиков в пользу оператора через систему Qiwi сог-но Договора о приеме платежей" +
                                    " № 2992/09-06-2016 от 09.06.2016г. "+new SimpleDateFormat("dd.MM.yyyy").format(c.getTime()));
                        }
                        paymentTwoRequest.getDataItems().add(dataItem6);
                        paymentTwoRequest.getDataItems().add(dataItem7);

                        Date reqTime = new Date();
                       // PaymentTwoResponse paymentTwoResponse = (PaymentTwoResponse) webServiceTemplate.marshalSendAndReceive(paymentTwoRequest);
                       // PaymentTwoResponse paymentTwoResponse =  soapRestServ.request(SoapRequest.builder().body(getSerializedObject(paymentTwoRequest,PaymentTwoRequest.class)).build(),"paymentTwo");
                        String sw = getSerializedObject(envelopePaymentTwo, EnvelopePaymentTwo.class);
                        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(sw);
                        SoapResponse response = soapRestServ.request(req.build(), "paymentTwo");
                        EnvelopePaymentTwoResponse paymentTwoResponse = (EnvelopePaymentTwoResponse) getDeserializedObject(response.getResponse(),EnvelopePaymentTwoResponse.class);

                        Date resTime = new Date();


                       // if(procedureResult!=null) {
                        if (paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId()!=null){
                            logMethods.wsLog(0,reqTime,resTime,System.getProperty("user.name"),getSerializedObject(paymentTwoRequest,PaymentTwoRequest.class),getSerializedObject(paymentTwoResponse,PaymentTwoResponse.class));
                        tblPaymentQiwiBalance.setDocId(paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
                        tblPaymentQiwiBalance.setDateTime(new Date());
                        tblPaymentQiwiBalance.setPayAmount(paymentTwoRequest.getAmount());
                        tblPaymentQiwiBalance.setBalance(balanceQiwi);
                        tblPaymentQiwiBalance.setIban(dataItem3.getAttrValue());
                        tblPaymentQiwiBalance.setIdAgent(1);
                        tblPaymentQiwiBalance.setSystemName(systemName);

                        tblPaymentQiwiBalanceRepository.save(tblPaymentQiwiBalance);

                    }

                    log.info("DOCID : " + paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
                 }
                }
            }
            log.info("Пополнение счетов киви закончился checkBalanceSubAgents");
        }catch (Exception e){

            log.error("Error in checkBalanceSubAgents",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in checkBalanceSubAgents  DB: "+dbConfig.getDbName()
                    , "\n Причина Ошибки :\n\n"+"\n"+e);
        }

    }


    public void  payToPayFormTransactions(){
        try {
            ProcedureResult procedureResult ;
            TblSubagents ibTntSubAgent = tblSubagentsRepository.findLoginPassbyCode("IB_TNT_PAYFORM");
            TblSubagents ibCnpSubAgent = tblSubagentsRepository.findLoginPassbyCode("IBCNP_PAYFORM");
            int workDayToday = tblSubagentsRepository.checkWorkingDayToday();
try {
    double  sum = 0.0 ;
    double paySum;
    Integer count = tblPaymentQiwiBalanceRepository.oldPaytoPayformCountIB();
    String dates = tblPaymentQiwiBalanceRepository.oldPaytoPayformDatesIB();
    OldTransPayfDTO oldTransPayfDTO = new OldTransPayfDTO();
     if(count == null){
         oldTransPayfDTO.setCount(1);
         Calendar c = Calendar.getInstance();
         c.add(Calendar.DATE, -1);
         oldTransPayfDTO.setDate(new SimpleDateFormat("dd.MM.yyyy").format(c.getTime()));
     } else {
         oldTransPayfDTO.setCount(count);
         oldTransPayfDTO.setDate(dates);
     }
    List<TblCheckPayOperations> ibTransac = tblCheckPayOperationsRepository.sumTransactionsPayedwithAgentPayformIB(oldTransPayfDTO.getCount());
    if(workDayToday != 0){
    if (ibTransac.size()>0) {
        for (int i = 0; i < ibTransac.size(); i++) {

            sum = sum + Double.parseDouble(ibTransac.get(i).getAmount());

        }
        int seqId = tblGetOperationRepository.getBalanceSeqId();
        paySum = sum;

        EnvelopePaymentTwo envelopePaymentTwo = new EnvelopePaymentTwo();
        BodyPaymentTwoRequest bodyPaymentTwo = new BodyPaymentTwoRequest();
        PaymentTwoRequest paymentTwoRequest = new PaymentTwoRequest();
        bodyPaymentTwo.setPaymentTwoRequest(paymentTwoRequest);
        envelopePaymentTwo.setHeader(new Header());
        envelopePaymentTwo.setBody(bodyPaymentTwo);
        paymentTwoRequest.setSystem("PPM");
        paymentTwoRequest.setPaymentType("PPM_PAYFORM_MT100");
        paymentTwoRequest.setCurrency("KZT");
        paymentTwoRequest.setAmount(BigDecimal.valueOf(paySum));
        paymentTwoRequest.setIdTerminal(0L);
        XMLGregorianCalendar xmlDate = null;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        try {
            xmlDate = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(gc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(xmlDate!=null) {
            paymentTwoRequest.setTransactionDate(xmlDate);
        }
        DataItem dataItem1 = new DataItem();
        dataItem1.setAttrName("REFERENCEDOCNUM");
        dataItem1.setAttrType("STRING");
        dataItem1.setAttrValue("PPM"+seqId);

        paymentTwoRequest.getDataItems().add(dataItem1);

        DataItem dataItem2 = new DataItem();
        dataItem2.setAttrName("DEBITACCID");
        dataItem2.setAttrType("NUMBER");
        dataItem2.setAttrValue(String.valueOf(ibTntSubAgent.getIdIban()));

        paymentTwoRequest.getDataItems().add(dataItem2);

        DataItem dataItem3 = new DataItem();
        dataItem3.setAttrName("DEBITACCCODE");
        dataItem3.setAttrType("STRING");
        dataItem3.setAttrValue(ibTntSubAgent.getIban());

        paymentTwoRequest.getDataItems().add(dataItem3);

        DataItem dataItem4 = new DataItem();
        dataItem4.setAttrName("SYSTEMNAME");
        dataItem4.setAttrType("STRING");
        dataItem4.setAttrValue("IB");

        paymentTwoRequest.getDataItems().add(dataItem4);

        DataItem dataItem5 = new DataItem();
        dataItem5.setAttrName("DATESPAY");
        dataItem5.setAttrType("STRING");
        dataItem5.setAttrValue(oldTransPayfDTO.getDate());

        paymentTwoRequest.getDataItems().add(dataItem5);

        Date reqTime = new Date();
       // PaymentTwoResponse paymentTwoResponse = (PaymentTwoResponse) webServiceTemplate.marshalSendAndReceive(paymentTwoRequest);
        String sw = getSerializedObject(envelopePaymentTwo, EnvelopePaymentTwo.class);
        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(sw);
        SoapResponse response = soapRestServ.request(req.build(), "paymentTwo");
        EnvelopePaymentTwoResponse paymentTwoResponse = (EnvelopePaymentTwoResponse) getDeserializedObject(response.getResponse(),EnvelopePaymentTwoResponse.class);

        Date resTime = new Date();

        TblPaymentQiwiBalance tblPaymentQiwiBalance = new TblPaymentQiwiBalance();

        if (paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId() != null) {
            logMethods.wsLog(0,reqTime,resTime,System.getProperty("user.name"),getSerializedObject(paymentTwoRequest,PaymentTwoRequest.class),getSerializedObject(paymentTwoResponse,PaymentTwoResponse.class));
            for (int i = 0; i < ibTransac.size(); i++) {
                ibTransac.get(i).setPayedAgr(1);
                tblCheckPayOperationsRepository.save(ibTransac.get(i));
            }

            tblPaymentQiwiBalance.setDocId(paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
            tblPaymentQiwiBalance.setDateTime(new Date());
            tblPaymentQiwiBalance.setPayAmount(paymentTwoRequest.getAmount());
            tblPaymentQiwiBalance.setBalance(0);
            tblPaymentQiwiBalance.setIban(dataItem3.getAttrValue());
            tblPaymentQiwiBalance.setIdAgent(ibTntSubAgent.getIdAgent());
            tblPaymentQiwiBalance.setSystemName(dataItem4.getAttrValue());
            tblPaymentQiwiBalanceRepository.save(tblPaymentQiwiBalance);
        }
        log.info("DOCID : " + paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
     }
    }
}catch (Exception e){
    log.error("ERROR IB PAY to PAYFORM",e);
    emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in IB pay to Payform  DB: "+dbConfig.getDbName()
            , "\n Причина Ошибки :\n\n"+"\n"+e);
}

try {
    double  sum = 0.0 ;
    double paySum;
    Integer count = tblPaymentQiwiBalanceRepository.oldPaytoPayformCountTNT();
    String dates = tblPaymentQiwiBalanceRepository.oldPaytoPayformDatesTNT();
    OldTransPayfDTO oldTransPayfDTO = new OldTransPayfDTO();
    if(count == null){
        oldTransPayfDTO.setCount(1);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        oldTransPayfDTO.setDate(new SimpleDateFormat("dd.MM.yyyy").format(c.getTime()));
    } else{
        oldTransPayfDTO.setDate(dates);
        oldTransPayfDTO.setCount(count);
    }
    List<TblCheckPayOperations> tntTransac = tblCheckPayOperationsRepository.sumTransactionsPayedwithAgentPayformTNT(oldTransPayfDTO.getCount());

    if(workDayToday != 0) {
        if (tntTransac.size() > 0) {
            for (int i = 0; i < tntTransac.size(); i++) {

                sum = sum + Double.parseDouble(tntTransac.get(i).getAmount());

            }
            paySum = sum;

            int seqId = tblGetOperationRepository.getBalanceSeqId();


            EnvelopePaymentTwo envelopePaymentTwo = new EnvelopePaymentTwo();
            BodyPaymentTwoRequest bodyPaymentTwo = new BodyPaymentTwoRequest();
            PaymentTwoRequest paymentTwoRequest = new PaymentTwoRequest();
            bodyPaymentTwo.setPaymentTwoRequest(paymentTwoRequest);
            envelopePaymentTwo.setHeader(new Header());
            envelopePaymentTwo.setBody(bodyPaymentTwo);
            paymentTwoRequest.setSystem("PPM");
            paymentTwoRequest.setPaymentType("PPM_PAYFORM_MT100");
            paymentTwoRequest.setCurrency("KZT");
            paymentTwoRequest.setAmount(BigDecimal.valueOf(paySum));
            paymentTwoRequest.setIdTerminal(0L);
            XMLGregorianCalendar xmlDate = null;
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            try {
                xmlDate = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(gc);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if(xmlDate!=null) {
                paymentTwoRequest.setTransactionDate(xmlDate);
            }
            DataItem dataItem1 = new DataItem();
            dataItem1.setAttrName("REFERENCEDOCNUM");
            dataItem1.setAttrType("STRING");
            dataItem1.setAttrValue("PPM"+seqId);

            paymentTwoRequest.getDataItems().add(dataItem1);

            DataItem dataItem2 = new DataItem();
            dataItem2.setAttrName("DEBITACCID");
            dataItem2.setAttrType("NUMBER");
            dataItem2.setAttrValue(String.valueOf(ibTntSubAgent.getIdIban()));

            paymentTwoRequest.getDataItems().add(dataItem2);

            DataItem dataItem3 = new DataItem();
            dataItem3.setAttrName("DEBITACCCODE");
            dataItem3.setAttrType("STRING");
            dataItem3.setAttrValue(ibTntSubAgent.getIban());

            paymentTwoRequest.getDataItems().add(dataItem3);

            DataItem dataItem4 = new DataItem();
            dataItem4.setAttrName("SYSTEMNAME");
            dataItem4.setAttrType("STRING");
            dataItem4.setAttrValue("IB");

            paymentTwoRequest.getDataItems().add(dataItem4);

            DataItem dataItem5 = new DataItem();
            dataItem5.setAttrName("DATESPAY");
            dataItem5.setAttrType("STRING");
            dataItem5.setAttrValue(oldTransPayfDTO.getDate());

            paymentTwoRequest.getDataItems().add(dataItem5);
            Date reqTime = new Date();
           // PaymentTwoResponse paymentTwoResponse = (PaymentTwoResponse) webServiceTemplate.marshalSendAndReceive(paymentTwoRequest);
            String sw = getSerializedObject(envelopePaymentTwo, EnvelopePaymentTwo.class);
            SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(sw);
            SoapResponse response = soapRestServ.request(req.build(), "paymentTwo");
            EnvelopePaymentTwoResponse paymentTwoResponse = (EnvelopePaymentTwoResponse) getDeserializedObject(response.getResponse(),EnvelopePaymentTwoResponse.class);

            Date resTime = new Date();

            TblPaymentQiwiBalance tblPaymentQiwiBalance = new TblPaymentQiwiBalance();
            if (paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId() != null) {
                logMethods.wsLog(0,reqTime,resTime,System.getProperty("user.name"),getSerializedObject(paymentTwoRequest,PaymentTwoRequest.class),getSerializedObject(paymentTwoResponse,PaymentTwoResponse.class));
                for (int i = 0; i < tntTransac.size(); i++) {

                    tntTransac.get(i).setPayedAgr(1);
                    tblCheckPayOperationsRepository.save(tntTransac.get(i));
                }

                tblPaymentQiwiBalance.setDocId(paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
                tblPaymentQiwiBalance.setDateTime(new Date());
                tblPaymentQiwiBalance.setPayAmount(paymentTwoRequest.getAmount());
                tblPaymentQiwiBalance.setBalance(0);
                tblPaymentQiwiBalance.setIban(dataItem3.getAttrValue());
                tblPaymentQiwiBalance.setIdAgent(ibTntSubAgent.getIdAgent());
                tblPaymentQiwiBalance.setSystemName(dataItem4.getAttrValue());

                tblPaymentQiwiBalanceRepository.save(tblPaymentQiwiBalance);
            }
            log.info("DOCID : " + paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
        }
    }
} catch (Exception e){
    log.error("Error in Tnt pay to Payform",e);
    emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in Tnt pay to Payform  DB: "+dbConfig.getDbName()
            , "\n Причина Ошибки :\n\n"+"\n"+e);
}

try {
    double  sum = 0.0 ;
    double paySum;
    Integer count = tblPaymentQiwiBalanceRepository.oldPaytoPayformCountIBCNP();
    String dates = tblPaymentQiwiBalanceRepository.oldPaytoPayformDatesIBCNP();
    OldTransPayfDTO oldTransPayfDTO = new OldTransPayfDTO();
    if(count == null){
        oldTransPayfDTO.setCount(1);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        oldTransPayfDTO.setDate(new SimpleDateFormat("dd.MM.yyyy").format(c.getTime()));
    } else {
        oldTransPayfDTO.setCount(count);
        oldTransPayfDTO.setDate(dates);
    }
    List<TblCheckPayOperations> ibCnpTransac = tblCheckPayOperationsRepository.sumTransactionsPayedwithAgentPayformIBCNP(oldTransPayfDTO.getCount());
    if(workDayToday != 0) {
        if (ibCnpTransac.size() > 0) {
            for (int i = 0; i < ibCnpTransac.size(); i++) {

                sum = sum + Double.parseDouble(ibCnpTransac.get(i).getAmount());


            }
            paySum = sum;
            int seqId = tblGetOperationRepository.getBalanceSeqId();

            EnvelopePaymentTwo envelopePaymentTwo = new EnvelopePaymentTwo();
            BodyPaymentTwoRequest bodyPaymentTwo = new BodyPaymentTwoRequest();
            PaymentTwoRequest paymentTwoRequest = new PaymentTwoRequest();
            bodyPaymentTwo.setPaymentTwoRequest(paymentTwoRequest);
            envelopePaymentTwo.setHeader(new Header());
            envelopePaymentTwo.setBody(bodyPaymentTwo);
            paymentTwoRequest.setSystem("PPM");
            paymentTwoRequest.setPaymentType("PPM_PAYFORM_MT100");
            paymentTwoRequest.setCurrency("KZT");
            paymentTwoRequest.setAmount(BigDecimal.valueOf(paySum));
            paymentTwoRequest.setIdTerminal(0L);
            XMLGregorianCalendar xmlDate = null;
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            try {
                xmlDate = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(gc);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if(xmlDate!=null) {
                paymentTwoRequest.setTransactionDate(xmlDate);
            }
            DataItem dataItem1 = new DataItem();
            dataItem1.setAttrName("REFERENCEDOCNUM");
            dataItem1.setAttrType("STRING");
            dataItem1.setAttrValue("PPM"+seqId);

            paymentTwoRequest.getDataItems().add(dataItem1);

            DataItem dataItem2 = new DataItem();
            dataItem2.setAttrName("DEBITACCID");
            dataItem2.setAttrType("NUMBER");
            dataItem2.setAttrValue(String.valueOf(ibCnpSubAgent.getIdIban()));

            paymentTwoRequest.getDataItems().add(dataItem2);

            DataItem dataItem3 = new DataItem();
            dataItem3.setAttrName("DEBITACCCODE");
            dataItem3.setAttrType("STRING");
            dataItem3.setAttrValue(ibCnpSubAgent.getIban());

            paymentTwoRequest.getDataItems().add(dataItem3);

            DataItem dataItem4 = new DataItem();
            dataItem4.setAttrName("SYSTEMNAME");
            dataItem4.setAttrType("STRING");
            dataItem4.setAttrValue("IB");

            paymentTwoRequest.getDataItems().add(dataItem4);

            DataItem dataItem5 = new DataItem();
            dataItem5.setAttrName("DATESPAY");
            dataItem5.setAttrType("STRING");
            dataItem5.setAttrValue(oldTransPayfDTO.getDate());

            paymentTwoRequest.getDataItems().add(dataItem5);

            Date reqTime = new Date();
          //  PaymentTwoResponse paymentTwoResponse = (PaymentTwoResponse) webServiceTemplate.marshalSendAndReceive(paymentTwoRequest);
            String sw = getSerializedObject(envelopePaymentTwo, EnvelopePaymentTwo.class);
            SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(sw);
            SoapResponse response = soapRestServ.request(req.build(), "paymentTwo");
            EnvelopePaymentTwoResponse paymentTwoResponse = (EnvelopePaymentTwoResponse) getDeserializedObject(response.getResponse(),EnvelopePaymentTwoResponse.class);

            Date resTime = new Date();

            TblPaymentQiwiBalance tblPaymentQiwiBalance = new TblPaymentQiwiBalance();

            if (paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId() != null) {
                logMethods.wsLog(0,reqTime,resTime,System.getProperty("user.name"),getSerializedObject(paymentTwoRequest,PaymentTwoRequest.class),getSerializedObject(paymentTwoResponse,PaymentTwoResponse.class));

                for (int i = 0; i < ibCnpTransac.size(); i++) {
                    ibCnpTransac.get(i).setPayedAgr(1);

                    tblCheckPayOperationsRepository.save(ibCnpTransac.get(i));
                }

                tblPaymentQiwiBalance.setDocId(paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
                tblPaymentQiwiBalance.setDateTime(new Date());
                tblPaymentQiwiBalance.setPayAmount(paymentTwoRequest.getAmount());
                tblPaymentQiwiBalance.setBalance(0);
                tblPaymentQiwiBalance.setIban(dataItem3.getAttrValue());
                tblPaymentQiwiBalance.setSystemName(dataItem4.getAttrValue());
                tblPaymentQiwiBalance.setIdAgent(ibCnpSubAgent.getIdAgent());

                tblPaymentQiwiBalanceRepository.save(tblPaymentQiwiBalance);
            }
            log.info("DOCID : " + paymentTwoResponse.getBody().getPaymentTwoResponse().getObsDocId());
        }
    }
}catch (Exception e){
    log.error("Error in IBCNP pay to Payform",e);
    emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in IBCNP pay to Payform  DB: "+dbConfig.getDbName()
            , "\n Причина Ошибки :\n\n"+"\n"+e);
}


            log.info("Пополнение счетов пэйформ закончился payToPayFormTransactions");

        }catch (Exception e){
            log.error("Error in payToPayFormTransactions",e);

        }

    }




}//end of class


