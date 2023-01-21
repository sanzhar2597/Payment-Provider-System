package kz.ufo.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kz.ufo.config.DBConfig;
import kz.ufo.config.RestConfigur;
import kz.ufo.dto.*;
import kz.ufo.dto.exstras.Exstras;
import kz.ufo.dto.exstras.Tariff;
import kz.ufo.dto.extrasERC.ExstrasERC;
import kz.ufo.dto.invoiceT.DataTempl;
import kz.ufo.dto.invoiceT.InvoiceTempl;
import kz.ufo.dto.invoiceT.InvoicesTempl;
import kz.ufo.dto.invoiceT.ServicesTempl;
import kz.ufo.entity.*;
import kz.ufo.repository.*;
import kz.ufo.service.soap.SoapRequest;
import kz.ufo.service.soap.SoapRequester;
import kz.ufo.service.soap.SoapRequesterImpl;
import kz.ufo.service.soap.SoapResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Slf4j
public class QiwiSoapServiceImpl {
    @Autowired
    SoapRequester soapRequester;

    @Autowired
    SoapRequesterImpl impl;

    @Autowired
    TblQiwiOperCodesRepository tblQiwiOperCodesRepository;


    @Autowired
    TblQiwiResultCodesRepository tblQiwiResultCodesRepository;

    @Autowired
    TblQiwiProviderExstraRepository tblQiwiProviderExstraRepository;

    @Autowired
    TblQiwiProviderParamRepository tblQiwiProviderParamRepository;



    @Autowired
    LogMethods logMethods;

    @Autowired
    RestConfigur restConfigur;


    @Autowired
    TblResultCodeRepository tblResultCodeRepository;

    @Autowired
    TblProviderDisplaysRepository tblProviderDisplaysRepository;

    @Autowired
    TblSubagentsRepository tblSubagentsRepository;

    @Autowired
    TblProvidersBlobRepository repository;

    @Autowired
    TblProviders2AgentsRepository tblProviders2AgentsRepository;

    @Autowired
    TblProviderFixPrice2AgentsRepository tblProviderFixPrice2AgentsRepository;

    @Autowired
    EmailServiceImpl emailService;

    @Autowired
    DBConfig dbConfig;

    @SneakyThrows
    @Transactional
    public void getAndSaveProviderList() {
        TblSubagents  tblSubagents = new TblSubagents();
        tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <getUIProviders/>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);

