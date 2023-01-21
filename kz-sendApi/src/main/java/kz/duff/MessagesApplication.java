package kz.duff;

import kz.duff.dto.CheckRequestDTO;
import kz.duff.dto.CheckServDTO;
import kz.duff.dto.PaymentDTO;
import kz.duff.service.MainService;
import kz.duff.service.MainServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication

@Slf4j
public class MessagesApplication {

	public static void main(String[] args) {
		SpringApplication.BANNER_LOCATION_PROPERTY.equals("src/main/resources");
		SpringApplication.BANNER_LOCATION_PROPERTY_VALUE.equals("banner.txt");
		SpringApplication.run(MessagesApplication.class, args);
	}



}
