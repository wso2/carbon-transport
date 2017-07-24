package org.wso2.carbon.transport.email.test;

import com.icegreen.greenmail.util.ServerSetup;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.email.provider.EmailServerConnectorProvider;
import org.wso2.carbon.transport.email.test.Utils.EmailTestUtils;
import org.wso2.carbon.transport.email.test.Utils.TestMessageProcessor;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

//import static org.testng.Assert.assertNotNull;
///import static org.testng.AssertJUnit.assertEquals;
//import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by chathurika on 7/13/17.
 */
public class IMAPServerTestCase {

    private static final String USER_PASSWORD = "abcdef123";
    private static final String USER_NAME = "hascode";
    private static final String EMAIL_USER_ADDRESS = "hascode@localhost";
    private static final String EMAIL_TO = "someone@localhost.com";
    private static final String EMAIL_SUBJECT = "Test E-Mail";
    private static final String EMAIL_TEXT = "This is a test e-mail.";
    private static final String LOCALHOST = "127.0.0.1";
    private GreenMail mailServer;

    @BeforeClass
    public void setUp() {
        mailServer = new GreenMail(ServerSetupTest.IMAP);
        mailServer.start();

    }

    @AfterClass
    public void tearDown() {
        mailServer.stop();
    }

    @Test
    public void getMails() throws IOException, MessagingException,
            UserException, InterruptedException {

        // create user on mail server
        GreenMailUser user = mailServer.setUser(EMAIL_USER_ADDRESS, USER_NAME,
                USER_PASSWORD);

        // create an e-mail message using javax.mail ..
        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(new InternetAddress(EMAIL_TO));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                EMAIL_USER_ADDRESS));
        message.setSubject(EMAIL_SUBJECT);
        message.setText(EMAIL_TEXT);

        // use greenmail to store the message
        user.deliver(message);


        EmailTestUtils emailtestutils = new EmailTestUtils();
        Map<String, String> emailProperty = emailtestutils.getEmailReceivedServerParameters
                (user.getLogin() , user.getPassword() , LOCALHOST , "imap" , "INBOX");

        emailProperty.put( "mail.imap.port" , Integer.toString(ServerSetupTest.IMAP.getPort()) );
        // fetch the e-mail via imap using javax.mail ..
        Properties props = new Properties();
        props.putAll(emailProperty);
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

        assertNotNull(messages);
        assertEquals( 1, messages.length);
        Assert.assertEquals(EMAIL_SUBJECT , messages[0].getSubject());
        assertEquals(EMAIL_TO, messages[0].getFrom()[0].toString());


    }



}
