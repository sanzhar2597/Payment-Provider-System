package kz.ufo.config;

import kz.ufo.entity.TblSubagents;
import kz.ufo.repository.TblSprRepository;
import kz.ufo.repository.TblSubagentsRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import javax.print.DocFlavor;
import java.io.IOException;
import java.net.*;

@Data
@Configuration
public class RestConfigur {

    //------------Qiwi Variables
  /*  @Value("${qiwi.loginIB}")
    private String loginIB;

    @Value("${qiwi.loginIBCNP}")
    private String loginIBCNP;

    @Value("${qiwi.loginTNT}")
    private String loginTNT;


    private String passwordIB;

    private String passwordIBCNP;

    private String passwordTNT;*/

    @Value("${qiwi.securType}")
    private  String securType;

    @Value("${qiwi.software}")
    private  String software;

   /* @Value("${qiwi.term}")
    private  String term;

    @Value("${qiwi.passwordIB}")
    public void setPasswordIB(String value){
        passwordIB = DigestUtils.md5DigestAsHex(value.getBytes()).toLowerCase();
    }

    @Value("${qiwi.passwordIBCNP}")
    public void setPasswordIBCNP(String value){
        passwordIBCNP = DigestUtils.md5DigestAsHex(value.getBytes()).toLowerCase();
    }

    @Value("${qiwi.passwordTNT}")
    public void setPasswordTNT(String value){
        passwordTNT = DigestUtils.md5DigestAsHex(value.getBytes()).toLowerCase();
    }*/

    //----------------PayForm Variables
   /* @Value("${payForm.URL}")
    private String URL;

    @Value("${payForm.token}")
    private String token;*/

    @Value("${payForm.auth}")
    private String auth;


}
