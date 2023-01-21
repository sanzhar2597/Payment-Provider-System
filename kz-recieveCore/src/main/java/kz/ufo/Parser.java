package kz.ufo;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kz.ufo.service.CoreLogicMethodsImpl;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class Parser {


    @Autowired
    CoreLogicMethodsImpl coreLogicMethods;

    @SneakyThrows
    public static void main(String[] args) {

String disp1="&lt;rs&gt;&lt;address&gt;Истомина Н.Н., Ауэзова 53 кв.3&lt;/address&gt;&lt;invoices&gt;&lt;invoice&gt;&lt;invoiceId /&gt;&lt;formedDate&gt;20230101000000&lt;/formedDate&gt;&lt;expireDate&gt;20230131235959&lt;/expireDate&gt;&lt;services&gt;&lt;service&gt;&lt;serviceId&gt;1&lt;/serviceId&gt;&lt;serviceName&gt;За энергию&lt;/serviceName&gt;&lt;fixSum&gt;14767.20&lt;/fixSum&gt;&lt;/service&gt;&lt;service&gt;&lt;serviceId&gt;110&lt;/serviceId&gt;&lt;serviceName&gt;ФАО Казахтелеком&lt;/serviceName&gt;&lt;fixSum&gt;2699.00&lt;/fixSum&gt;&lt;/service&gt;&lt;service&gt;&lt;serviceId&gt;160&lt;/serviceId&gt;&lt;serviceName&gt;ТОО &quot;Горгаз-сервис&quot;&lt;/serviceName&gt;&lt;fixSum&gt;1815.75&lt;/fixSum&gt;&lt;/service&gt;&lt;service&gt;&lt;serviceId&gt;1792&lt;/serviceId&gt;&lt;serviceName&gt;КСК &quot;Вегас&quot;&lt;/serviceName&gt;&lt;fixSum&gt;2648.00&lt;/fixSum&gt;&lt;/service&gt;&lt;service&gt;&lt;serviceId&gt;2&lt;/serviceId&gt;&lt;serviceName&gt;За установку ТПУ&lt;/serviceName&gt;&lt;fixSum&gt;415.20&lt;/fixSum&gt;&lt;/service&gt;&lt;service&gt;&lt;serviceId&gt;20&lt;/serviceId&gt;&lt;serviceName&gt;КызылжарСу&lt;/serviceName&gt;&lt;fixSum&gt;1314.48&lt;/fixSum&gt;&lt;/service&gt;&lt;service&gt;&lt;serviceId&gt;70&lt;/serviceId&gt;&lt;serviceName&gt;ТОО &quot;ЧИСТЫЙ ПЕТРОПАВЛОВСК&quot;&lt;/serviceName&gt;&lt;fixSum&gt;880.00&lt;/fixSum&gt;&lt;/service&gt;&lt;/services&gt;&lt;/invoice&gt;&lt;/invoices&gt;&lt;/rs&gt;";
        String htmlParse = HtmlUtils.htmlUnescape(disp1).replaceAll("\"\"","\"");


        JsonNode jsonNode = new XmlMapper().readTree(htmlParse);

        String jsonParse = new ObjectMapper().writeValueAsString(jsonNode);

        System.out.println(jsonParse);



    }
}

