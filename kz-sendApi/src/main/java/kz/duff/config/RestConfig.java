package kz.duff.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RestConfig {
    @Value("${core.url}")
    private  String URL;

}
