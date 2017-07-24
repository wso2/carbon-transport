package org.wso2.carbon.transport.email.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import javax.security.auth.callback.Callback;
import java.util.concurrent.CountDownLatch;

/**
 * Created by chathurika on 7/24/17.
 */
public class EmailServerConnectorCallback implements CarbonCallback {
    private static final Logger log = LoggerFactory.getLogger(EmailServerConnectorCallback.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void done(CarbonMessage carbonMessage) {
        if (log.isDebugEnabled()) {
            log.debug("Message processor acknowledgement received.");
        }
        latch.countDown();
    }


}
