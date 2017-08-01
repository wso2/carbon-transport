package org.wso2.carbon.transport.email.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.email.provider.EmailServerConnectorProvider;
import org.wso2.carbon.transport.email.test.Utils.EmailTestConstant;
import org.wso2.carbon.transport.email.test.Utils.TestMessageProcessor;

import javax.mail.Message;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chathurika on 7/26/17.
 */
public class ImapGmailServerTest {

    private static final String USER_PASSWORD = "A&Cforever";
    private static final String USER_NAME = "dck.amarathunga@gmail.com";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost.com";
    private static final String EMAIL_FROM = "pop3test@gmail.com.com";
    private static final String EMAIL_CC = "dck.amarathunga@gmail.com";
    private static final String EMAIL_BCC = "hi4amila@gmail.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String EMAIL_HOST = "imap.gmail.com";
    private Map<String,String> emailProperties = new HashMap<>();


    @BeforeClass
    public void setProperties() {
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_USERNAME, USER_NAME);
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_PASSWORD, USER_PASSWORD);
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_HOST_NAME, EMAIL_HOST);
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_STORE_TYPE, "imap");
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_FOLDER_NAME, "INBOX");
        emailProperties.put("mail.imap.port", "993");
        emailProperties.put("mail.imap.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        emailProperties.put("mail.imap.socketFactory.fallback" , "false");
        emailProperties.put("mail.%s.socketFactory.port" , "993");
        emailProperties.put(EmailTestConstant.POLLING_INTERVAL, "100000");

    }

    @Test (description = "Testing the scenario of stringEmailSearchTerm: receiving email and asserting whether" +
            "number of messages receives is equal to what is expected.")
    public void getMails() throws ServerConnectorException, InterruptedException {

        SearchTerm subjectTerm = new RecipientStringTerm(Message.RecipientType.CC, "pop3testwso2@gmail.com");
        String emailSerch = "subject:DAS,";

        EmailServerConnectorProvider emailServerConnectorProvider = new EmailServerConnectorProvider();
        ServerConnector connector = emailServerConnectorProvider.createConnector("testEmail" ,
                emailProperties , emailSerch);
        TestMessageProcessor testMessageProcessor = new TestMessageProcessor();
        connector.setMessageProcessor(testMessageProcessor);
        connector.start();
        testMessageProcessor.waitTillDone();
        Assert.assertEquals(testMessageProcessor.count, 1);
        connector.stop();
        Thread.sleep(10000);

    }


}
