package kz.duff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.duff.config.EmailSenderConfig;
import kz.duff.config.RestConfig;
import kz.duff.dto.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Transactional
@Slf4j
public class MainServiceImpl implements MainService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    RestConfig restConfig;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GetMethodsService getMethodsService;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    EmailSenderConfig emailSenderConfig;

    @SneakyThrows
    public Message messageParse(PaymentDTO paymentDTO){
        String orderJson = objectMapper.writeValueAsString(paymentDTO);
        Message message = MessageBuilder
                .withBody(orderJson.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        return message;
    }

    public String getAuthUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication.getName();
        log.info("USERNAME:  " +user);
        return user;
    }

    @Override
    public CheckServDTO getProvidersInfo(GetRequestDTO paymentDTO) {
        try {
            paymentDTO.setSystemName(getAuthUser());
            ResponseEntity<CheckServDTO> responseEntity =
                    restTemplate.postForEntity(restConfig.getURL()+"/getProvidersInfo",paymentDTO,CheckServDTO.class);

            return responseEntity.getBody();
        }catch (Exception e){
            log.error("API Error in getProvidersInfo",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API getProvidersInfo method  "+emailSenderConfig.getDbValue()
                    ,"Service Id:"+paymentDTO.getService()+"\n"+
                          "Account : "+paymentDTO.getAccount()+"\n"+
                            "SystemName : "+paymentDTO.getSystemName()+"\n Причина Ошибки :\n\n"+"\n"+e);
            return  null;
        }
    }

    @Override
    public CheckServDTO checkMethod(CheckRequestDTO paymentDTO) {
        try {
            paymentDTO.setSystemName(getAuthUser());
            ResponseEntity<CheckServDTO> responseEntity = restTemplate.postForEntity(restConfig.getURL()+"/check",paymentDTO,CheckServDTO.class);

            return responseEntity.getBody();

        }catch (Exception e){
            log.error("API Check method Error ", e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in  API Check method  : "+emailSenderConfig.getDbValue()
                    ,"Service Id:"+paymentDTO.getService()+"\n" +
                            "REFERENCENUMBER :"+paymentDTO.getReferenceNumber()+"\n"+
                            "SYSTEMNAME :"+paymentDTO.getSystemName()+"\n"+
                            "\n Причина Ошибки :\n\n"+"\n"+e);
            return  null;
        }

    }

    @Override
    public PayERDTO payMethod(PaymentDTO paymentDTO) {
        PayERDTO payERDTO = new PayERDTO();
        paymentDTO.setSystemName(getAuthUser());
        try {

            payERDTO.setResultCode(0);
            payERDTO.setResultMessage("Не найден платеж по такой id= "+paymentDTO.getId());
            List<TransacIdDTO> transacIdDTOS = getMethodsService.checkIdTransactionForPay();
            for (int i = 0; i <transacIdDTOS.size() ; i++) {
                if(paymentDTO.getId().equals(transacIdDTOS.get(i).getId()) && transacIdDTOS.get(i).getMethod().equals("check")){
                    rabbitTemplate.convertAndSend("ppm.exchange", "payment", messageParse(paymentDTO));
                    log.info("Платеж отправлен в очередь ppm.payment.queue  id: "+paymentDTO.getId());
                    payERDTO.setResultCode(1);
                    payERDTO.setResultMessage("Платеж принят по id= "+transacIdDTOS.get(i).getId());
                } else if(paymentDTO.getId().equals(transacIdDTOS.get(i).getId())  && !transacIdDTOS.get(i).getMethod().equals("check")){
                    payERDTO.setResultCode(2);
                    payERDTO.setResultMessage("Платеж уже проведен по id= "+transacIdDTOS.get(i).getId());
                }
            }


        }catch (Exception e){
            log.error("Ошибка при отправке в очередь  id: "+paymentDTO.getId(),e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API Pay method   "+emailSenderConfig.getDbValue()
                    ," Id:"+paymentDTO.getId()+"\n" +
                            "SystemName : "+paymentDTO.getSystemName()+
                            "\n Причина Ошибки :\n\n"+"\n"+e);
            payERDTO.setResultMessage(e.getMessage());
            payERDTO.setResultCode(500);
        }
        return payERDTO;
    }
}
