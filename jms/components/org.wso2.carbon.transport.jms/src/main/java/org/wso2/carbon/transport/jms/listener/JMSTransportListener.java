package org.wso2.carbon.transport.jms.listener;

import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.transport.jms.factory.JMSConnectionFactory;

import javax.jms.Connection;
import java.util.List;
import java.util.Properties;

/**
 * This is a transport listener for JMS
 */
public class JMSTransportListener extends TransportListener {
    private CarbonMessageProcessor carbonMessageProcessor;
    private List<Connection> =


    public JMSTransportListener(String id) {
        super(id);
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.carbonMessageProcessor = carbonMessageProcessor;
    }

    public CarbonMessageProcessor getCarbonMessageProcessor() {
        return carbonMessageProcessor;
    }

    @Override
    public boolean bind(String s) {
        return false;
    }

    @Override
    public boolean unBind(String s) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void start() {
        Properties properties = new Properties();
        properties.put("interval", 1000);
        properties.put("transport.jms.CacheLevel", 1);
        properties.put("transport.jms.ConnectionFactoryJNDIName", "QueueConnectionFactory");
        properties.put("sequential", true);
        properties.put("java.naming.factory.initial", "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
        properties.put("transport.jms.ConnectionFactoryType", "queue");
        properties.put("java.naming.provider.url", "conf/jndi.properties");
    }

    @Override
    protected void stop() {

    }

    @Override
    protected void beginMaintenance() {

    }

    @Override
    protected void endMaintenance() {

    }
}
