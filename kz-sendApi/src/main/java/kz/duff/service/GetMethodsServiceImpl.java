package kz.duff.service;

import kz.duff.config.EmailSenderConfig;
import kz.duff.config.RestConfig;
import kz.duff.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class GetMethodsServiceImpl implements GetMethodsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestConfig restConfig;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    EmailSenderConfig emailSenderConfig;

    @Override
    public List<TblProvidersAllDTO> getProviderList() {
        try {
            ResponseEntity<List<TblProvidersAllDTO>> responseEntity =
                    restTemplate.exchange(restConfig.getURL()+"/providers", HttpMethod.GET,
                            null, new ParameterizedTypeReference<List<TblProvidersAllDTO>>() {
                            })   ;

            List<TblProvidersAllDTO> allOperCodes = responseEntity.getBody();

            return allOperCodes;
        }catch (Exception e){
            log.error("Error in API getProviderList",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API getProvidersInfo method  "+emailSenderConfig.getDbValue()
                    ," Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }


    }


    @Override
    public GetProviderByPhoneDTO getProviderNameByPhone(PhoneProviderDTO phone) {
        try {
            ResponseEntity<GetProviderByPhoneDTO> responseEntity =  restTemplate.postForEntity(restConfig.getURL()+"/providerByPhone",phone,GetProviderByPhoneDTO.class);
            return responseEntity.getBody();
        }catch (Exception e){
            log.error("Error in API getProviderNameByPhone",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API getProviderNameByPhone method  "+emailSenderConfig.getDbValue()
                    ," Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }

    @Override
    public List<GetLogoDTO> getLogo() {
        try {
            ResponseEntity<List<GetLogoDTO>> responseEntity =
                    restTemplate.exchange(restConfig.getURL()+"/getLogo", HttpMethod.GET,
                            null, new ParameterizedTypeReference<List<GetLogoDTO>>() {
                            })   ;

            List<GetLogoDTO> allLogo = responseEntity.getBody();

            return allLogo;
        }catch (Exception e){
            log.error("Error in API getLogo",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API getLogo method  "+emailSenderConfig.getDbValue()
                    ," Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }

    @Override
    public ListGetStatusDTO getStatusPay(ListStatusDTO statusIdDTO) {
        try {
            ResponseEntity<ListGetStatusDTO> responseEntity =
                    restTemplate.postForEntity(restConfig.getURL()+"/getStatusPay",statusIdDTO,ListGetStatusDTO.class);

            return responseEntity.getBody();
        }catch (Exception e){
            log.error("Error in API getStatusPay",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API getStatusPay method  "+emailSenderConfig.getDbValue()
                    ," Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }

    @Override
    public List<TransacIdDTO> checkIdTransactionForPay() {
        try {
            ResponseEntity<List<TransacIdDTO>> responseEntity =
                    restTemplate.exchange(restConfig.getURL()+"/getTransactionIdForPay", HttpMethod.POST,
                            null, new ParameterizedTypeReference<List<TransacIdDTO>>() {
                            })  ;

            List<TransacIdDTO> transacIdDTOS = responseEntity.getBody();

            return transacIdDTOS;
        }catch (Exception e){
            log.error("Error in API checkIdTransactionForPay",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in API checkIdTransactionForPay method  "+emailSenderConfig.getDbValue()
                    ," Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }
    }

    @Override
    public List<TblAllResultCodesDTO> getResultCodes() {
        ResponseEntity<List<TblAllResultCodesDTO>> responseEntity =
                restTemplate.exchange(restConfig.getURL()+"/getResultCodes", HttpMethod.GET,
                        null, new ParameterizedTypeReference<List<TblAllResultCodesDTO>>() {
                        })   ;

        List<TblAllResultCodesDTO> allResultCodes = responseEntity.getBody();

        return allResultCodes;
    }
}
