package kz.ufo.config;


import kz.ufo.entity.TblSpr;
import kz.ufo.repository.TblSprRepository;
import lombok.Data;
import org.springframework.amqp.core.*;

import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;



@Configuration
@Data
public class RabbitMQConfig implements RabbitListenerConfigurer {

    @Autowired
    TblSprRepository tblSprRepository;
  /*  @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;

    @Value("${spring.rabbitmq.virtualHost}")
    private String virtualHost;*/

    public String getSprValueByCode(String code){
        TblSpr tblSpr ;
        tblSpr = tblSprRepository.findByCode(code);
        return tblSpr.getValue();
    }



    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory();

        connectionFactory.setHost(getSprValueByCode("RABBIT_HOST"));
        connectionFactory.setUsername(getSprValueByCode("RABBIT_USER"));
        connectionFactory.setPassword(getSprValueByCode("RABBIT_PASSWORD"));
        connectionFactory.setPort(Integer.parseInt(getSprValueByCode("RABBIT_PORT")));
        connectionFactory.setVirtualHost(getSprValueByCode("RABBIT_VHOST"));
        return connectionFactory;
    }
    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());

    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(consumerJackson2MessageConverter());
        return messageHandlerMethodFactory;
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }
    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }


    // Объявляем очередь
    // payment queue
    @Bean
    public Queue paymentQueue(){
        return QueueBuilder
                .durable("ppm.payment.queue")
                .deadLetterExchange("ppm.exchange")
                .deadLetterRoutingKey("payment.error")
                .build();
    }


    @Bean
    DirectExchange ppmExchange() {

        return ExchangeBuilder.directExchange("ppm.exchange").durable(true).build();
    }

    @Bean
    Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue()).to(ppmExchange()).with("payment");
    }


    // payment Send to Queue for IB
    @Bean
    public Queue paymentStatus(){
        return QueueBuilder
                .durable("ppm.paymentStatus.queue")
                .build();
    }

    @Bean
    DirectExchange ppmStatusExchange() {

        return ExchangeBuilder.directExchange("ppm.status.exchange").durable(true).build();
    }



    @Bean
    Binding paymentStatusBinding(){
        return  BindingBuilder.bind(paymentStatus()).to(ppmStatusExchange()).with("paymentStatus");
    }

    @Bean
    public Queue paymentStatusTNT(){
        return QueueBuilder
                .durable("ppm.paymentStatusTNT.queue")
                .build();
    }

    @Bean
    DirectExchange ppmStatusTNTExchange() {

        return ExchangeBuilder.directExchange("ppm.statusTNT.exchange").durable(true).build();
    }



    @Bean
    Binding paymentStatusTNTBinding(){
        return  BindingBuilder.bind(paymentStatusTNT()).to(ppmStatusTNTExchange()).with("paymentStatusTNT");
    }
    // payment exception queue
    @Bean
    public Queue paymentExceptionQueue(){
        return QueueBuilder
                .durable("ppm.payment.exception.queue")
                .build();
    }

    @Bean
    Binding paymentExceptionQueueBinding() {
        return BindingBuilder
                .bind(paymentExceptionQueue())
                .to(ppmExchange())
                .with("payment.error");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


}