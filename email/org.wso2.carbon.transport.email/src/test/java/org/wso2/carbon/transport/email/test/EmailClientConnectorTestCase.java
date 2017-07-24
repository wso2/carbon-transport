package org.wso2.carbon.transport.email.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.email.sender.EmailClientConnector;
import org.wso2.carbon.transport.email.test.Utils.EmailTestConstant;

import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chathurika on 7/18/17.
 */
public class EmailClientConnectorTestCase {
 Map<String,String> map = new HashMap<>();
 private CarbonMessage carbonMessage;
    private static final Logger logger = LoggerFactory.getLogger(EmailClientConnectorTestCase.class);

 @BeforeClass
 public void setClientProperties(){
     map.put(EmailTestConstant.MAIL_SENDER_USERNAME , "emailtestwso2@gmail.com");
     map.put(EmailTestConstant.MAIL_SENDER_PASSWORD , "EmailTest@Wso2");
     map.put(EmailTestConstant.MAIL_HEADER_SUBJECT , "Test Client of transport");
     map.put(EmailTestConstant.MAIL_HEADER_TO , "dck.amarathunga@gmail.com");
     map.put("mail.smtp.host" , "smtp.gmail.com");
     map.put(EmailTestConstant.MAIL_HEADER_FROM , "emailtestwso2@gmail.com");
     map.put(EmailTestConstant.MAIL_SENDER_HOST_NAME , "smtp.gmail.com" );


     carbonMessage = new TextCarbonMessage("This is try message");
 }

    @Test(groups = "queueSending", description = "Testing whether queue sending is working correctly without any " +
            "exceptions")
    public void queueListeningTestCase() throws InterruptedException, JMSException, ClientConnectorException {

        EmailClientConnector sender = new EmailClientConnector();
        sender.send(carbonMessage, null, map);
        sender.send(carbonMessage, null, map);
    }


}
