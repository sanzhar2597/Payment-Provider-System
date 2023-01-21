package kz.ufo.service;



import kz.ufo.config.ApplicationConfig;
import kz.ufo.config.DBConfig;
import kz.ufo.config.ProxyConfig;
import kz.ufo.config.RestConfigur;
import kz.ufo.dto.*;
import kz.ufo.entity.*;
import kz.ufo.repository.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



import java.net.*;
import java.util.*;


@Service
@Slf4j
public class PayFServiceImpl implements PayFService {

    @Autowired
    ApplicationConfig appConf;

    @Autowired
    RestConfigur restConfigur;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TblPayformOpercodesRepository tblPayformOpercodesRepository;

    @Autowired
    TblPayformProviderParamRepository tblPayformProviderParamRepository;

    @Autowired
    TblPayformProviderExstraRepository tblPayformProviderExstraRepository;

    @Autowired
    LogMethods logMethods;

    @Autowired
    ProxyConfig proxyConfig;

    @Autowired
    TblResultCodeRepository tblResultCodeRepository;

    @Autowired
    TblProviderDisplaysRepository tblProviderDisplaysRepository;

    @Autowired
    TblSprRepository tblSprRepository;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    DBConfig dbConfig;


    public String getValue(String code){
        TblSpr tblSpr = new TblSpr();
        tblSpr = tblSprRepository.findByCode(code);
        return tblSpr.getValue();
    }


