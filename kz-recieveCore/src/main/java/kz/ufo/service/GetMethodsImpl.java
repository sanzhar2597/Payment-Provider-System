package kz.ufo.service;

import kz.ufo.config.DBConfig;
import kz.ufo.dto.*;
import kz.ufo.entity.*;
import kz.ufo.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
@Slf4j
public class GetMethodsImpl implements GetMethods {

    @Autowired
    private TblProviders2AgentsRepository tblProviders2AgentsRepository;

    @Autowired
    private TblProvidersParamsRepository tblProvidersParamsRepository;

    @Autowired
    private TblProvidersParamExtraRepository tblProvidersParamExtraRepository;

    @Autowired
    private QiwiSoapServiceImpl qiwiSoapService;

    @Autowired
    private  PayFServiceImpl payFService;

    @Autowired
    TblProvidersBlobRepository tblProvidersBlobRepository;

    @Autowired
    TblCheckPayOperationsRepository tblCheckPayOperationsRepository;

    @Autowired
    TblProvidersRepository tblProvidersRepository;

    @Autowired
    TblResultCodeRepository tblResultCodeRepository;

    @Autowired
    TblResultCode2AgentsRepository tblResultCode2AgentsRepository;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    DBConfig dbConfig;

    @Autowired
    TblProvidersParamsExtraTypeValuesRepository tblProvidersParamsExtraTypeValuesRepository;

    @Autowired
    TblProviderDisplaysRepository tblProviderDisplaysRepository;

