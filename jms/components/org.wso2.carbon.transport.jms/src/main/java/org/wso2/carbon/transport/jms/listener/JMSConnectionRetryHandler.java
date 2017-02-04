package org.wso2.carbon.transport.jms.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.jms.exception.JMSServerConnectorException;

/**
 * This class tries to connect to JMS provider until the maximum re-try count meets
 */
class JMSConnectionRetryHandler {
    private JMSServerConnector jmsServerConnector;
    private static final Log logger = LogFactory.getLog(JMSConnectionRetryHandler.class.getName());
    private long retryInteval;
    private int retryCount = 0;
    private int maxRetryCount;

    JMSConnectionRetryHandler(JMSServerConnector jmsServerConnector, long retryInterval, int maxRetryCount) {
        this.jmsServerConnector = jmsServerConnector;
        this.retryInteval = retryInterval;
        this.maxRetryCount = maxRetryCount;
    }

    void start() throws JMSServerConnectorException {
        int minuteToMilli = 60000;
        try {
            Thread.sleep(retryInteval * minuteToMilli);
        } catch (InterruptedException e) {
            //Ignore the exception
        }
        while (retryCount < maxRetryCount) {
            try {
                retryCount++;
                jmsServerConnector.createDestinationListener();
                return;
            } catch (JMSServerConnectorException ex) {
                jmsServerConnector.closeAll();
                logger.error("Retry connection attempt " + retryCount + " to JMSfailed. Retry will be attempted ");
                retryInteval = retryInteval * 2;
                try {
                    Thread.sleep(retryInteval * minuteToMilli);
                } catch (InterruptedException e) {
                    // ignore the exception
                }
                retryCount++;
            }
        }
        throw new JMSServerConnectorException("Connection to the jms provider failed after retrying for " +
                retryCount + " times");

    }
}
