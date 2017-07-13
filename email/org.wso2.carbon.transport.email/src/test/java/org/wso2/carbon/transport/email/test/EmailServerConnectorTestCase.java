package org.wso2.carbon.transport.email.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.email.provider.EmailServerConnectorProvider;
import org.wso2.carbon.transport.email.test.Utils.EmailTestUtils;
import org.wso2.carbon.transport.email.test.Utils.TestMessageProcessor;

import java.util.Map;

/**
 * Created by chathurika on 7/11/17.
 */
public class EmailServerConnectorTestCase {

    @Test(description = "Testing the scenario: reading a file and asserting whether"
            + " its content " + "is equal to what is expected.")
    public void emailTest() throws ServerConnectorException, InterruptedException {
        EmailTestUtils emailtestutils = new EmailTestUtils();
        Map<String, String> emailProperty = emailtestutils.getEmailReceivedServerParameters
                ("dck.amarathunga@gmail.com" , "A&Cforever" , "imap.gmail.com" , "imaps" , "INBOX");
       /* Map<String, String> emailProperty = emailtestutils.getEmailReceivedServerParameters
                ("pop3testwso2@gmail.com" , "Pop3Test@Wso2" , "pop.gmail.com" , "pop3s" , "INBOX");*/
        EmailServerConnectorProvider emailServerConnectorProvider = new EmailServerConnectorProvider();
        ServerConnector connector = emailServerConnectorProvider.createConnector("testEmail" , emailProperty);
        TestMessageProcessor testMessageProcessor = new TestMessageProcessor();
        connector.setMessageProcessor(testMessageProcessor);

        connector.start();
        testMessageProcessor.waitTillDone();
        System.out.println(testMessageProcessor.subject);
        Assert.assertEquals(testMessageProcessor.count, 1);

    }

    @Test(description = "Testing the scenario: reading a file and asserting whether"
            + " its content " + "is equal to what is expected.")
    public void emailTest2() throws ServerConnectorException, InterruptedException {
        EmailTestUtils emailtestutils = new EmailTestUtils();
        Map<String, String> emailProperty = emailtestutils.getEmailReceivedServerParameters
                ("pop3testwso2@gmail.com" , "Pop3Test@Wso2" , "pop.gmail.com" , "pop3s" , "INBOX");
        EmailServerConnectorProvider emailServerConnectorProvider = new EmailServerConnectorProvider();
        ServerConnector connector = emailServerConnectorProvider.createConnector("testEmail" , emailProperty);
        TestMessageProcessor testMessageProcessor = new TestMessageProcessor();
        connector.setMessageProcessor(testMessageProcessor);

        connector.start();
        testMessageProcessor.waitTillDone();
        System.out.println(testMessageProcessor.subject);
        Assert.assertEquals(testMessageProcessor.count, 1);

    }
}