    @Override
    public List<TblProvidersAllDTO> getProviderList() {
        try {
            List<TblProviders2Agents> providers = tblProviders2AgentsRepository.findUniqueByIdOrderByPriority();
            List<TblProvidersAllDTO> all = new ArrayList<>();

            for (int i = 0; i < providers.size() ; i++) {
                TblProvidersAllDTO allPar = new TblProvidersAllDTO();
                allPar.setId(providers.get(i).getIdProvider());
                allPar.setActive(providers.get(i).getActive());
                allPar.setCommission(providers.get(i).getCommission());
                allPar.setCurrid(providers.get(i).getCurrId());
                TblProviders tblProviders = tblProvidersRepository.findById(providers.get(i).getIdProvider());
                allPar.setFullname(tblProviders.getFullname());
                allPar.setComplex(providers.get(i).getComplex());
                allPar.setKnp(providers.get(i).getKnp());
                allPar.setRegId(providers.get(i).getRegId());
                allPar.setMinSum(providers.get(i).getMinSum());
                allPar.setMaxSum(providers.get(i).getMaxSum());
                allPar.setFee(providers.get(i).getFee());
                allPar.setGroupId(providers.get(i).getGroupId());
                allPar.setFixPrice(providers.get(i).getFixPrice());
                List<TblProvidersParams> findMainByIdProv = tblProvidersParamsRepository.findAllByIdProvider(providers.get(i).getIdProvider());
                allPar.setMainParams(findMainByIdProv);
                List<TblProviderDisplays> findDispbyIdProv = tblProviderDisplaysRepository.findAllByIdProvider(providers.get(i).getIdProvider());
                allPar.setDispParams(findDispbyIdProv);
                List<TblProvidersParamsExtraTypeValues> findValByIdProv = tblProvidersParamsExtraTypeValuesRepository.findAllByIdProvider(providers.get(i).getIdProvider());
                allPar.setExtraKeyValues(findValByIdProv);
                all.add(allPar);
            }
            return all;
        }catch (Exception e){
            log.info("GetProviderList : ",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in GetProviderList method  DB: "+dbConfig.getDbName()
                    ,"\n Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }

    @Override
    public GetProviderByPhoneDTO getProviderNameByPhone(PhoneProviderDTO phone) {
        String jsonParse = phone.getPhoneNumber();
       // String returnedValue;
        GetProviderByPhoneDTO getProviderByPhoneDTO = new GetProviderByPhoneDTO();
        try {
        String response = qiwiSoapService.getProviderbyPhone(jsonParse);
        if(response==null){
            response = payFService.getProviderbyPhone(phone);
        }
         TblProviders2Agents tblProviders2Agents = tblProviders2AgentsRepository.findByCode(response);
        if(tblProviders2Agents != null) {
           getProviderByPhoneDTO.setProviderID(tblProviders2Agents.getIdProvider());
           getProviderByPhoneDTO.setErrMsg("OK");
           getProviderByPhoneDTO.setErrCode(1);

        } else {

            response = payFService.getProviderbyPhone(phone);

            TblProviders2Agents tblProviders2Agents2 = tblProviders2AgentsRepository.findByCode(response);
            if(tblProviders2Agents2 != null) {
                getProviderByPhoneDTO.setProviderID(tblProviders2Agents2.getIdProvider());
                getProviderByPhoneDTO.setErrMsg("OK");
                getProviderByPhoneDTO.setErrCode(1);
            } else {
                getProviderByPhoneDTO.setProviderID(0l);
                getProviderByPhoneDTO.setErrMsg("Provider Not Found");
                getProviderByPhoneDTO.setErrCode(0);
            }
        }
            return getProviderByPhoneDTO;
        }
        catch (Exception e){
            log.error("getProviderNameByPhone ",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in getProviderNameByPhone method  DB: "+dbConfig.getDbName()
                    ,"PHONENUMBER: "+phone.getPhoneNumber()+"\n"+
                    "\n Причина Ошибки :\n\n"+"\n"+e);
            getProviderByPhoneDTO.setProviderID(0l);
            getProviderByPhoneDTO.setErrMsg("Exception");
            getProviderByPhoneDTO.setErrCode(2);

            return  getProviderByPhoneDTO;
        }
    }


    @Override
    public List<GetLogoDTO> getLogo() {
        try {
            List<TblProvidersBlob> allLogo = tblProvidersBlobRepository.findAll();
            List<GetLogoDTO> logos = new ArrayList<>();
            for (int i = 0; i <allLogo.size() ; i++) {
                GetLogoDTO getLogoDTO = new GetLogoDTO();

                byte[] encodeBase64 = Base64.getEncoder().encode(allLogo.get(i).getLogo());
                String base64Encoded = new String(encodeBase64,"UTF-8");

                getLogoDTO.setId(allLogo.get(i).getId());
                getLogoDTO.setIdProvider(allLogo.get(i).getIdProvider());
                getLogoDTO.setLogo(base64Encoded);
                logos.add(getLogoDTO);
            }

            return  logos;
        }catch (Exception e){
            log.error("Error in GetLogo");
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in GetLogo method  DB: "+dbConfig.getDbName()
                    ,"\n Причина Ошибки :\n\n"+"\n"+e.getCause());
            return null;
        }
    }

    @Override
    public ListGetStatusPayDTO getStatusPay(ListStatusDTO statusIdDTO) {

        try {
            ListGetStatusPayDTO listStatusPay = new ListGetStatusPayDTO();
            List<GetStatusPayDTO> getList = new ArrayList<>();

            for (int i = 0; i <statusIdDTO.getList().size() ; i++) {
                TblCheckPayOperations tblCheckPayOperations = tblCheckPayOperationsRepository.findForGetStatusPay(statusIdDTO.getList().get(i).getId());
                GetStatusPayDTO getStatusPayDTO = new GetStatusPayDTO();
                if(tblCheckPayOperations!= null) {
                    getStatusPayDTO.setStatus(tblCheckPayOperations.getStatus());
                    getStatusPayDTO.setId(tblCheckPayOperations.getTransactionId());
                    getStatusPayDTO.setIdAgent(tblCheckPayOperations.getIdAgent());
                    getStatusPayDTO.setErrCode(tblCheckPayOperations.getIdResult());

                }
                else {
                       getStatusPayDTO.setStatus(-1);
                       getStatusPayDTO.setId(statusIdDTO.getList().get(i).getId());
                       getStatusPayDTO.setIdAgent(1);
                       getStatusPayDTO.setErrCode(2000);
                }
                getList.add(getStatusPayDTO);
            }
            listStatusPay.setGetStatusPay(getList);

            return listStatusPay;
        }catch (Exception e){
            log.error("Error GetStatusPay",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in GetStatusPay method  DB: "+dbConfig.getDbName()
                    ,"\n Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }


    @Override
    public List<TransacIdDTO> checkIdTransactionForPay(){
        try {
            List<TblCheckPayOperations> tblCheckPayOperations = tblCheckPayOperationsRepository.findCheckTransactIdForPay();

            List<TransacIdDTO> transacIdDTOS = new ArrayList<>();
            for (int i = 0; i <tblCheckPayOperations.size() ; i++) {
                TransacIdDTO transacIdDTO = new TransacIdDTO();
                transacIdDTO.setId(tblCheckPayOperations.get(i).getTransactionId());
                transacIdDTO.setMethod(tblCheckPayOperations.get(i).getMethod());
                transacIdDTOS.add(transacIdDTO);
            }
            return  transacIdDTOS;
        }catch (Exception e){
            log.error("Error in checkIdTransactionForPay",e);
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in checkIdTransactionForPay method  DB: "+dbConfig.getDbName()
                    ,"\n Причина Ошибки :\n\n"+"\n"+e);
            return null;
        }

    }

    @Override
    public List<TblAllResultCodesDTO> getResultCodes() {
        List<TblResultCode> tblResultCodes = tblResultCodeRepository.findAll();

        List<TblAllResultCodesDTO> allResultCodes = new ArrayList<>();
        for (int i = 0; i <tblResultCodes.size() ; i++) {
            TblAllResultCodesDTO tblAllResultCodes = new TblAllResultCodesDTO();
            tblAllResultCodes.setId(tblResultCodes.get(i).getId());
            tblAllResultCodes.setText(tblResultCodes.get(i).getText());
            allResultCodes.add(tblAllResultCodes);
        }

        return allResultCodes;
    }
}
