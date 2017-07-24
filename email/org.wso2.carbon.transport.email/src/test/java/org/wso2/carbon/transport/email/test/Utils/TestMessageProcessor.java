package org.wso2.carbon.transport.email.test.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TransportSender;

import java.util.concurrent.CountDownLatch;

/**
 * Created by chathurika on 7/11/17.
 */
public class TestMessageProcessor implements CarbonMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(TestMessageProcessor.class);
    private CountDownLatch latch = new CountDownLatch(1);
    public String subject;
    public int count = 0;

    @Override public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        // content = carbonMessage.getMessageBody();
        log.info("one recve is called and subject is");
        Object obj = carbonMessage.getProperty("subject");
        subject = (String)obj;
        log.info("one recve is called and subject is:"+subject);
        count ++;
        done();

        return false;
    }

    @Override public void setTransportSender(TransportSender transportSender) {

    }

    @Override public void setClientConnector(ClientConnector clientConnector) {

    }

    @Override public String getId() {
        return null;
    }

    public void waitTillDone() throws InterruptedException {
        latch.await();
    }
    private void done() {
        latch.countDown();
    }
}
