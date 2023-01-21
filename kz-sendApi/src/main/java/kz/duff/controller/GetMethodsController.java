package kz.duff.controller;

import kz.duff.dto.*;
import kz.duff.service.GetMethodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GetMethodsController {

    @Autowired
    GetMethodsService getProviderList;


    @PostMapping(value = "/providers" , produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TblProvidersAllDTO> showAllOpercodes(){

        List<TblProvidersAllDTO> allOperCodes = getProviderList.getProviderList();

        return allOperCodes;
    }


    @PostMapping(value = "/providerByPhone" , produces = MediaType.APPLICATION_JSON_VALUE)
    public GetProviderByPhoneDTO getProvNameByPhone(@RequestBody @Valid PhoneProviderDTO phone){

        return getProviderList.getProviderNameByPhone(phone);
    }

    @PostMapping(value = "/getLogo" , produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GetLogoDTO> getLogo(){

        List<GetLogoDTO> allLogo = getProviderList.getLogo();

        return allLogo;
    }

    @PostMapping(value = "/getStatusPay" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ListGetStatusDTO getStatusPay(@RequestBody @Valid ListStatusDTO statusIdDTO){
        ListGetStatusDTO status =  getProviderList.getStatusPay(statusIdDTO);

        return  status;
    }

    @PostMapping(value = "getTransactionIdForPay", produces = MediaType.APPLICATION_JSON_VALUE)
    public  List<TransacIdDTO> getTransactionIdForPay(){
        List<TransacIdDTO> tblCheckPayOperations = getProviderList.checkIdTransactionForPay();

        return tblCheckPayOperations;
    }

    @PostMapping(value = "/getResultCodes" , produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TblAllResultCodesDTO> getAllResultCodes(){

        List<TblAllResultCodesDTO> allResultCodes = getProviderList.getResultCodes();

        return allResultCodes;
    }

}