    @Override
    @SneakyThrows
    public PayServDTO payService(PayFTemplDTO payFTemplDTO) {
        proxyConfig.setProxy();

        Date reqDate = new Date();
        ResponseEntity<PayServDTO> responseEntity = restTemplate.exchange(
                RequestEntity.post(URI.create(getValue("PF_URL")) + "/pay")
                        .header(restConfigur.getAuth(), getValue("PF_TOKEN"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(payFTemplDTO),
                PayServDTO.class);




        logMethods.wsLog(responseEntity.getStatusCode().value()==200 ? 0 :responseEntity.getStatusCode().value(),reqDate, new Date(), System.getProperty("user.name"), payFTemplDTO.toString(), responseEntity.getBody().toString());//Log
        log.info("Pay: " + responseEntity.getBody());

        PayServDTO payServDTO = new PayServDTO();
        payServDTO = responseEntity.getBody();
        TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(payServDTO.getResult(),2);
        payServDTO.setResult(resultCode.getId());
        payServDTO.setResultMessage(resultCode.getText());

        //proxyConfig.clearProxy();

        return  payServDTO;

    }

    @Override
    @SneakyThrows
    public CheckServDTO checkService(PayFTemplDTO payFTemplDTO) {

        proxyConfig.setProxy();

        Date reqDate = new Date();
            ResponseEntity<CheckServDTO> responseEntity = restTemplate.exchange(
                    RequestEntity.post(URI.create(getValue("PF_URL")) + "/check")
                            .header(restConfigur.getAuth(), getValue("PF_TOKEN"))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .body(payFTemplDTO),
                    CheckServDTO.class);


        logMethods.wsLog(responseEntity.getStatusCode().value()==200 ? 0 :responseEntity.getStatusCode().value(),reqDate, new Date(), System.getProperty("user.name"), payFTemplDTO.toString(), responseEntity.getBody().toString());

           CheckServDTO checkServDTO = new CheckServDTO();
        checkServDTO = responseEntity.getBody();

        if(checkServDTO.getDisplays()!=null) {
            Set<Map.Entry<String, String>> entrySet = checkServDTO.getDisplays().entrySet();
            Map<String, String> dispMap = new HashMap<>();
            for (Map.Entry<String, String> pair : entrySet) {
                TblProviderDisplays tblProviderDisplays =
                        tblProviderDisplaysRepository.findByProviderAgentCode(pair.getKey(), 2, payFTemplDTO.getServiceId());
                if (tblProviderDisplays != null) {
                    dispMap.put(tblProviderDisplays.getCode(), pair.getValue());
                }
            }

            checkServDTO.setDisplays(dispMap);
        }


        TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkServDTO.getResult(),2);
        checkServDTO.setResult(resultCode.getId());
        checkServDTO.setResultMessage(resultCode.getText());

     //   proxyConfig.clearProxy();

        return  checkServDTO;

    }

    @Override
    @SneakyThrows
    public void getAndSavePayFormServices() {

        Date reqDate = new Date();
       try {
           proxyConfig.setProxy();
           ResponseEntity<GetServicesPayformDTO> responseEntity = restTemplate.exchange(
                   RequestEntity.post(getValue("PF_URL") + "/services")
                           .header(restConfigur.getAuth(),getValue("PF_TOKEN"))
                           .header("Content-Type", "application/json")
                           .header("Accept", "application/json")
                           .body(""), GetServicesPayformDTO.class);


           if(responseEntity.getBody().getResult()==0) {

               logMethods.wsLog(responseEntity.getStatusCode().value()==200 ? 0 :responseEntity.getStatusCode().value(),reqDate, new Date(), System.getProperty("user.name"), "(PAYFORM) getAndSavePayFormServices{}", responseEntity.getBody().toString());

               log.info("Удаление провайдеров Payform");
               tblPayformOpercodesRepository.deleteAll();
               tblPayformProviderParamRepository.deleteAll();
               tblPayformProviderExstraRepository.deleteAll();




               List<Services>  servicesPayforms = responseEntity.getBody().getServices();
               for (int i = 0; i < servicesPayforms.size(); i++) {

                   TblPayformOpercodes tblPayformOpercodes = new TblPayformOpercodes();
                   tblPayformOpercodes.setId(servicesPayforms.get(i).getServiceId());
                   tblPayformOpercodes.setCode("Code "+servicesPayforms.get(i).getServiceId());
                   tblPayformOpercodes.setCountry(servicesPayforms.get(i).getCountry());
                   tblPayformOpercodes.setFixedPayment(servicesPayforms.get(i).isFixedPayment());
                   tblPayformOpercodes.setFullname(servicesPayforms.get(i).getName());
                   tblPayformOpercodes.setGroupName(servicesPayforms.get(i).getGroup());
                   tblPayformOpercodes.setMinSum(servicesPayforms.get(i).getMinSum());

                   tblPayformOpercodesRepository.save(tblPayformOpercodes);

                   List<InputsPayform> inputs =  servicesPayforms.get(i).getInputs();
                   for (int j = 0; j <inputs.size() ; j++) {
                       TblPayformProviderParam tblPayformProviderParam  = new TblPayformProviderParam();
                       tblPayformProviderParam.setIdProvider(servicesPayforms.get(i).getServiceId());
                       tblPayformProviderParam.setHeader(inputs.get(j).getTitle());
                       tblPayformProviderParam.setName(inputs.get(j).getName());

                       tblPayformProviderParamRepository.save(tblPayformProviderParam);
                   }

                   List<DisplaysPayform> ss = new ArrayList<>();
                   ss.add(new DisplaysPayform("1",false,""));
                   List<DisplaysPayform> displays = Optional.ofNullable(servicesPayforms.get(i).getDisplays()).orElse(ss);

                   for (int j = 0; j < displays.size(); j++) {
                       if (!displays.get(j).getName().equals("1")) {
                           TblPayfromProviderExstra tblPayfromProviderExstra = new TblPayfromProviderExstra();
                           tblPayfromProviderExstra.setIdProvider(servicesPayforms.get(i).getServiceId());
                           tblPayfromProviderExstra.setHeader(displays.get(j).getTitle());
                           tblPayfromProviderExstra.setDispName(displays.get(j).getName());

                           tblPayformProviderExstraRepository.save(tblPayfromProviderExstra);
                       }
                   }

               }

           }
        //   proxyConfig.clearProxy();
       }catch (Exception e){
            log.info(e.getMessage());
           emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in updateProviders Payform DB: "+dbConfig.getDbName()
                   ," Причина Ошибки :\n\n"+e);
       }

    }

    public String getProviderbyPhone(PhoneProviderDTO phoneProviderDTO){
        proxyConfig.setProxy();
        Date reqDate = new Date();
        ResponseEntity<PhoneProviderRespDTO> responseEntity = restTemplate.exchange(
                RequestEntity.post(URI.create(getValue("PF_URL")) + "/operatorByPhone")
                        .header(restConfigur.getAuth(), getValue("PF_TOKEN"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(phoneProviderDTO),
                PhoneProviderRespDTO.class);

        logMethods.wsLog(responseEntity.getBody().getResult(),reqDate, new Date(), System.getProperty("user.name"), phoneProviderDTO.toString(), responseEntity.getBody().toString());

        String providerId = responseEntity.getBody().getServiceId();
      //  proxyConfig.clearProxy();

        return providerId;

    }


}
