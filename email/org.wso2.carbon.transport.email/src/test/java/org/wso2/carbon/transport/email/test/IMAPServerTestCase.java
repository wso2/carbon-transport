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
import org.wso2.carbon.transport.email.test.Utils.EmailTestConstant;
import org.wso2.carbon.transport.email.test.Utils.EmailTestUtils;
import org.wso2.carbon.transport.email.test.Utils.TestMessageProcessor;
import org.wso2.carbon.transport.email.utils.EmailConstants;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Created by chathurika on 7/13/17.
 */
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
    private Map<String,String> emailProperties = new HashMap<>();

    @BeforeClass
    public void setUp() {
        mailServer = new GreenMail(ServerSetupTest.IMAP);
        mailServer.start();

    }

    @AfterClass
    public void tearDown() {
        mailServer.stop();
    }

    @BeforeMethod
    public void sendTestMail() throws MessagingException, UserException { }


    @AfterMethod
    public void deleteAllMails() {

    }

    @Test
    public void getMails()
            throws IOException, MessagingException, UserException, InterruptedException, ServerConnectorException {

        // create user on mail server
       /* GreenMailUser user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME,
                USER_PASSWORD);

        // create an e-mail message using javax.mail ..
        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_TO));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                EMAIL_USER_ADDRESS));
        message.setSubject(EMAIL_SUBJECT);
        message.setText(EMAIL_TEXT);

        // use greenmail to store the message
        user.deliver(message);*/

        user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME,
                USER_PASSWORD);

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
       emailProperties.put("mail.imap.port" , Integer.toString(ServerSetupTest.IMAP.getPort()));

        /*EmailTestUtils emailtestutils = new EmailTestUtils();
        Map<String, String> emailProperty = emailtestutils.getEmailReceivedServerParameters
                (user.getLogin() , user.getPassword() , LOCALHOST , "imap" , "INBOX");*/

       // emailProperty.put( "mail.imap.port" , Integer.toString(ServerSetupTest.IMAP.getPort()) );

        // fetch the e-mail via imap using javax.mail ..
           Properties props = new Properties();
          props.putAll(emailProperties);
      //  props.put("mail.imap.port" , ServerSetupTest.IMAP.getPort() );
          Session session = Session.getInstance(props);
       /* URLName urlName = new URLName("imap", LOCALHOST,
                ServerSetupTest.IMAP.getPort(), null, user.getLogin(),
                user.getPassword());*/
          Store store = session.getStore("imap");
          store.connect(LOCALHOST, user.getLogin(), user.getPassword());

          Folder folder = store.getFolder("INBOX");
          folder.open(Folder.READ_ONLY);
          Message[] messages = folder.getMessages();
        assertEquals( 3, messages.length);
        store.close();

        EmailServerConnectorProvider emailServerConnectorProvider = new EmailServerConnectorProvider();
        ServerConnector connector = emailServerConnectorProvider.createConnector("testEmail" , emailProperties);
        TestMessageProcessor testMessageProcessor = new TestMessageProcessor();
        connector.setMessageProcessor(testMessageProcessor);
        connector.start();
        testMessageProcessor.waitTillDone();
        Assert.assertEquals(testMessageProcessor.count, 3);



       // assertNotNull(messages);
       // assertEquals( 1, messages.length);
       // Assert.assertEquals(EMAIL_SUBJECT , messages[0].getSubject());
        //assertEquals(EMAIL_TO, messages[0].getFrom()[0].toString());




    }



}
