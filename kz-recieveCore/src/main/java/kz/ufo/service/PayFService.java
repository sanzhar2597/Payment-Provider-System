package kz.ufo.service;

import kz.ufo.dto.CheckServDTO;
import kz.ufo.dto.PayFTemplDTO;
import kz.ufo.dto.PayServDTO;

public interface PayFService {
    CheckServDTO checkService(PayFTemplDTO payFTemplDTO);
    PayServDTO payService(PayFTemplDTO payFTemplDTO);
    void getAndSavePayFormServices();

}
