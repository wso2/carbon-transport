/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.jms.listener;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ListeningServerConnector;
import org.wso2.carbon.transport.jms.exception.JMSServerConnectorException;
import org.wso2.carbon.transport.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * This is a transport listener for JMS
 */
public class JMSServerConnector extends ListeningServerConnector {
    private static final Log logger = LogFactory.getLog(JMSServerConnector.class.getName());
    private CarbonMessageProcessor carbonMessageProcessor;
    private JMSConnectionFactory jmsConnectionFactory = null;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer messageConsumer;
    private Properties properties;
    private int retryInterval = 1;
    private int maxRetryCount = 5;

    public JMSServerConnector(String id) {
        super(id);
    }

    public void init(Map<String, String> map) {
        properties = new Properties();
        Set<Map.Entry<String, String>> set = map.entrySet();
        for (Map.Entry<String, String> entry : set) {
            String mappedParameter = JMSConstants.MAPPING_PARAMETERS.get(entry.getKey());
            if (mappedParameter != null) {
                properties.put(mappedParameter, entry.getValue());
            }
        }

        String retryInterval = map.get(JMSConstants.RETRY_INTERVAL);
        if (retryInterval != null) {
            try {
                this.retryInterval = Integer.parseInt(retryInterval);
            } catch (NumberFormatException ex) {
                logger.error("Provided value for retry interval is invalid, using the default retry interval value "
                        + retryInterval);
            }
        }

        String maxRetryCount = map.get(JMSConstants.MAX_RETRY_COUNT);
        if (maxRetryCount != null) {
            try {
                this.maxRetryCount = Integer.parseInt(maxRetryCount);
            } catch (NumberFormatException ex) {
                logger.error("Provided value for max retry count is invalid, using the default max retry count "
                        + maxRetryCount);
            }
        }

    }


    @Override
    public boolean bind() {
        try {
            createDestinationListener();
            return true;
        } catch (Exception e) {
            try {
                if (jmsConnectionFactory == null) {
                    throw new RuntimeException("Cannot create the jms connection factory. please check the connection"
                            + " properties and re-deploy the jms service");
                }
                closeAll();
                JMSConnectionRetryHandler jmsConnectionRetryHandler = new JMSConnectionRetryHandler(this,
                        retryInterval, maxRetryCount);
                jmsConnectionRetryHandler.run();
            } catch (JMSServerConnectorException e1) {
                throw new RuntimeException(e1.getMessage(), e1);
            }
        }
        return false;
    }

    @SuppressFBWarnings({"REC_CATCH_EXCEPTION"})
    void createDestinationListener() throws JMSServerConnectorException {
        try {
            jmsConnectionFactory = new JMSConnectionFactory(properties);
            connection = jmsConnectionFactory.createConnection();
            jmsConnectionFactory.start(connection);
            session = jmsConnectionFactory.createSession(connection);
            destination = jmsConnectionFactory.getDestination(session);
            messageConsumer = jmsConnectionFactory.createMessageConsumer(session, destination);
            messageConsumer.setMessageListener(
                    new JMSMessageListener(carbonMessageProcessor, id, session.getAcknowledgeMode(), session));
        } catch (Exception e) {
            logger.error("Error while creating the connection from connection factory. " + e.getMessage());
            throw new JMSServerConnectorException("Error while creating the connection from connection factory", e);
        }

    }

    void closeAll() {
        try {
            jmsConnectionFactory.closeConnection(connection);
            jmsConnectionFactory.closeSession(session);
            jmsConnectionFactory.closeMessageConsumer(messageConsumer);
        } catch (JMSServerConnectorException e) {
            logger.error("Error while closing the connection, session and message consumer ", e);
        }
    }

    @Override
    public boolean unbind() {
        return false;
    }

    public JMSServerConnector() {
        super("jms");
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.carbonMessageProcessor = carbonMessageProcessor;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void start() {
        // Not needed for JMS Transport
    }

    @Override
    public void stop() {
        try {
            jmsConnectionFactory.closeConnection(connection);
            jmsConnectionFactory.closeSession(session);
            jmsConnectionFactory.closeMessageConsumer(messageConsumer);
        } catch (JMSServerConnectorException e) {
            logger.error("Error while closing the connection, session and consumer ", e);
        }
    }

    @Override
    protected void beginMaintenance() {
        try {
            jmsConnectionFactory.stop(connection);
        } catch (JMSServerConnectorException e) {
            logger.error("Error while trying to stop the connection to stop receiving the messages");
        }
    }

    @Override
    protected void endMaintenance() {
        try {
            jmsConnectionFactory.start(connection);
        } catch (JMSServerConnectorException e) {
            logger.error("Error while trying to start the connection to start receiving the messages");
        }
    }
}
