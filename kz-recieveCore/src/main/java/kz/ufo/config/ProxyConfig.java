package kz.ufo.config;

import kz.ufo.entity.TblSpr;
import kz.ufo.repository.TblSprRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Data
public class ProxyConfig {

    @Autowired
    TblSprRepository tblSprRepository;

    @Value("${proxy.httpHostKey}")
    private String httpHostKey;

    @Value("${proxy.httpPortKey}")
    private String httpPortKey;

    @Value("${proxy.httpsHostKey}")
    private String httpsHostKey;

    @Value("${proxy.httpsPortKey}")
    private String httpsPortKey;

    @Value("${proxy.hostValue}")
    private String hostValue;

    @Value("${proxy.portValue}")
    private String portValue;


    public void setProxy(){
        /*TblSpr hostValue = new TblSpr();
        hostValue = tblSprRepository.findByCode("PROXY_HOST_VALUE");
        TblSpr portValue = new TblSpr();
        portValue = tblSprRepository.findByCode("PROXY_PORT_VALUE");*/

        System.setProperty(httpHostKey,hostValue);
        System.setProperty(httpPortKey,portValue);
        System.setProperty(httpsHostKey,hostValue);
        System.setProperty(httpsPortKey,portValue);
    }

    public void clearProxy(){
        System.clearProperty(httpHostKey);
        System.clearProperty(httpPortKey);
        System.clearProperty(httpsHostKey);
        System.clearProperty(httpsPortKey);
    }
}
