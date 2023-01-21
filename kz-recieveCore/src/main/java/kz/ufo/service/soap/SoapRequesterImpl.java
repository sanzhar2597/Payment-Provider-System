package kz.ufo.service.soap;



import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Date;

@Service
@Slf4j
public class SoapRequesterImpl implements SoapRequester {
    public Date reqTime;
    private final RestTemplate restTemplate;



    {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.8.54.68", 9090));
        requestFactory.setProxy(proxy);

        try{
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
        }catch (Exception e){
            log.error("Error SSL Congig",e);
        }


        restTemplate = new RestTemplateBuilder()
                .requestFactory(() -> requestFactory)
//                .basicAuthentication("10514218", "kPpqLTCJsa")
                .build();
    }

    @Override
    public SoapResponse request(SoapRequest request) {


        reqTime = new Date();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                RequestEntity.post(URI.create("https://xml1.qiwi.com/xmlgate/xml.jsp"))
                        .header("Content-Type", "text/xml; charset=utf-8")
                        .body(request.getBody()),
                String.class);

        return SoapResponse.builder().response(responseEntity.getBody()).build();

    }
}
