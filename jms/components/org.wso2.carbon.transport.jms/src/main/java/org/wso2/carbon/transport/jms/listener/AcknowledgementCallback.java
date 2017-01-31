package org.wso2.carbon.transport.jms.listener;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * JMSAcknowlegementCallback to be used when there is a need for acknowledgement
 */
public class AcknowledgementCallback implements CarbonCallback {
    @Override
    public void done(CarbonMessage carbonMessage) {

    }
}
