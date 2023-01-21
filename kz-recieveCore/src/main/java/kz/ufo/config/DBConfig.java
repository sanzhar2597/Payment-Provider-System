package kz.ufo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Data
public class DBConfig {
    @Value("${spring.db.name}")
    private String dbName;
}