        SoapResponse response = soapRequester.request(req.build());


        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse()); // Log
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_code = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_text = xPath.compile("/response/text()").evaluate(parse, XPathConstants.STRING);
            Object method_resp_code = xPath.compile("/response/providers/getUIProviders/@result").evaluate(parse, XPathConstants.NUMBER);
            Object providers = xPath.compile("/response/providers/getUIProviders/provider").evaluate(parse, XPathConstants.NODESET);

            int getResCode  = ((Number) method_resp_code).intValue();

            if(getResCode==0) {
                tblQiwiOperCodesRepository.deleteAll();
                tblQiwiProviderExstraRepository.deleteAll();
                tblQiwiProviderParamRepository.deleteAll();
                log.info("Deleting old records");

                NodeList list = (NodeList) providers;
                for (int i = 0; i < list.getLength(); i++) {
                    Node provider = list.item(i);
                    NamedNodeMap attributes = provider.getAttributes();

                    String providerName = attributes.getNamedItem("sName").getNodeValue();
                    Long id = Long.valueOf(attributes.getNamedItem("id").getNodeValue());
                    String grpId = attributes.getNamedItem("grpId").getNodeValue();
                    String currId = attributes.getNamedItem("curId").getNodeValue();
                    Node logos = (Node) xPath.evaluate("logos/logo", provider, XPathConstants.NODE);
                    NamedNodeMap logAttr = logos.getAttributes();
                    String logoPath = logAttr.getNamedItem("path").getNodeValue();
                    String logoCrc = logAttr.getNamedItem("crc").getNodeValue();

                    TblQiwiOperCodes tblQiwiOperCodes = new TblQiwiOperCodes();
                    tblQiwiOperCodes.setId(id);
                    tblQiwiOperCodes.setName("name " + id);
                    tblQiwiOperCodes.setCode("code " + id);
                    tblQiwiOperCodes.setFullname(providerName);
                    tblQiwiOperCodes.setCrc(logoCrc);
                    tblQiwiOperCodes.setPathLogo(logoPath);
                    tblQiwiOperCodes.setCurrId(currId);
                    tblQiwiOperCodesRepository.save(tblQiwiOperCodes);

                    NodeList controls11 = (NodeList) xPath.evaluate("pages/page/controls/control", provider, XPathConstants.NODESET);

                    for (int controlIndex = 0; controlIndex < controls11.getLength(); controlIndex++) {
                        Node control = controls11.item(controlIndex);

                        NamedNodeMap controlAttributes = control.getAttributes();
                        Node typAttr = controlAttributes.getNamedItem("type");
                        String controlType = typAttr == null ? "" : typAttr.getNodeValue();

                        if (controlType.equals("disp_input")) {
                            Node nameAttr = controlAttributes.getNamedItem("name");
                            String name_ = nameAttr == null ? "" : nameAttr.getNodeValue();
                            Node dipAttr = controlAttributes.getNamedItem("disp_name");
                            String disp_name = dipAttr == null ? "" : dipAttr.getNodeValue();
                            Node headerAttr = controlAttributes.getNamedItem("header");
                            String header = headerAttr == null ? "" : headerAttr.getNodeValue();

                            // System.out.println("provider name :" + providerName + "    Extras: [ "+ name_ +"   "+ disp_name + "   "+header +" ]");
                            TblQiwiProviderExstra tblQiwiProviderExstra = new TblQiwiProviderExstra();
                            tblQiwiProviderExstra.setIdProvider(id);
                            tblQiwiProviderExstra.setName(name_);
                            tblQiwiProviderExstra.setDispName(disp_name);
                            tblQiwiProviderExstra.setHeader(header);
                            tblQiwiProviderExstraRepository.save(tblQiwiProviderExstra);
                        }
                        if (controlType.equals("text_input")) {
                            Node nameAttr = controlAttributes.getNamedItem("name");
                            String nameParam = nameAttr == null ? "" : nameAttr.getNodeValue();
                            Node headerAttr = controlAttributes.getNamedItem("header");
                            String headerParam = headerAttr == null ? "" : headerAttr.getNodeValue();

                            TblQiwiProviderParam tblQiwiProviderParam = new TblQiwiProviderParam();
                            tblQiwiProviderParam.setIdProvider(id);
                            tblQiwiProviderParam.setName(nameParam);
                            tblQiwiProviderParam.setHeader(headerParam);

                            tblQiwiProviderParamRepository.save(tblQiwiProviderParam);
                        }

                    }


                }
            } else {
                logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());
                throw new RuntimeException("Qiwi response incorrect!!!");
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse()); // Log
            emailService.sendSimpleMessage(emailService.sendEmailIds(), "Error in updateProviders Киви DB: "+dbConfig.getDbName()
                    ," Причина Ошибки :\n\n"+e);
        }


    }



    public String getLogin(PaymentDTO paymentDTO){
        TblSubagents tblSubagents = new TblSubagents();
        String login;
        if (paymentDTO.getSystemName().equals("IB")){
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
            login = tblSubagents.getLogin();
        }else if(paymentDTO.getSystemName().equals("IBCNP")){
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IBCNP_AUTH");
            login = tblSubagents.getLogin();
        } else if(paymentDTO.getSystemName().equals("TNT")){
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("TNT_AUTH");
            login = tblSubagents.getLogin();
        } else {
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("TNT_AUTH");
            login = tblSubagents.getLogin();
        }

        return login;
    }

    public String getPassword(PaymentDTO paymentDTO){
        TblSubagents tblSubagents = new TblSubagents();
        String password;
        if (paymentDTO.getSystemName().equals("IB")){
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
            password = DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase();
        }else if(paymentDTO.getSystemName().equals("IBCNP")){
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IBCNP_AUTH");
            password = DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase();
        } else if(paymentDTO.getSystemName().equals("TNT")){
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("TNT_AUTH");
            password = DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase();
        } else {
            tblSubagents = tblSubagentsRepository.findLoginPassbyCode("TNT_AUTH");
            password = DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase();
        }

        return password;
    }



    @SneakyThrows
    public CheckServDTO checkQiwiPayments(PaymentDTO paymentDTO) {

        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" +login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <checkPaymentRequisites>\n" +
                "              <payment id=\"" + paymentDTO.getId() + "\">\n" +
                "                   <from currency=\"" + paymentDTO.getCurrency() + "\" amount=\"" + paymentDTO.getAmount() + "\"/>\n" +
                "                   <to currency=\"" + paymentDTO.getCurrency() + "\" service=\"" + paymentDTO.getService() + "\" amount=\"" + paymentDTO.getAmount() + "\" account=\"" + paymentDTO.getAccount() + "\" />\n" +
                "                   <receipt id=\"" + paymentDTO.getId() + "\" date=\"" + new SimpleDateFormat("yyyy-MM-dd").format(paymentDTO.getDate()) + "\"/>\n" +
                "               </payment>\n" +
                "        </checkPaymentRequisites>\n" +
                "    </providers>\n" +
                "</request>";
        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);

        SoapResponse response = soapRequester.request(req.build());

        Integer resResult = -1;
        String descript = "";
        BigInteger id = BigInteger.ZERO;
        BigInteger uid = BigInteger.ZERO;
        String fatal="";
        CheckServDTO checkServDTO = new CheckServDTO();
        try {


            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_descr = xPath.compile("/response/@result-description").evaluate(parse, XPathConstants.STRING);
            Object response_result_date = xPath.compile("/response/providers/checkPaymentRequisites/payment/@date").evaluate(parse, XPathConstants.STRING);
            Object response_checkRes = xPath.compile("/response/providers/checkPaymentRequisites/payment/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResId = xPath.compile("/response/providers/checkPaymentRequisites/payment/@id").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResStatus = xPath.compile("/response/providers/checkPaymentRequisites/payment/@status").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_uid = xPath.compile("/response/providers/checkPaymentRequisites/payment/@uid").evaluate(parse, XPathConstants.NUMBER);
            Object response_exstras = xPath.compile("/response/providers/checkPaymentRequisites/payment/extras").evaluate(parse, XPathConstants.NODE);
            Object response_fatal = xPath.compile("/response/providers/checkPaymentRequisites/payment/@fatal").evaluate(parse, XPathConstants.STRING);

            resResult = ((Number) response_result).intValue();
            descript = response_result_descr.toString();
            String resDate = response_result_date.toString();
            Integer checkRes = ((Number) response_checkRes).intValue();
            Integer checkStatus = ((Number) response_checkResStatus).intValue();
            uid = BigInteger.valueOf(((Number) response_result_uid).longValue());
            id = BigInteger.valueOf(((Number) response_checkResId).longValue());

            fatal = response_fatal.toString();


            logMethods.wsLog(resResult, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            if (resResult == 0) {
                Map<String,String> dispMap  = new HashMap<>();
                Node exstras = (Node) response_exstras;
                NamedNodeMap exstraAttr = exstras.getAttributes();
                for (int i = 0; i < exstraAttr.getLength(); i++) {
                    Node disp1Atr = exstraAttr.item(i);
                    if(disp1Atr.getNodeName().startsWith("disp")){
                        TblProviderDisplays tblProviderDisplays =
                                tblProviderDisplaysRepository.findByProviderAgentCode(disp1Atr.getNodeName(),1,paymentDTO.getService());
                        if(tblProviderDisplays!=null) {
                            dispMap.put(tblProviderDisplays.getCode(), disp1Atr.getNodeValue());
                        }
                    }
                }
                // Для Алсеко
                InvoiceTempl invoiceTempl = new InvoiceTempl();

                List<InvoicesTempl> invoicesTemplList = new ArrayList<>();
                List<ServicesTempl> servicesTemplList = new ArrayList<>();
                InvoicesTempl invoicesTempl = new InvoicesTempl();
                invoiceTempl.setInvoices(invoicesTemplList);
                invoicesTemplList.add(invoicesTempl);
                invoicesTempl.setServices(servicesTemplList);
                if(paymentDTO.getService().equals("8147")){


                    Node disp1Atr = exstraAttr.getNamedItem("disp1");
                    String disp1 = disp1Atr == null ? "" : disp1Atr.getNodeValue();
                    if (!disp1.isEmpty()) {
                       // String htmlParse = HtmlUtils.htmlUnescape(disp1).replaceAll("\"\"","\"");

                        JsonNode jsonNode = new XmlMapper().readTree(disp1);

                        String jsonParse = new ObjectMapper().writeValueAsString(jsonNode);

                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        //objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
                        Exstras exstraObject = objectMapper.readValue(jsonParse, Exstras.class);

                        // Ответ
                        invoicesTempl.setInvoiceId(exstraObject.getAccount().getInvoices().getInvoice().getInvoiceId());
                        invoicesTempl.setClientName(exstraObject.getAccount().getName());
                        invoicesTempl.setClientAddress(exstraObject.getAccount().getAddress());
                        invoicesTempl.setExpireDate(exstraObject.getAccount().getInvoices().getInvoice().getExpireDate());
                        invoicesTempl.setFormedDate(exstraObject.getAccount().getInvoices().getInvoice().getFormedDate());

                        int sizeServices = exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().size();

                        for (int i = 0; i < sizeServices ; i++) {
                            ServicesTempl servicesTempl = new ServicesTempl();
                            DataTempl dataTempl = new DataTempl();
                            servicesTemplList.add(servicesTempl);

                            servicesTempl.setSubServiceName(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceName());
                            servicesTempl.setSubServiceId(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceId());

                            servicesTempl.setData(dataTempl);

                            String debtInfo = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                                                                                    .getServices().getService().get(i).getDebtInfo();
                            String lastCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                                                                                    .getServices().getService().get(i).getLastCount();
                            String prevCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                                                                                    .getServices().getService().get(i).getPrevCount();
                            String paySum = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum() == "" ?"0" : exstraObject.getAccount().getInvoices().getInvoice()
                                                                                                 .getServices().getService().get(i).getFixSum();
                            String isMeter = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService() == "" ?"" : exstraObject.getAccount().getInvoices().getInvoice()
                                                                                                            .getServices().getService().get(i).getIsCounterService();
                            String si  = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure()=="" ? "" : exstraObject.getAccount().getInvoices().getInvoice()
                                                                                                            .getServices().getService().get(i).getMeasure();

                            Tariff tariff = exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getTariff()== null ? new Tariff().setMaxTariffValue("0.0").setMinTariffValue("0.0")
                                    :exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getTariff();

                                String maxSum = tariff.getMaxTariffValue()==""?"0.0" : tariff.getMaxTariffValue();

                                String minSum = tariff.getMinTariffValue()==""?"0.0" :tariff.getMinTariffValue() ;
                                dataTempl.setMaxSum(Double.parseDouble(maxSum));
                                dataTempl.setMinSum(Double.parseDouble(minSum));

                            dataTempl.setDebtInfo(debtInfo);
                            dataTempl.setLastCount(lastCount);
                            dataTempl.setPrevCount(prevCount);
                            dataTempl.setPaySum(Double.parseDouble(paySum));
                            dataTempl.setIsMeter(Boolean.parseBoolean(isMeter));
                            dataTempl.setSi(si);

                        }

                    }


                } else if(paymentDTO.getService().equals("90287") || paymentDTO.getService().equals("82099")){

                    Node disp1Atr = exstraAttr.getNamedItem("disp1");
                    String disp1 = disp1Atr == null ? "" : disp1Atr.getNodeValue();
                    if (!disp1.isEmpty()) {
                        checkServDTO.setCurrencyRate(disp1);
                    }
                }
                else if(paymentDTO.getService().equals("6105") || paymentDTO.getService().equals("8146")){

                    Node disp1Atr = exstraAttr.getNamedItem("disp1");
                    String disp1 = disp1Atr == null ? "" : disp1Atr.getNodeValue();
                    if (!disp1.isEmpty()) {
                        // String htmlParse = HtmlUtils.htmlUnescape(disp1).replaceAll("\"\"","\"");

                        JsonNode jsonNode = new XmlMapper().readTree(disp1);

                        String jsonParse = new ObjectMapper().writeValueAsString(jsonNode);

                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        //objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
                        ExstrasERC exstraObject = objectMapper.readValue(jsonParse, ExstrasERC.class);

                        // Ответ
                        invoicesTempl.setInvoiceId(exstraObject.getAccount().getInvoices().getInvoice().getInvoiceId());
                        invoicesTempl.setClientName(exstraObject.getAccount().getName());
                        invoicesTempl.setClientAddress(exstraObject.getAccount().getAddress());
                        invoicesTempl.setExpireDate(exstraObject.getAccount().getInvoices().getInvoice().getExpireDate());
                        invoicesTempl.setFormedDate(exstraObject.getAccount().getInvoices().getInvoice().getFormedDate());

                        int sizeServices = exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().size();

                        for (int i = 0; i < sizeServices ; i++) {
                            ServicesTempl servicesTempl = new ServicesTempl();
                            DataTempl dataTempl = new DataTempl();
                            servicesTemplList.add(servicesTempl);

                            servicesTempl.setSubServiceName(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceName());
                            servicesTempl.setSubServiceId(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceId());

                            servicesTempl.setData(dataTempl);

                            String debtInfo = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo();
                            String lastCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount();
                            String prevCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount();
                            String paySum = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum() == "" ?"0" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum();
                            String isMeter = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService() == "" ?"" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService();
                            String si  = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure()=="" ? "" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure();

                            String tariff = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getTariff()==""?"": exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getTariff();
                            String comment = "";
                            String minSum = "0.0";
                            String maxSum = "0.0";

                            dataTempl.setDebtInfo(debtInfo);
                            dataTempl.setLastCount(lastCount);
                            dataTempl.setPrevCount(prevCount);
                            dataTempl.setPaySum(Double.parseDouble(paySum));
                            dataTempl.setIsMeter(Boolean.parseBoolean(isMeter));
                            dataTempl.setSi(si);
                            dataTempl.setTariff(Double.parseDouble(tariff));
                            dataTempl.setComment(comment);
                            dataTempl.setMaxSum(Double.parseDouble(maxSum));
                            dataTempl.setMinSum(Double.parseDouble(minSum));
                        }

                    }


                }

                // for fix sum
                TblProviders2Agents providers2Agents = tblProviders2AgentsRepository.findByCode(paymentDTO.getService());
                if(providers2Agents.getFixPrice()==1){
                    TblProviderFixPrice2Agents fixPrice2Agents = tblProviderFixPrice2AgentsRepository.findByIdProviderPartnerAndAgentId(paymentDTO.getService(),1);
                    Node disp1Atr = exstraAttr.getNamedItem(fixPrice2Agents.getCode());
                    String disp = disp1Atr == null ? "0.0":disp1Atr.getNodeValue();
                    checkServDTO.setFixedPrice(Double.parseDouble(disp));
                }

                TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkRes,1);

                checkServDTO.setResultMessage(resultCode.getText());
                checkServDTO.setCurrency("");
                checkServDTO.setResult(checkRes);
                checkServDTO.setTransactionId(uid);
                checkServDTO.setAgentTransactionId(id);
                checkServDTO.setStatus(checkStatus);
                checkServDTO.setFatal(fatal);
                if(paymentDTO.getService().equals("8147") || paymentDTO.getService().equals("6105") || paymentDTO.getService().equals("8146")){
                    checkServDTO.setDisplays(null);
                    checkServDTO.setInvoice(invoiceTempl);
                } else if(paymentDTO.getService().equals("90287") || paymentDTO.getService().equals("82099")){
                    checkServDTO.setDisplays(null);
                }
                else {
                    checkServDTO.setDisplays(dispMap);
                }
            }
            return checkServDTO;
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(resResult, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log

            return checkServDTO;
        }

    }


    @SneakyThrows
    public CheckServDTO checkQiwiExtrasPayments(PaymentDTO paymentDTO) {
        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);

        StringBuilder extraBody = new StringBuilder();
        extraBody.append("<extras ");
        for (Map.Entry<String, String> e : paymentDTO.getExtras().entrySet()) {
            extraBody
                    .append(e.getKey())
                    .append("=\"")
                    .append(e.getValue())
                    .append("\" ");
        }
        extraBody.append("/>");

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <checkPaymentRequisites>\n" +
                "              <payment id=\"" + paymentDTO.getId() + "\">\n" +
                "                   <from currency=\"" + paymentDTO.getCurrency() + "\" amount=\"" + paymentDTO.getAmount() + "\"/>\n" +
                "                   <to currency=\"" + paymentDTO.getCurrency() + "\" service=\"" + paymentDTO.getService() + "\" amount=\"" + paymentDTO.getAmount() + "\" account=\"" + paymentDTO.getAccount() + "\" />\n" +
                "                   <receipt id=\"" + paymentDTO.getId() + "\" date=\"" + "" + "\"/>\n" +
                "                  "+extraBody +"\n" +
                "               </payment>\n" +
                "        </checkPaymentRequisites>\n" +
                "    </providers>\n" +
                "</request>";
        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);

        SoapResponse response = soapRequester.request(req.build());

        Integer resResult = -1;
        String descript = "";
        BigInteger id = BigInteger.ZERO;
        BigInteger uid = BigInteger.ZERO;
        String fatal="";
        CheckServDTO checkServDTO = new CheckServDTO();
        try {


            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_descr = xPath.compile("/response/@result-description").evaluate(parse, XPathConstants.STRING);
            Object response_result_date = xPath.compile("/response/providers/checkPaymentRequisites/payment/@date").evaluate(parse, XPathConstants.STRING);
            Object response_checkRes = xPath.compile("/response/providers/checkPaymentRequisites/payment/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResId = xPath.compile("/response/providers/checkPaymentRequisites/payment/@id").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResStatus = xPath.compile("/response/providers/checkPaymentRequisites/payment/@status").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_uid = xPath.compile("/response/providers/checkPaymentRequisites/payment/@uid").evaluate(parse, XPathConstants.NUMBER);
            Object response_exstras = xPath.compile("/response/providers/checkPaymentRequisites/payment/extras").evaluate(parse, XPathConstants.NODE);
            Object response_fatal = xPath.compile("/response/providers/checkPaymentRequisites/payment/@fatal").evaluate(parse, XPathConstants.STRING);


            resResult = ((Number) response_result).intValue();
            descript = response_result_descr.toString();
            String resDate = response_result_date.toString();
            Integer checkRes = ((Number) response_checkRes).intValue();
            Integer checkStatus = ((Number) response_checkResStatus).intValue();
            uid = BigInteger.valueOf(((Number) response_result_uid).longValue());
            id = BigInteger.valueOf(((Number) response_checkResId).longValue());
            fatal = response_fatal.toString();

            logMethods.wsLog(resResult, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            if (resResult == 0) {
                Map<String,String> dispMap  = new HashMap<>();
                Node exstras = (Node) response_exstras;
                NamedNodeMap exstraAttr = exstras.getAttributes();
                for (int i = 0; i < exstraAttr.getLength(); i++) {
                    Node disp1Atr = exstraAttr.item(i);
                    if(disp1Atr.getNodeName().startsWith("disp")){

                        TblProviderDisplays tblProviderDisplays =
                                tblProviderDisplaysRepository.findByProviderAgentCode(disp1Atr.getNodeName(),1,paymentDTO.getService());

                        if(tblProviderDisplays!=null) {
                            dispMap.put(tblProviderDisplays.getCode(), disp1Atr.getNodeValue());
                        }

                    }

                }

                // Для Алсеко
                InvoiceTempl invoiceTempl = new InvoiceTempl();

                List<InvoicesTempl> invoicesTemplList = new ArrayList<>();
                List<ServicesTempl> servicesTemplList = new ArrayList<>();
                InvoicesTempl invoicesTempl = new InvoicesTempl();
                invoiceTempl.setInvoices(invoicesTemplList);
                invoicesTemplList.add(invoicesTempl);
                invoicesTempl.setServices(servicesTemplList);
                if(paymentDTO.getService().equals("8147")){


                    Node disp1Atr = exstraAttr.getNamedItem("disp1");
                    String disp1 = disp1Atr == null ? "" : disp1Atr.getNodeValue();
                    if (!disp1.isEmpty()) {
                        // String htmlParse = HtmlUtils.htmlUnescape(disp1).replaceAll("\"\"","\"");

                        JsonNode jsonNode = new XmlMapper().readTree(disp1);

                        String jsonParse = new ObjectMapper().writeValueAsString(jsonNode);

                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        //objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
                        Exstras exstraObject = objectMapper.readValue(jsonParse, Exstras.class);

                        // Ответ
                        invoicesTempl.setInvoiceId(exstraObject.getAccount().getInvoices().getInvoice().getInvoiceId());
                        invoicesTempl.setClientName(exstraObject.getAccount().getName());
                        invoicesTempl.setClientAddress(exstraObject.getAccount().getAddress());
                        invoicesTempl.setExpireDate(exstraObject.getAccount().getInvoices().getInvoice().getExpireDate());
                        invoicesTempl.setFormedDate(exstraObject.getAccount().getInvoices().getInvoice().getFormedDate());

                        int sizeServices = exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().size();

                        for (int i = 0; i < sizeServices ; i++) {
                            ServicesTempl servicesTempl = new ServicesTempl();
                            DataTempl dataTempl = new DataTempl();
                            servicesTemplList.add(servicesTempl);

                            servicesTempl.setSubServiceName(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceName());
                            servicesTempl.setSubServiceId(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceId());

                            servicesTempl.setData(dataTempl);

                            String debtInfo = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo();
                            String lastCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount();
                            String prevCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount();
                            String paySum = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum() == "" ?"0" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum();
                            String isMeter = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService() == "" ?"" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService();
                            String si  = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure()=="" ? "" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure();

                            Tariff tariff = exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getTariff()== null ? new Tariff().setMaxTariffValue("0.0").setMinTariffValue("0.0")
                                    :exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getTariff();

                            String maxSum = tariff.getMaxTariffValue()==""?"0.0" : tariff.getMaxTariffValue();

                            String minSum = tariff.getMinTariffValue()==""?"0.0" :tariff.getMinTariffValue() ;
                            dataTempl.setMaxSum(Double.parseDouble(maxSum));
                            dataTempl.setMinSum(Double.parseDouble(minSum));

                            dataTempl.setDebtInfo(debtInfo);
                            dataTempl.setLastCount(lastCount);
                            dataTempl.setPrevCount(prevCount);
                            dataTempl.setPaySum(Double.parseDouble(paySum));
                            dataTempl.setIsMeter(Boolean.parseBoolean(isMeter));
                            dataTempl.setSi(si);

                        }

                    }


                }
                if(paymentDTO.getService().equals("6105") || paymentDTO.getService().equals("8146") ){

                    Node disp1Atr = exstraAttr.getNamedItem("disp1");
                    String disp1 = disp1Atr == null ? "" : disp1Atr.getNodeValue();
                    if (!disp1.isEmpty()) {
                        // String htmlParse = HtmlUtils.htmlUnescape(disp1).replaceAll("\"\"","\"");

                        JsonNode jsonNode = new XmlMapper().readTree(disp1);

                        String jsonParse = new ObjectMapper().writeValueAsString(jsonNode);

                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                        //objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
                        ExstrasERC exstraObject = objectMapper.readValue(jsonParse, ExstrasERC.class);

                        // Ответ
                        invoicesTempl.setInvoiceId(exstraObject.getAccount().getInvoices().getInvoice().getInvoiceId());
                        invoicesTempl.setClientName(exstraObject.getAccount().getName());
                        invoicesTempl.setClientAddress(exstraObject.getAccount().getAddress());
                        invoicesTempl.setExpireDate(exstraObject.getAccount().getInvoices().getInvoice().getExpireDate());
                        invoicesTempl.setFormedDate(exstraObject.getAccount().getInvoices().getInvoice().getFormedDate());

                        int sizeServices = exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().size();

                        for (int i = 0; i < sizeServices ; i++) {
                            ServicesTempl servicesTempl = new ServicesTempl();
                            DataTempl dataTempl = new DataTempl();
                            servicesTemplList.add(servicesTempl);

                            servicesTempl.setSubServiceName(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceName());
                            servicesTempl.setSubServiceId(exstraObject.getAccount().getInvoices().getInvoice().getServices().getService().get(i).getServiceId());

                            servicesTempl.setData(dataTempl);

                            String debtInfo = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getDebtInfo();
                            String lastCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getLastCount();
                            String prevCount = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount() == "" ?"" :exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getPrevCount();
                            String paySum = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum() == "" ?"0" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getFixSum();
                            String isMeter = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService() == "" ?"" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getIsCounterService();
                            String si  = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure()=="" ? "" : exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getMeasure();

                            String tariff = exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getTariff()==""?"": exstraObject.getAccount().getInvoices().getInvoice()
                                    .getServices().getService().get(i).getTariff();
                            String comment = "";

                            String minSum = "0.0";
                            String maxSum ="0.0";

                            dataTempl.setDebtInfo(debtInfo);
                            dataTempl.setLastCount(lastCount);
                            dataTempl.setPrevCount(prevCount);
                            dataTempl.setPaySum(Double.parseDouble(paySum));
                            dataTempl.setIsMeter(Boolean.parseBoolean(isMeter));
                            dataTempl.setSi(si);
                            dataTempl.setTariff(Double.parseDouble(tariff));
                            dataTempl.setComment(comment);
                            dataTempl.setMaxSum(Double.parseDouble(maxSum));
                            dataTempl.setMinSum(Double.parseDouble(minSum));
                        }

                    }


                }
                // for fix sum
                TblProviders2Agents providers2Agents = tblProviders2AgentsRepository.findByCode(paymentDTO.getService());
                if(providers2Agents.getFixPrice()==1){
                    TblProviderFixPrice2Agents fixPrice2Agents = tblProviderFixPrice2AgentsRepository.findByIdProviderPartnerAndAgentId(paymentDTO.getService(),1);
                    Node disp1Atr = exstraAttr.getNamedItem(fixPrice2Agents.getCode());
                    String disp = disp1Atr == null ? "0.0":disp1Atr.getNodeValue();
                    checkServDTO.setFixedPrice(Double.parseDouble(disp));
                }
                TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkRes,1);
               // TblQiwiResultCode resultCode = tblQiwiResultCodesRepository.findByCode(Long.valueOf(checkRes));


                checkServDTO.setResultMessage(resultCode.getText());
                checkServDTO.setCurrency(paymentDTO.getCurrency());
                checkServDTO.setResult(checkRes);
                checkServDTO.setTransactionId(uid);
                checkServDTO.setAgentTransactionId(id);
                checkServDTO.setStatus(checkStatus);
                if(paymentDTO.getService().equals("8147")||paymentDTO.getService().equals("6105") || paymentDTO.getService().equals("8146")){
                    checkServDTO.setDisplays(null);
                    checkServDTO.setInvoice(invoiceTempl);
                }else {
                    checkServDTO.setDisplays(dispMap);
                }

                checkServDTO.setFatal(fatal);
            }
            return checkServDTO;

        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(resResult, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            checkServDTO.setResult(resResult);
            checkServDTO.setResultMessage("Exception ERROR");
            return checkServDTO;
        }

    }


    @SneakyThrows
    public PayServDTO authQiwiPayment(PaymentDTO paymentDTO) {

        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <authorizePayment>\n" +
                "              <payment id=\"" + paymentDTO.getId() + "\">\n" +
                "                   <from currency=\"" + paymentDTO.getCurrency() + "\" amount=\"" + paymentDTO.getAmount() + "\"/>\n" +
                "                   <to currency=\"" + paymentDTO.getCurrency() + "\" service=\"" + paymentDTO.getService() + "\" amount=\"" + paymentDTO.getAmount() + "\" account=\"" + paymentDTO.getAccount() + "\" />\n" +
                "                   <receipt id=\"" + paymentDTO.getId() + "\" date=\"" + new SimpleDateFormat("yyyy-MM-dd").format(paymentDTO.getDate()) + "\"/>\n" +
                "               </payment>\n" +
                "        </authorizePayment>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());
        PayServDTO payServDTO = new PayServDTO();

        Integer resResult = -1;
        String descript = "";
        BigInteger id = BigInteger.ZERO;
        BigInteger uid = BigInteger.ZERO;
        String fatal="";
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_date = xPath.compile("/response/providers/authorizePayment/payment/@date").evaluate(parse, XPathConstants.STRING);
            Object response_checkRes = xPath.compile("/response/providers/authorizePayment/payment/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResId = xPath.compile("/response/providers/authorizePayment/payment/@id").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResStatus = xPath.compile("/response/providers/authorizePayment/payment/@status").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_uid = xPath.compile("/response/providers/authorizePayment/payment/@uid").evaluate(parse, XPathConstants.NUMBER);
            Object response_exstras = xPath.compile("/response/providers/authorizePayment/payment/extras").evaluate(parse, XPathConstants.NODE);
            Object response_fatal = xPath.compile("/response/providers/checkPaymentRequisites/payment/@fatal").evaluate(parse, XPathConstants.STRING);

            resResult = ((Number) response_result).intValue();
            String resDate = response_result_date.toString();
            Integer checkRes = ((Number) response_checkRes).intValue();
            Integer checkStatus = ((Number) response_checkResStatus).intValue();
            uid = BigInteger.valueOf(((Number) response_result_uid).longValue());
            id = BigInteger.valueOf(((Number) response_checkResId).longValue());
            fatal = response_fatal.toString();

            logMethods.wsLog(resResult, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            if (resResult == 0) {

                Map<String,String> dispMap  = new HashMap<>();

                Node exstras = (Node) response_exstras;
                NamedNodeMap exstraAttr = exstras.getAttributes();
                for (int i = 0; i < exstraAttr.getLength(); i++) {
                    Node disp1Atr = exstraAttr.item(i);
                    if(disp1Atr.getNodeName().startsWith("disp")){

                        dispMap.put(disp1Atr.getNodeName(),disp1Atr.getNodeValue());

                    }

                }
                TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkRes,1);

                payServDTO.setResultMessage(resultCode.getText());
                payServDTO.setCurrency(paymentDTO.getCurrency());
                payServDTO.setResult(checkRes);
                payServDTO.setTransactionId(uid);
                payServDTO.setAgentTransactionId(id);
                payServDTO.setStatus(checkStatus);
                payServDTO.setDisplays(dispMap);
                payServDTO.setFatal(fatal);
            }

            return payServDTO;
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());// Log
        }
        return null;

    }

    @SneakyThrows
    public PayServDTO authQiwiExtrasPayment(PaymentDTO paymentDTO) {

        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);

        StringBuilder extraBody = new StringBuilder();
        extraBody.append("<extras ");
        for (Map.Entry<String, String> e : paymentDTO.getExtras().entrySet()) {
            extraBody
                    // .append("\"")
                    .append(e.getKey())
                    .append("=\"")
                    .append(e.getValue())
                    .append("\" ");
        }
        extraBody.append("/>");

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <authorizePayment>\n" +
                "              <payment id=\"" + paymentDTO.getId() + "\">\n" +
                "                   <from currency=\"" + paymentDTO.getCurrency() + "\" amount=\"" + paymentDTO.getAmount() + "\"/>\n" +
                "                   <to currency=\"" + paymentDTO.getCurrency() + "\" service=\"" + paymentDTO.getService() + "\" amount=\"" + paymentDTO.getAmount() + "\" account=\"" + paymentDTO.getAccount() + "\" />\n" +
                "                   <receipt id=\"" + paymentDTO.getId() + "\" date=\"" + new SimpleDateFormat("yyyy-MM-dd").format(paymentDTO.getDate()) + "\"/>\n" +
                "                  "+extraBody+"\n"+
                "               </payment>\n" +
                "        </authorizePayment>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        Integer resResult = -1;
        BigInteger id = BigInteger.ZERO;
        BigInteger uid = BigInteger.ZERO;
        String fatal="";
        PayServDTO payServDTO = new PayServDTO();
        try {

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();

            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_date = xPath.compile("/response/providers/authorizePayment/payment/@date").evaluate(parse, XPathConstants.STRING);
            Object response_checkRes = xPath.compile("/response/providers/authorizePayment/payment/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResId = xPath.compile("/response/providers/authorizePayment/payment/@id").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResStatus = xPath.compile("/response/providers/authorizePayment/payment/@status").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_uid = xPath.compile("/response/providers/authorizePayment/payment/@uid").evaluate(parse, XPathConstants.NUMBER);
            Object response_exstras = xPath.compile("/response/providers/authorizePayment/payment/extras").evaluate(parse, XPathConstants.NODE);
            Object response_fatal = xPath.compile("/response/providers/checkPaymentRequisites/payment/@fatal").evaluate(parse, XPathConstants.STRING);


            resResult = ((Number) response_result).intValue();
            String resDate = response_result_date.toString();
            Integer checkRes = ((Number) response_checkRes).intValue();
            Integer checkStatus = ((Number) response_checkResStatus).intValue();
            uid = BigInteger.valueOf(((Number) response_result_uid).longValue());
            id = BigInteger.valueOf(((Number) response_checkResId).longValue());
            fatal = response_fatal.toString();

            logMethods.wsLog(resResult, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            if (resResult == 0) {

                Map<String,String> dispMap  = new HashMap<>();

                Node exstras = (Node) response_exstras;
                NamedNodeMap exstraAttr = exstras.getAttributes();
                for (int i = 0; i < exstraAttr.getLength(); i++) {
                    Node disp1Atr = exstraAttr.item(i);
                    if(disp1Atr.getNodeName().startsWith("disp")){

                        dispMap.put(disp1Atr.getNodeName(),disp1Atr.getNodeValue());

                    }

                }
                TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkRes,1);

                payServDTO.setResultMessage(resultCode.getText());
                payServDTO.setCurrency(paymentDTO.getCurrency());
                payServDTO.setResult(checkRes);
                payServDTO.setTransactionId(uid);
                payServDTO.setAgentTransactionId(id);
                payServDTO.setStatus(checkStatus);
                payServDTO.setDisplays(dispMap);
                payServDTO.setFatal(fatal);
            }
            return payServDTO;
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());// Log
        }
        return null;

    }

    @SneakyThrows
    public PayServDTO confirmQiwiPayment(PaymentDTO paymentDTO) {

        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <confirmPayment>\n" +
                "              <payment id=\"" + paymentDTO.getId() + "\"/>\n" +
                "        </confirmPayment>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());
        PayServDTO payServDTO = new PayServDTO();
        Integer resResult = -1;
        BigInteger id = BigInteger.ZERO;
        BigInteger uid = BigInteger.ZERO;
        String fatal="";
        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse()); // Log
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkRes = xPath.compile("/response/providers/confirmPayment/payment/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResId = xPath.compile("/response/providers/confirmPayment/payment/@id").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_uid = xPath.compile("/response/providers/confirmPayment/payment/@uid").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResStatus = xPath.compile("/response/providers/confirmPayment/payment/@status").evaluate(parse, XPathConstants.NUMBER);
            Object response_fatal = xPath.compile("/response/providers/checkPaymentRequisites/payment/@fatal").evaluate(parse, XPathConstants.STRING);

            resResult = ((Number) response_result).intValue();
            Integer checkRes = ((Number) response_checkRes).intValue();
            Integer checkStatus = ((Number) response_checkResStatus).intValue();
            uid = BigInteger.valueOf(((Number) response_result_uid).longValue());
            id = BigInteger.valueOf(((Number) response_checkResId).longValue());
            fatal = response_fatal.toString();


            if (resResult == 0) {
                TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkRes,1);

                if (checkRes == 0) {
                    payServDTO.setResultMessage("Успешно");
                } else {
                    payServDTO.setResultMessage(resultCode.getText());
                }
                payServDTO.setCurrency(paymentDTO.getCurrency());
                payServDTO.setResult(checkRes);
                payServDTO.setTransactionId(uid);
                payServDTO.setAgentTransactionId(id);
                payServDTO.setStatus(checkStatus);
                payServDTO.setFatal(fatal);
            }
            return payServDTO;
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse()); // Log
        }
