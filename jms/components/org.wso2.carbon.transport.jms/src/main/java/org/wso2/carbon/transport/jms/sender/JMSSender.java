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

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.jms.exception.JMSServerConnectorException;
import org.wso2.carbon.transport.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.transport.jms.utils.JMSConstants;
import org.wso2.carbon.transport.jms.utils.JMSUtils;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * JMS sender implementation.
 */

public class JMSSender implements TransportSender {

    @Override
    public boolean send(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws MessageProcessorException {
        try {
            Properties properties = new Properties();
            Set<Map.Entry<String, Object>> set = carbonMessage.getProperties().entrySet();
            for (Map.Entry<String, Object> entry : set) {
                String mappedParameter = JMSConstants.MAPPING_PARAMETERS.get(entry.getKey());
                if (mappedParameter != null) {
                    properties.put(mappedParameter, entry.getValue());
                }
            }
            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(properties);

            String conUsername = (String) carbonMessage.getProperty(JMSConstants.CONNECTION_USERNAME);
            String conPassword = (String) carbonMessage.getProperty(JMSConstants.CONNECTION_PASSWORD);

            Connection connection = null;
            if (conUsername != null && conPassword != null) {
                connection = jmsConnectionFactory.createConnection(conUsername, conPassword);
            }
            if (connection == null) {
                connection = jmsConnectionFactory.createConnection();
            }

            Session session = jmsConnectionFactory.createSession(connection);
            Destination destination = jmsConnectionFactory.getDestination(session);
            MessageProducer messageProducer = jmsConnectionFactory.createMessageProducer(session, destination);

            Message message = null;
            String messageType = (String) carbonMessage.getProperty(JMSConstants.JMS_MESSAGE_TYPE);

            if (messageType.equals(JMSConstants.TEXT_MESSAGE_TYPE)) {
                message = session.createTextMessage();
                TextMessage textMessage = (TextMessage) message;
                if (carbonMessage.getProperty(JMSConstants.TEXT_DATA) != null) {
                    textMessage.setText((String) carbonMessage.getProperty(JMSConstants.TEXT_DATA));
                }
            }

            Object transportHeaders = carbonMessage.getProperty(JMSConstants.TRANSPORT_HEADERS);
            if (transportHeaders != null && transportHeaders instanceof Map) {
                JMSUtils.setTransportHeaders(message,
                        (Map<String, Object>) carbonMessage.getProperty(JMSConstants.TRANSPORT_HEADERS));
            }

            messageProducer.send(message);

            session.close();
            connection.close();

        } catch (JMSException e) {
            throw new RuntimeException("Exception occurred while sending the message.");
        } catch (JMSServerConnectorException e) {
            throw new RuntimeException("Exception occurred while sending the message", e);
        }
        return false;
    }


    @Override
    public String getId() {
        return "JMS";
    }
}
