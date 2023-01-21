package kz.ufo.config;


import lombok.Data;


import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;


@Configuration
@EnableSwagger2
@Data
public class ApplicationConfig {

    @Autowired
    RestConfigur restConfigur;

  /*  @Bean
    @SneakyThrows
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.8.54.68", 9090));
        requestFactory.setProxy(proxy);
        URL weburl = new URL(restConfigur.getURL());
        HttpURLConnection webProxyConnection
                = (HttpURLConnection) weburl.openConnection(proxy);

        return new RestTemplate(requestFactory);

    }*/

    @Bean
    public RestTemplate restTemplate(){

        return new RestTemplate();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("kz.ufo.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo getApiInfo() {
        Contact contact = new Contact("Sanzhar Dadakhanov  ", "http://homecredit.kz", "Sanzhar.Dadakhanov2@homecredit.kz");
        return new ApiInfoBuilder()
                .title("Core")
                .description("PPM")
                .version("1.0.0")
                .contact(contact)
                .build();
    }
}