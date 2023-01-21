package kz.ufo.service;

import kz.ufo.entity.TblSpr;
import kz.ufo.repository.TblSprRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
public class EmailServiceImpl  {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    TblSprRepository tblSprRepository;


    public String getSprValueByCode(String code){
        TblSpr tblSpr ;
        tblSpr = tblSprRepository.findByCode(code);
        return tblSpr.getValue();
    }
    public String[] sendEmailIds() {

        List<String> list1 = Arrays.asList(getSprValueByCode("EMAIL_NOTIF_OPM").replace(" ","").split(";"));
        String[] emailIds = new String[list1.size()];

        for (int i = 0; i <emailIds.length ; i++) {
            emailIds[i] = list1.get(i);
        }
        return emailIds;
    }
    public void sendSimpleMessage(
            String[] to, String subject, String text) {



        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ppm@homecredit.kz");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);

        log.info( "Email sended to: "+to);

    }

}