return null;
    }

    @SneakyThrows
    public PayServDTO addQiwiOfflinePayment(PaymentDTO paymentDTO) {

        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);


        String invoice = paymentDTO.getExtras().get("invoiceId");
        StringBuilder extraServicesBody = new StringBuilder();
         String doubleDot;
        for (int i = 0; i < paymentDTO.getExtraServices().size(); i++) {

            if (i== paymentDTO.getExtraServices().size()-1){
                 doubleDot="";
            }else {
                 doubleDot="::";
            }

            extraServicesBody
                    .append(paymentDTO.getExtraServices().get(i).getSubServiceId())
                    .append(";")
                    .append("0;")
                    .append("0;")
                    .append(paymentDTO.getExtraServices().get(i).getAmount())
                    .append(doubleDot);


        }
        
        StringBuilder extraBody = new StringBuilder();
        extraBody.append("<extras ");

            extraBody

                    .append("ev_account1")
                    .append("=\"")
                    .append(paymentDTO.getAccount())
                    .append("::").append(invoice)
                    .append("::").append(extraServicesBody)
                    .append("\" ");

        extraBody.append("/>");

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <addOfflinePayment>\n" +
                "              <payment id=\"" + paymentDTO.getId() + "\">\n" +
                "                   <from currency=\"" + paymentDTO.getCurrency() + "\" amount=\"" + paymentDTO.getAmount() + "\"/>\n" +
                "                   <to currency=\"" + paymentDTO.getCurrency() + "\" service=\"" + paymentDTO.getService() + "\" amount=\"" + paymentDTO.getAmount() + "\" account=\"" + paymentDTO.getAccount() + "\" />\n" +
                "                   <receipt id=\"" + paymentDTO.getId() + "\" date=\"" + new SimpleDateFormat("yyyy-MM-dd").format(paymentDTO.getDate()) + "\"/>\n" +
                "                  "+extraBody+"\n"+
                "               </payment>\n" +
                "        </addOfflinePayment>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        Integer resResult = -1;
        BigInteger id = BigInteger.ZERO;
        BigInteger uid = BigInteger.ZERO;
        String fatal="";
        PayServDTO payServDTO = new PayServDTO();
        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());// Log

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();

            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_date = xPath.compile("/response/providers/addOfflinePayment/payment/@date").evaluate(parse, XPathConstants.STRING);
            Object response_checkRes = xPath.compile("/response/providers/addOfflinePayment/payment/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResId = xPath.compile("/response/providers/addOfflinePayment/payment/@id").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkResStatus = xPath.compile("/response/providers/addOfflinePayment/payment/@status").evaluate(parse, XPathConstants.NUMBER);
            Object response_result_uid = xPath.compile("/response/providers/addOfflinePayment/payment/@uid").evaluate(parse, XPathConstants.NUMBER);
            //Object response_exstras = xPath.compile("/response/providers/addOfflinePayment/payment/extras").evaluate(parse, XPathConstants.NODE);
            Object response_fatal = xPath.compile("/response/providers/addOfflinePayment/payment/@fatal").evaluate(parse, XPathConstants.STRING);


            resResult = ((Number) response_result).intValue();
            String resDate = response_result_date.toString();
            Integer checkRes = ((Number) response_checkRes).intValue();
            Integer checkStatus = ((Number) response_checkResStatus).intValue();
            uid = BigInteger.valueOf(((Number) response_result_uid).longValue());
            id = BigInteger.valueOf(((Number) response_checkResId).longValue());
            fatal = response_fatal.toString();

            if (resResult == 0) {

                TblResultCode resultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(checkRes,1);

                payServDTO.setResultMessage(resultCode.getText());
                payServDTO.setCurrency(paymentDTO.getCurrency());
                payServDTO.setResult(checkRes);
                payServDTO.setTransactionId(uid);
                payServDTO.setAgentTransactionId(id);
                payServDTO.setStatus(checkStatus);
                payServDTO.setFatal(fatal);
            }
            return payServDTO;
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());// Log
        }
        return null;

    }

    // Не до конца
    @SneakyThrows
    public List<GetQiwiPaymentStatusDTO> getQiwiPaymentStatus(List<TblCheckPayOperations> tblCheckPayOperations,PaymentDTO paymentDTO) {
        String login = getLogin(paymentDTO);
        String password = getPassword(paymentDTO);
        StringBuilder paymentIds = new StringBuilder();
        for (int i = 0; i <tblCheckPayOperations.size() ; i++) {
            paymentIds.append("             <payment id=\"")
                      .append(tblCheckPayOperations.get(i).getTransactionId())
                      .append("\"/>\n");
        }

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + login + "\"  sign=\"" + password + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + login + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <getPaymentStatus>\n" +
                             paymentIds+
                "\n        </getPaymentStatus>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());
        List<GetQiwiPaymentStatusDTO> listPaymentStatus = new ArrayList<>();
        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());  // Log

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_checkRes = xPath.compile("/response/providers/getPaymentStatus/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_payments = xPath.compile("/response/providers/getPaymentStatus/payment").evaluate(parse, XPathConstants.NODESET);
          //  Object response_checkResId = xPath.compile("/response/providers/getPaymentStatus/payment/@id").evaluate(parse, XPathConstants.NUMBER);
          //  Object response_checkResStatus = xPath.compile("/response/providers/getPaymentStatus/payment/@status").evaluate(parse, XPathConstants.NUMBER);
          //  Object response_PayStatRes = xPath.compile("/response/providers/getPaymentStatus/payment/@result").evaluate(parse, XPathConstants.NUMBER);

            Integer respRes = ((Number) response_result).intValue();
     //       Integer checkStatus = ((Number) response_checkResStatus).intValue();
      //      Integer payStatRes =((Number) response_PayStatRes).intValue();

            if(respRes==0) {



                NodeList list = (NodeList) response_payments;
                for (int i = 0; i < list.getLength(); i++) {
                    Node payments = list.item(i);
                    NamedNodeMap attributes = payments.getAttributes();
                    GetQiwiPaymentStatusDTO getQiwiPaymentStatusDTO = new GetQiwiPaymentStatusDTO();

                    Integer paymentId = Integer.parseInt(attributes.getNamedItem("id").getNodeValue());
                    Integer paymentResult = Integer.parseInt(attributes.getNamedItem("result").getNodeValue());
                    Integer paymentStatus = Integer.parseInt(attributes.getNamedItem("status").getNodeValue());

                    TblResultCode tblResultCode = tblResultCodeRepository.findByCodeErrorAndAgentId(paymentResult, 1);
                    getQiwiPaymentStatusDTO.setStatus(paymentStatus);
                    getQiwiPaymentStatusDTO.setResult(paymentResult);
                    getQiwiPaymentStatusDTO.setResultMessage(tblResultCode.getText());
                    getQiwiPaymentStatusDTO.setId(paymentId);
                    listPaymentStatus.add(getQiwiPaymentStatusDTO);
                }
            }


        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());  // Log
        }
        return listPaymentStatus;

    }

    @SneakyThrows
    public void getQiwiResultCodes() {
        TblSubagents  tblSubagents = new TblSubagents();
        tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <system>\n" +
                "       <getResultCodes/>\n" +
                "    </system>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        try {


            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_resultCode = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_resultCodes = xPath.compile("/response/system/getResultCodes/row").evaluate(parse, XPathConstants.NODESET);

            NodeList list = (NodeList) response_resultCodes;

            for (int i = 0; i < list.getLength(); i++) {

                Node item = list.item(i);
                NamedNodeMap attributes = item.getAttributes();

                String errId = attributes.getNamedItem("err_id").getNodeValue();
                String errMess = attributes.getNamedItem("err_text").getNodeValue();

                TblQiwiResultCode tblQiwiResultCode = new TblQiwiResultCode();

                tblQiwiResultCode.setCode(Long.valueOf(errId));
                tblQiwiResultCode.setText(errMess);

                tblQiwiResultCodesRepository.save(tblQiwiResultCode);

               //System.out.println("Err id:  " + errId + "    Err Message:  " + errMess);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
        }


    }

    @SneakyThrows
    public void getUIGroups() {
        TblSubagents  tblSubagents = new TblSubagents();
        tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "       <getUIGroups/>\n" +
                "    </providers>\n" +
                "</request>";


        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_resultCode = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_resultCodes = xPath.compile("/response/providers/getUIGroups/group").evaluate(parse, XPathConstants.NODESET);


            NodeList list = (NodeList) response_resultCodes;

            for (int i = 0; i < list.getLength(); i++) {
                // Object response_resultProv = xPath.compile("/response/providers/getUIGroups/group/provider").evaluate(parse, XPathConstants.NODESET);
                Node item = list.item(i);
                NamedNodeMap attributes = item.getAttributes();

                String id = attributes.getNamedItem("id").getNodeValue();
                String name = attributes.getNamedItem("name").getNodeValue();
                System.out.println("id: " + id + "     name: " + name);
                //  ArrayList<String> arrayList = new ArrayList<>();

                printItem(xPath, item, name);

            }
        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
        }


    }

    private void printItem(XPath xPath, Node item, String name) throws XPathExpressionException {
        NodeList providers = (NodeList) xPath.evaluate("provider", item, XPathConstants.NODESET);

        for (int j = 0; j < providers.getLength(); j++) {

            Node provider = providers.item(j);

            NamedNodeMap providerAttributes = provider.getAttributes();

            String idProv = providerAttributes.getNamedItem("id").getNodeValue();
            Node tag = providerAttributes.getNamedItem("tag");
            String tagN = tag == null ? "" : tag.getNodeValue();

            System.out.println("name: " + name + "   tagId: " + idProv + "   tagN: " + tagN);
        }

        NodeList groups = (NodeList) xPath.evaluate("group", item, XPathConstants.NODESET);

        for (int i = 0; i < groups.getLength(); i++) {
            Node innerItem = groups.item(0);
            printItem(xPath, innerItem, name);
        }

    }

    private void collectGroups(Node group, List<String> groupIds) {
        String groupId = group.getAttributes().getNamedItem("id").getNodeValue();
//        System.out.println("groupId = " + groupId);
        groupIds.add(groupId);

        NodeList child = group.getChildNodes();

        for (int childIndex = 0; childIndex < child.getLength(); childIndex++) {
            Node node = child.item(childIndex);

            if (node.getNodeName().equals("group")) {
                collectGroups(node, groupIds);
            }
        }

    }


    @SneakyThrows
    public String getProviderbyPhone(String phone) {
        TblSubagents  tblSubagents = new TblSubagents();
        tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "   <providers>\n" +
                "       <getProviderByPhone>\n" +
                "           <phone>" + phone + "</phone>\n" +
                "       </getProviderByPhone>\n" +
                "   </providers>\n" +
                "</request>";


        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));
            XPath xPath = XPathFactory.newInstance().newXPath();
            Object result = xPath.compile("/response/providers/getProviderByPhone/@result").evaluate(parse, XPathConstants.STRING);
            Object result_descript = xPath.compile("/response/providers/getProviderByPhone/@result-description").evaluate(parse, XPathConstants.STRING);
            Object response_provId = xPath.compile("/response/providers/getProviderByPhone/providerId").evaluate(parse, XPathConstants.STRING);

            String providerId = String.valueOf(response_provId);


            return providerId;
        } catch (Exception e) {
           // logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            log.info(e.getMessage());
            return e.getMessage();
        }

    }


    public void getCommissions(String idTerminal) {
        TblSubagents  tblSubagents = new TblSubagents();
        tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");
        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "   <terminals>\n" +
                "       <getCommissions>\n" +
                "           <target-terminal>" + idTerminal + "</target-terminal>\n" +
                "       </getCommissions>\n" +
                "   </terminals>\n" +
                "</request>";


        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        NodeList rows;

        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object result = xPath.compile("/response/terminals/getCommissions/@result").evaluate(parse, XPathConstants.STRING);
            Object row = xPath.compile("/response/terminals/getCommissions/row").evaluate(parse, XPathConstants.NODESET);

            rows = (NodeList) row;
        } catch (Exception e) {
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());//Log
            rows = null;
        }


        for (int i = 0; i < rows.getLength(); i++) {
            Node rowItem = rows.item(i);
            NamedNodeMap rowAttr = rowItem.getAttributes();
            String fixCom = rowAttr.getNamedItem("fix-com").getNodeValue();
            Long provId = Long.valueOf(rowAttr.getNamedItem("prv-id").getNodeValue());
            String profileId = rowAttr.getNamedItem("profile-id").getNodeValue();
            try {
                Optional<TblQiwiOperCodes> tblQiwiOperCodes = tblQiwiOperCodesRepository.findById(provId);
                if (tblQiwiOperCodes.isPresent()) {
                    tblQiwiOperCodes.get().setCode(fixCom);
                    tblQiwiOperCodesRepository.save(tblQiwiOperCodes.get());
                }
            } catch (EntityNotFoundException e) {
                log.info(e.getMessage());
            }
        }

    }




    @SneakyThrows
    public void getBlob(){
        TblSubagents  tblSubagents = new TblSubagents();
        tblSubagents = tblSubagentsRepository.findLoginPassbyCode("IB_AUTH");

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <providers>\n" +
                "        <getUIProviders/>\n" +
                "    </providers>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);

        SoapResponse response = soapRequester.request(req.build());


        try {
            repository.deleteAll();

            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse()); // Log
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object method_resp_code = xPath.compile("/response/providers/getUIProviders/@result").evaluate(parse, XPathConstants.NUMBER);
            Object providers = xPath.compile("/response/providers/getUIProviders/provider").evaluate(parse, XPathConstants.NODESET);

            int getResCode  = ((Number) method_resp_code).intValue();

            if(getResCode==0) {

                NodeList list = (NodeList) providers;
                for (int i = 0; i < list.getLength(); i++) {
                    Node provider = list.item(i);
                    NamedNodeMap attributes = provider.getAttributes();
                    Long id = Long.valueOf(attributes.getNamedItem("id").getNodeValue());
                    String logoPath = attributes.getNamedItem("logo").getNodeValue();

                    TblProviders2Agents tblProviders2Agents = new TblProviders2Agents();

                    tblProviders2Agents = tblProviders2AgentsRepository.findByCode(String.valueOf(id));

                   // String imageString = null;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    URL url = new URL("https://update1.osmp.ru/"+logoPath);
                    BufferedImage img = ImageIO.read( url );

                    ImageIO.write(img, "png", bos);
                    byte[] imageBytes = bos.toByteArray();

                   // BASE64Encoder encoder = new BASE64Encoder();
                   // imageString = encoder.encode(imageBytes);

                    bos.close();

                    if(tblProviders2Agents!=null) {
                        TblProvidersBlob tblProvidersBlob = new TblProvidersBlob();
                        tblProvidersBlob.setIdProvider(tblProviders2Agents.getIdProvider());
                        tblProvidersBlob.setIdPartnerProvider(id);
                        tblProvidersBlob.setLogoPath("https://update1.osmp.ru/"+logoPath);
                        tblProvidersBlob.setLogo(imageBytes);
                        repository.save(tblProvidersBlob);
                    }


                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @SneakyThrows
    public GetBalanceDTO getBalance(String code ) {
        TblSubagents  tblSubagents = tblSubagentsRepository.findByCode(code);;
        GetBalanceDTO getBalanceDTO = new GetBalanceDTO();

        String reqBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                "<request>\n" +
                "    <auth login=\"" + tblSubagents.getLogin() + "\"  sign=\"" + DigestUtils.md5DigestAsHex(tblSubagents.getPassword().getBytes()).toLowerCase() + "\" signAlg=\"" + restConfigur.getSecurType() + "\"/>\n" +
                "    <client terminal=\"" + tblSubagents.getLogin() + "\" software=\"" + restConfigur.getSoftware() + "\"/>\n" +
                "    <agents>\n" +
                "        <getBalance/>\n" +
                "    </agents>\n" +
                "</request>";

        SoapRequest.SoapRequestBuilder req = SoapRequest.builder().body(reqBody);
        SoapResponse response = soapRequester.request(req.build());


        try {
            logMethods.wsLog(0, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());  // Log

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document parse = builder.parse(new ByteArrayInputStream(response.getResponse().getBytes()));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Object response_result = xPath.compile("/response/@result").evaluate(parse, XPathConstants.NUMBER);
            Object response_agentRes = xPath.compile("/response/agents/getBalance/@result").evaluate(parse, XPathConstants.NUMBER);
            Object agentId = xPath.compile("/response/agents/getBalance/agent-id").evaluate(parse, XPathConstants.NUMBER);
            Object balance = xPath.compile("/response/agents/getBalance/balance").evaluate(parse, XPathConstants.NUMBER);



            Double sumBalance = ((Number) balance).doubleValue();

            getBalanceDTO.setBalance(sumBalance);
            getBalanceDTO.setLogin(tblSubagents.getLogin());
            getBalanceDTO.setCode(tblSubagents.getCode());




        } catch (Exception e) {
            log.info(e.getMessage());
            logMethods.wsLog(1, impl.reqTime, new Date(), System.getProperty("user.name"), reqBody, response.getResponse());  // Log

        }
        return getBalanceDTO;
    }




}
