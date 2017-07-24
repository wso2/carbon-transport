/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.transport.jms.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.SerializableCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.jms.exception.JMSConnectorException;
import org.wso2.carbon.transport.jms.factory.CachedJMSConnectionFactory;
import org.wso2.carbon.transport.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.transport.jms.factory.PooledJMSConnectionFactory;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;

/**
 * JMS sender implementation.
 */
public class JMSClientConnector implements ClientConnector {

    private static final Logger logger = LoggerFactory.getLogger(JMSClientConnector.class);

    private MessageProducer messageProducer;
    private Session session;
    private Connection connection;
    private JMSConnectionFactory jmsConnectionFactory;

    public JMSClientConnector() {
        super();
    }

    @Override
    public Object init(CarbonMessage carbonMessage, CarbonCallback carbonCallback, Map<String, Object> map)
            throws ClientConnectorException {
        throw new ClientConnectorException("Method not supported for JMS.");
    }

    /**
     * @return false because, in this instance, the send method with a map parameter is required.
     */
    @Override
    public boolean send(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws ClientConnectorException {
        return false;
    }

    @Override
    public synchronized boolean send(CarbonMessage carbonMessage, CarbonCallback carbonCallback,
                                  Map<String, String> propertyMap) throws ClientConnectorException {
        try {

            setupMessageProducer(propertyMap);

            Message jmsMessage = createJmsMessage(carbonMessage, propertyMap);

            sendMessage(carbonMessage, jmsMessage);

        } finally {
            if (jmsConnectionFactory != null) {
                try {
                    jmsConnectionFactory.closeMessageProducer(messageProducer);
                    jmsConnectionFactory.closeSession(session);
                    jmsConnectionFactory.closeConnection(connection);
                } catch (JMSConnectorException e) {
                    logger.error("Exception occurred when closing connection. Error: " + e.getMessage(), e);
                }
            }
        }
        return false;
    }

    private void sendMessage(CarbonMessage carbonMessage, Message jmsMessage) throws ClientConnectorException {
        int deliveryMode = DeliveryMode.PERSISTENT;
        if (getHeader(carbonMessage, JMSConstants.JMS_DELIVERY_MODE).
                equalsIgnoreCase(JMSConstants.NON_PERSISTENT_DELIVERY_MODE)) {
            deliveryMode = DeliveryMode.NON_PERSISTENT;
        }
        int priority = JMSConstants.DEFAULT_PRIORITY;
        String value = carbonMessage.getHeader(JMSConstants.JMS_PRIORITY);
        if (value != null) {
            priority = Integer.parseInt(value);
        }

        long timeToLive = 0;
        value = carbonMessage.getHeader(JMSConstants.JMS_EXPIRATION);
        if (value != null) {
            timeToLive = Long.parseLong(value);
        }
        try {
            messageProducer.send(jmsMessage, deliveryMode, priority, timeToLive);
        } catch (JMSException e) {
            throw new ClientConnectorException("Send Failed with priority " + priority + " , delivery mode "
                    + deliveryMode + " [ " + e.getMessage() + " ]", e);
        }

    }

    private Message createJmsMessage(CarbonMessage carbonMessage, Map<String, String> propertyMap)
                                                                        throws ClientConnectorException {
        Message jmsMessage;
        String messageType = propertyMap.get(JMSConstants.JMS_MESSAGE_TYPE);
        try {
            if (carbonMessage instanceof TextCarbonMessage) {
                String textData = ((TextCarbonMessage) carbonMessage).getText();
                jmsMessage = session.createTextMessage(textData);
            } else if (carbonMessage instanceof SerializableCarbonMessage) {
                jmsMessage = session.createObjectMessage((SerializableCarbonMessage) carbonMessage);
            } else if (carbonMessage instanceof MapCarbonMessage) {
                MapMessage jmsMapMessage = session.createMapMessage();
                MapCarbonMessage mapCarbonMessage = (MapCarbonMessage) carbonMessage;
                Enumeration<String> mapNames = mapCarbonMessage.getMapNames();
                while (mapNames.hasMoreElements()) {
                    String key = mapNames.nextElement();
                    jmsMapMessage.setString(key, mapCarbonMessage.getValue(key));
                }
                jmsMessage = jmsMapMessage;
            } else if (carbonMessage instanceof DefaultCarbonMessage) {
                jmsMessage = session.createMessage();
            } else {
                throw new ClientConnectorException("Unknown carbon message instance provided. " +
                        "Message type: " + messageType);
            }
            setJmsProperties(carbonMessage, jmsMessage);
            setJmsHeaders(carbonMessage, jmsMessage);
            return jmsMessage;
        } catch (JMSException e) {
            throw new ClientConnectorException("Error occurred while preparing the JMS message. " +
                                                e.getMessage(), e);
        }
    }

    /**
     * Set the JMS headers that are allowed to set by the client application.
     * refer: https://docs.oracle.com/cd/E19798-01/821-1841/bnces/index.html
     *
     * @param carbonMessage source {@link CarbonMessage}
     * @param jmsMessage Headers are set to the {@link Message}
     * @throws ClientConnectorException throws when there is an internal error when setting the JMS headers
     */
    private void setJmsHeaders(CarbonMessage carbonMessage, Message jmsMessage) throws ClientConnectorException {
        try {
            String value = getHeader(carbonMessage, JMSConstants.JMS_CORRELATION_ID);
            if (!value.isEmpty()) {
                jmsMessage.setJMSCorrelationID(value);
            }
            value = getHeader(carbonMessage, JMSConstants.JMS_MESSAGE_TYPE);
            if (!value.isEmpty()) {
                jmsMessage.setJMSType(value);
            }
            value = getHeader(carbonMessage, JMSConstants.JMS_REPLY_TO);
            if (!value.isEmpty()) {
                Destination destination = jmsConnectionFactory.getDestination(value);
                jmsMessage.setJMSReplyTo(destination);
            }
        } catch (NamingException e) {
            throw new ClientConnectorException("Error occurred while setting " + JMSConstants.JMS_REPLY_TO +
                    " JMS message header." + e.getMessage(), e);
        } catch (JMSException e) {
            throw new ClientConnectorException("Error occurred while setting JMS message headers. "
                                                + e.getMessage(), e);
        }
    }

    private String getHeader(CarbonMessage carbonMessage, String headerName) {
        String value = carbonMessage.getHeader(headerName);
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private void setJmsProperties(CarbonMessage carbonMessage, Message message) throws JMSException {
        for (Map.Entry<String, Object> entry : carbonMessage.getProperties().entrySet()) {
            message.setStringProperty(entry.getKey(), entry.getValue().toString());
        }
    }

    /**
     * Creates a new {@link MessageProducer} using a new or existing connection to the JMS provider.
     *
     * @param propertyMap               Map of user defined properties
     * @throws ClientConnectorException throws when an internal error occur trying to make the JMS provider connection
     */
    private void setupMessageProducer(Map<String, String> propertyMap) throws ClientConnectorException {
        try {
            Properties properties = new Properties();
            properties.putAll(propertyMap);

            String connectionFactoryNature = properties.getProperty(JMSConstants.CONNECTION_FACTORY_NATURE);

            if (connectionFactoryNature != null) {
                switch (connectionFactoryNature) {
                    case JMSConstants.CACHED_CONNECTION_FACTORY:
                        jmsConnectionFactory = new CachedJMSConnectionFactory(properties);
                        break;
                    case JMSConstants.POOLED_CONNECTION_FACTORY:
                        jmsConnectionFactory = new PooledJMSConnectionFactory(properties);
                        break;
                    default:
                        jmsConnectionFactory = new JMSConnectionFactory(properties);
                }
            } else {
                jmsConnectionFactory = new JMSConnectionFactory(properties);
            }

            String conUsername = properties.getProperty(JMSConstants.CONNECTION_USERNAME);
            String conPassword = properties.getProperty(JMSConstants.CONNECTION_PASSWORD);

            Connection connection;
            if (conUsername != null && conPassword != null) {
                connection = this.jmsConnectionFactory.createConnection(conUsername, conPassword);
            } else {
                connection = this.jmsConnectionFactory.createConnection();
            }

            this.connection = connection;

            Session session = this.jmsConnectionFactory.getSession(connection);
            this.session = session;

            Destination destination = this.jmsConnectionFactory.getDestination(session);
            this.messageProducer = this.jmsConnectionFactory.getMessageProducer(session, destination);
        } catch (JMSConnectorException | JMSException e) {
            throw new ClientConnectorException("Error connecting to JMS provider. " + e.getMessage(), e);
        }
}

    @Override
    public String getProtocol() {
        return "jms";
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor messageProcessor) {
        // Message processor is not needed with regards to jms client connector
    }
}
