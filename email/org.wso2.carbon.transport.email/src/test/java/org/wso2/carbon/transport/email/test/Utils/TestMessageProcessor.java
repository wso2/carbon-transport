package org.wso2.carbon.transport.email.test.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;

import java.util.concurrent.CountDownLatch;

/**
 * Created by chathurika on 7/11/17.
 */
public class TestMessageProcessor implements CarbonMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(TestMessageProcessor.class);
    private CountDownLatch latch = new CountDownLatch(1);
    public String content;
    public int count = 0;
    public String subject;

    @Override public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        content = ((TextCarbonMessage) carbonMessage).getText();
        log.info("one recved is called and content is" + content);
        subject = carbonMessage.getHeader(EmailTestConstant.MAIL_HEADER_SUBJECT);
        log.info("one recved is called and subject is:" + subject);
        count++;
        if (carbonCallback != null) {
            carbonCallback.done(carbonMessage);
        }
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
