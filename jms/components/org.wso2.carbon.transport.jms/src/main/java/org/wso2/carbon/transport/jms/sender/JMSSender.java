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
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.transport.jms.listener.JMSTransportListener;
import org.wso2.carbon.transport.jms.utils.JMSConstants;


import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * JMS sender implementation.
 */

public class JMSSender implements TransportSender {

    private Logger logger = LoggerFactory.getLogger(JMSTransportListener.class);

    @Override public boolean send(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
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

            String conUsername =
                    (String) carbonMessage.getProperty(JMSConstants.CONNECTION_USERNAME);
            String conPassword =
                    (String) carbonMessage.getProperty(JMSConstants.CONNECTION_PASSWORD);

            Connection connection = null;
            if (conUsername != null && conPassword != null) {
                connection = jmsConnectionFactory.createConnection(conUsername, conPassword);
            }
            if (connection == null) {
                connection = jmsConnectionFactory.getConnection();
            }

            Session session = jmsConnectionFactory.getSession(connection);
            Destination destination = jmsConnectionFactory.getDestination(session);
            MessageProducer messageProducer =
                    jmsConnectionFactory.createMessageProducer(session, destination);


            //Create a message producer.

            //            MessageProducer producer = session.createProducer(destination);

            Message message = null;
            String messageType = (String) carbonMessage.getProperty(JMSConstants.JMS_MESSAGE_TYPE);

            //text message, map message
            if (messageType.equals(JMSConstants.TEXT_MESSAGE_TYPE)) {
                message = session.createTextMessage();
                TextMessage textMessage = (TextMessage) message;
                if (carbonMessage.getProperty(JMSConstants.TEXT_DATA) != null) {
                    textMessage.setText((String) carbonMessage.getProperty(JMSConstants.TEXT_DATA));
                }
            } else if (messageType.equals("JMS_MAP_MESSAGE")) {
                message = session.createMapMessage();
                MapMessage mapMessage = (MapMessage) message;
                //todo set values to map message
            }

            //set transport headers

            Object transportHeaders = carbonMessage.getProperty(JMSConstants.TRANSPORT_HEADERS);
            if (transportHeaders != null && transportHeaders instanceof Map) {
                JMSMessageUtils.setTransportHeaders(message, (Map<String, Object>) carbonMessage
                        .getProperty(JMSConstants.TRANSPORT_HEADERS));
            }

            //Send a message to the destination(queue/topic).
            messageProducer.send(message);

            //Close the session and connection resources.
            session.close();
            connection.close();

        } catch (JMSException e) {
            logger.error("Error in the message sender");
        }

        return false;
    }

   /* protected javax.naming.Context getInitialContext(String initialContextFactory,
                                                     String jndiProviderUrl) {

        try {

            Hashtable env;

            env = new Hashtable();

            // Store the environment variable that tell JNDI which initial context
            // to use and where to find the provider.

            // For use with the File System JNDI Service Provider
            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            env.put(javax.naming.Context.PROVIDER_URL, jndiProviderUrl);

            // Create the initial context.
            return new InitialContext(env);

        } catch (NamingException e) {

        }

        return null;
    }*/

    @Override public String getId() {
        return "JMS";
    }

    //    private String getStringPayload(CarbonMessage message){
    //        String result;
    //        try {
    //            if (message.isAlreadyRead()) {
    //                result = message.bui;
    //            } else {
    //                String payload = MessageUtils.getStringFromInputStream(msg.value().getInputStream());
    //                result = new BString(payload);
    //                msg.setBuiltPayload(result);
    //                msg.setAlreadyRead(true);
    //            }
    //            if (log.isDebugEnabled()) {
    //                log.debug("Payload in String:" + result.stringValue());
    //            }
    //        } catch (Throwable e) {
    //            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
    //        }
    //        return getBValues(result);
    //    }
}
