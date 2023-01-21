package kz.ufo;



import kz.ufo.service.CoreLogicMethodsImpl;
import kz.ufo.service.PayFServiceImpl;
import kz.ufo.service.QiwiSoapServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.springframework.boot.Banner;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@EnableConfigurationProperties
@Slf4j
public class ServiceApplication {


    public static void main(String[] args) {
        SpringApplication.BANNER_LOCATION_PROPERTY.equals("src/main/resources");
        SpringApplication.BANNER_LOCATION_PROPERTY_VALUE.equals("banner.txt");
        SpringApplication.run(ServiceApplication.class, args);

    }



}
