package kz.duff.service;

import kz.duff.dto.*;

public interface MainService {
    CheckServDTO getProvidersInfo(GetRequestDTO paymentDTO);
    CheckServDTO checkMethod(CheckRequestDTO paymentDTO);
    PayERDTO payMethod(PaymentDTO paymentDTO);
}
