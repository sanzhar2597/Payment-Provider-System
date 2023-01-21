package kz.ufo.controller;

import kz.ufo.dto.CheckServDTO;
import kz.ufo.dto.PayFTemplDTO;
import kz.ufo.dto.PayServDTO;
import kz.ufo.dto.PaymentDTO;
import kz.ufo.service.CoreLogicMethods;
import kz.ufo.service.PayFService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleDatabaseException;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api")
@Slf4j
public class MainController {

    @Autowired
    PayFService payFService;

    @Autowired
    CoreLogicMethods coreLogicMethods;

    @PostMapping(value = "/getProvidersInfo")
    @ResponseBody
    public CheckServDTO getProvidersInfo( @RequestBody PaymentDTO paymentDTO) {
        CheckServDTO checkServDTO = new CheckServDTO();
        try {
             checkServDTO = coreLogicMethods.getProvidersInfo(paymentDTO);
            return checkServDTO;
        }catch (Exception e){
            log.error("Error Controller GetProvidersInfo  "+e.getMessage());
            checkServDTO.setResultMessage(e.getMessage());
            return checkServDTO;
        }


    }

    @PostMapping(value = "/check")
    @ResponseBody
    public CheckServDTO check(@RequestBody  PaymentDTO paymentDTO) {
       CheckServDTO checkServDTO =new CheckServDTO();
        try {
             checkServDTO = coreLogicMethods.check(paymentDTO);
            return checkServDTO;

        }
        catch (Exception e){
    if(e.getMessage().indexOf("could not execute statement")!=-1){
        checkServDTO.setResult(20500);
        checkServDTO.setResultMessage("Транзакция существует с referenceNumber= "+paymentDTO.getReferenceNumber()+" и    systemName= "+paymentDTO.getSystemName());
    }
    else {
        checkServDTO.setResult(25000);
        checkServDTO.setResultMessage(e.getLocalizedMessage());
    }
            return checkServDTO;
        }
    }

  /*  @PostMapping("/services")
    @ResponseBody
    public String getServices() {

        payFService.getAndSavePayFormServices();
        return "payFService.getServices()";
    }


    @PostMapping(value = "/checkM")
    @ResponseBody
    public CheckServDTO payfCheck(@RequestBody PayFTemplDTO payFTemplDTO) {
        return payFService.checkService(payFTemplDTO);
    }


    @PostMapping(value = "/pay")
    @ResponseBody
    public PayServDTO payfPay(@RequestBody PayFTemplDTO payFTemplDTO) {
        return payFService.payService(payFTemplDTO);
    }
*/



@GetMapping(value = "/403")
 public  String accessDeniedPage(){

    return "403 Access Denied ";
}

}
