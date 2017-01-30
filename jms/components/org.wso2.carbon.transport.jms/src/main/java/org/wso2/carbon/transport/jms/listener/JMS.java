package org.wso2.carbon.transport.jms.listener;

import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

/**
 * Created by megala on 1/30/17.
 */
public class JMS extends TransportListener {
    public JMS(String id) {
        super(id);
    }

    public JMS() {
        super("abc");
    }
    @Override public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {

    }

    @Override public boolean bind(String s) {
        return false;
    }

    @Override public boolean unBind(String s) {
        return false;
    }

    @Override public String getProtocol() {
        return JMSConstants.PROTOCOL_JMS;
    }

    @Override protected void start() {

    }

    @Override protected void stop() {

    }

    @Override protected void beginMaintenance() {

    }

    @Override protected void endMaintenance() {

    }
}
