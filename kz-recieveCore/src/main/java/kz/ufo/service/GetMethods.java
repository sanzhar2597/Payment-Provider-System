package kz.ufo.service;

import kz.ufo.dto.*;
import kz.ufo.entity.TblCheckPayOperations;
import kz.ufo.entity.TblProvidersBlob;


import java.math.BigInteger;
import java.util.List;

public interface GetMethods {
     List<TblProvidersAllDTO> getProviderList();

     GetProviderByPhoneDTO getProviderNameByPhone(PhoneProviderDTO phone);

     List<GetLogoDTO> getLogo();

     ListGetStatusPayDTO getStatusPay(ListStatusDTO statusIdDTO);

     List<TransacIdDTO> checkIdTransactionForPay();

     List<TblAllResultCodesDTO> getResultCodes();
}
