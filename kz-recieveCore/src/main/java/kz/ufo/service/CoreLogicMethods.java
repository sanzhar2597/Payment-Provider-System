package kz.ufo.service;

import kz.ufo.dto.CheckServDTO;
import kz.ufo.dto.PayServDTO;
import kz.ufo.dto.PaymentDTO;
import kz.ufo.entity.TblCheckPayOperations;
import oracle.jdbc.OracleDatabaseException;

import java.sql.SQLException;
import java.util.List;

public interface CoreLogicMethods {
    CheckServDTO getProvidersInfo(PaymentDTO paymentDTO);
    CheckServDTO check(PaymentDTO paymentDTO) ;
    void   pay(PaymentDTO paymentDTO);

}
