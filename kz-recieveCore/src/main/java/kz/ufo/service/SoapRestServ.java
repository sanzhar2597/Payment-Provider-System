package kz.ufo.service;

import kz.ufo.service.soap.SoapRequest;
import kz.ufo.service.soap.SoapResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.Charset;

@Component
@Slf4j
public class SoapRestServ {

    @Autowired
    RestTemplate restTemplate;

    @Value("${opm.service.url}")
    String opmUrl;
    @Value("${opm.service.user}")
    String opmUser;
    @Value("${opm.service.password}")
    String opmPassword;


    public SoapResponse request( SoapRequest request, String soapAction) {


        ResponseEntity<String> responseEntity = restTemplate.exchange(
                RequestEntity.post(URI.create(opmUrl))
                        .header("Content-Type", "application/xml; charset=UTF-8")
                        .header("Authorization", "Basic " + new String( Base64.encodeBase64((opmUser+":"+opmPassword).getBytes(Charset.forName("US-ASCII")) ) ))
                        .header("SOAPAction", soapAction)
                        .body(request.getBody()),
                String.class);
            log.error("BODY :" +responseEntity.getBody());
        return SoapResponse.builder().response(responseEntity.getBody()).build();
    }
}
