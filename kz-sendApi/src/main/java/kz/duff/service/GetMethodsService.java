package kz.duff.service;

import kz.duff.dto.*;

import java.math.BigInteger;
import java.util.List;

public interface GetMethodsService {
    List<TblProvidersAllDTO> getProviderList();


    GetProviderByPhoneDTO getProviderNameByPhone(PhoneProviderDTO phone);

    List<GetLogoDTO> getLogo();

    ListGetStatusDTO getStatusPay(ListStatusDTO statusIdDTO);
    List<TransacIdDTO> checkIdTransactionForPay();

    List<TblAllResultCodesDTO> getResultCodes();
}
