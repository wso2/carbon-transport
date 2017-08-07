package org.wso2.carbon.transport.email.test;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.email.provider.EmailServerConnectorProvider;
import org.wso2.carbon.transport.email.test.utils.EmailTestConstant;
import org.wso2.carbon.transport.email.test.utils.TestMessageProcessor;

import java.util.HashMap;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;


public class IMAPServerTestCase {

    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost.com";
    private static final String EMAIL_FROM = "someone@localhost.com";
    private static final String EMAIL_CC = "ccsomeone@localhost.com";
    private static final String EMAIL_BCC = "bcsomeone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String LOCALHOST = "127.0.0.1";
    private GreenMail mailServer;
    private GreenMailUser user;
    private Map<String, String> emailProperties = new HashMap<>();

    @BeforeClass public void setUp() throws MessagingException, UserException {

        mailServer = new GreenMail(ServerSetupTest.IMAP);
        mailServer.start();
        user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);

        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS));
        message.addRecipient(Message.RecipientType.CC, new InternetAddress(EMAIL_CC));
        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(EMAIL_BCC));
        message.setSubject("with ToCcBcc recipients");
        message.setText(EMAIL_TEXT);
        user.deliver(message);

        MimeMessage message2 = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS));
        message.setSubject("with to recipients");
        message.setText(EMAIL_TEXT);
        user.deliver(message);

        MimeMessage message3 = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_USER_ADDRESS));
        message.addRecipient(Message.RecipientType.CC, new InternetAddress(EMAIL_CC));
        message.setSubject("with cc recipients");
        message.setText(EMAIL_TEXT);
        user.deliver(message);

        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_USERNAME, user.getLogin());
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_PASSWORD, user.getPassword());
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_HOST_NAME, LOCALHOST);
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_STORE_TYPE, "imap");
        emailProperties.put(EmailTestConstant.MAIL_RECEIVER_FOLDER_NAME, "INBOX");
        emailProperties.put("mail.imap.port", Integer.toString(ServerSetupTest.IMAP.getPort()));
        emailProperties.put(EmailTestConstant.POLLING_INTERVAL, "10000");
        emailProperties.put(EmailTestConstant.AUTO_ACKNOWLEDGE, "true");

    }

    @AfterClass public void tearDown() {
        mailServer.stop();
    }

    @BeforeMethod public void sendTestMail() throws MessagingException, UserException {
    }

    @AfterMethod public void deleteAllMails() {

    }

    @Test public void getMails() throws ServerConnectorException, InterruptedException {

        SearchTerm subjectTerm = new SubjectTerm("with ToCcBcc recipients");
        String subjectTermS = "subject: with ToCcBcc recipients ";

        EmailServerConnectorProvider emailServerConnectorProvider = new EmailServerConnectorProvider();
        ServerConnector connector = emailServerConnectorProvider.createConnector("testEmail", emailProperties);
        TestMessageProcessor testMessageProcessor = new TestMessageProcessor();
        connector.setMessageProcessor(testMessageProcessor);
        connector.start();
        testMessageProcessor.waitTillDone();
        Assert.assertEquals(testMessageProcessor.count, 1);
        connector.stop();
        Thread.sleep(2000);
        connector.start();
        testMessageProcessor.waitTillDone();
        Thread.sleep(2000);

    }

}
