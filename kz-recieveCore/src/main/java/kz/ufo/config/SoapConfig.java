package kz.ufo.config;


import org.springframework.context.annotation.Configuration;



public class SoapConfig {
   /* @Value("${opm.service.url}")
    String opmUrl;
    @Value("${opm.service.user}")
    String opmUser;
    @Value("${opm.service.password}")
    String opmPassword;
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("kz.ufo.service.opm");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller());
        webServiceTemplate.setUnmarshaller(marshaller());
        webServiceTemplate.setDefaultUri(opmUrl);

        // set a HttpComponentsMessageSender which provides support for basic authentication
        webServiceTemplate.setMessageSender(httpComponentsMessageSenderOpm());

        return webServiceTemplate;
    }

    @Bean
    public HttpComponentsMessageSender httpComponentsMessageSenderOpm() {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
        // set the basic authorization credentials
        httpComponentsMessageSender.setCredentials(usernamePasswordCredentialsOpm());

        return httpComponentsMessageSender;
    }

    @Bean
    public UsernamePasswordCredentials usernamePasswordCredentialsOpm() {
        return new UsernamePasswordCredentials(opmUser, opmPassword);
    }*/
}
