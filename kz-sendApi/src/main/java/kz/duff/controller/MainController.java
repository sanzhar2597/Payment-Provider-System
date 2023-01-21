package kz.duff.controller;

import kz.duff.config.SecurityConfig;
import kz.duff.dto.*;
import kz.duff.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "api")
public class MainController {

    @Autowired
    MainService mainService;

    @Autowired
    SecurityConfig securityConfig;


    @PostMapping("/getProvidersInfo")
    @ResponseBody
    public CheckServDTO getProvidersInfo(@RequestBody @Valid GetRequestDTO paymentDTO){

        return mainService.getProvidersInfo(paymentDTO);
    }

    @PostMapping("/check")
    @ResponseBody
    public CheckServDTO check( @RequestBody @Valid  CheckRequestDTO paymentDTO){
        return mainService.checkMethod(paymentDTO);
    }

    @PostMapping("/pay")
    @ResponseBody
    public PayERDTO pay(@RequestBody  @Valid PaymentDTO paymentDTO){
       PayERDTO msg = mainService.payMethod(paymentDTO);
        return msg;
    }
}
