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

package org.wso2.carbon.transport.jms.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.jms.exception.JMSServerConnectorException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * This class is maintains the common methods used by JMS transport
 */
public class JMSUtils {

    private static final Log log = LogFactory.getLog(JMSUtils.class);

    /**
     * Return the JMS destination with the given destination name looked up from the context
     *
     * @param context         the Context to lookup
     * @param destinationName name of the destination to be looked up
     * @param destinationType type of the destination to be looked up
     * @return the JMS destination, or null if it does not exist
     */
    public static Destination lookupDestination(Context context, String destinationName, String destinationType)
            throws NamingException {
        if (destinationName == null) {
            return null;
        }

        try {
            return JMSUtils.lookup(context, Destination.class, destinationName);
        } catch (NameNotFoundException e) {
            try {
                Properties initialContextProperties = new Properties();
                if (context.getEnvironment() != null) {
                    if (context.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL) != null) {

                        initialContextProperties.put(JMSConstants.NAMING_FACTORY_INITIAL,
                                context.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL));
                    }
                    if (context.getEnvironment().get(JMSConstants.CONNECTION_STRING) != null) {
                        initialContextProperties.put(JMSConstants.CONNECTION_STRING,
                                context.getEnvironment().get(JMSConstants.CONNECTION_STRING));
                    }
                    if (context.getEnvironment().get(JMSConstants.PROVIDER_URL) != null) {
                        initialContextProperties.put(JMSConstants.PROVIDER_URL,
                                context.getEnvironment().get(JMSConstants.PROVIDER_URL));
                    }
                }
                if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
                    initialContextProperties.put(JMSConstants.TOPIC_PREFIX + destinationName, destinationName);
                } else if (JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)
                        || JMSConstants.DESTINATION_TYPE_GENERIC.equalsIgnoreCase(destinationType)) {
                    initialContextProperties.put(JMSConstants.QUEUE_PREFIX + destinationName, destinationName);
                }
                InitialContext initialContext = new InitialContext(initialContextProperties);
                try {
                    return JMSUtils.lookup(initialContext, Destination.class, destinationName);
                } catch (NamingException e1) {
                    return JMSUtils.lookup(context, Destination.class,
                            (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType) ?
                                    "dynamicTopics/" :
                                    "dynamicQueues/") + destinationName);
                }

            } catch (NamingException ex) {
                log.warn("Cannot locate destination : " + destinationName);
                throw ex;
            }
        } catch (NamingException ex) {
            log.warn("Cannot locate destination : " + destinationName, ex);
            throw ex;
        }
    }

    /**
     * JNDI look up in the context
     * @param context Context that need to looked up
     * @param clazz Class of the object that need to be found
     * @param name Name of the object that need to be looked up
     * @param <T> Class of the Object that need to be found
     * @return the relevant object, if found in the context
     * @throws NamingException, if the found object is different from the expected object
     */
    private static <T> T lookup(Context context, Class<T> clazz, String name) throws NamingException {
        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            // Instead of a ClassCastException, throw an exception with some
            // more information.
            if (object instanceof Reference) {
                Reference ref = (Reference) object;
                String errorMessage = "JNDI failed to de-reference Reference with name " + name + "; is the "
                        + "factory " + ref.getFactoryClassName() + " in your classpath?";
                throw new NamingException(errorMessage);
            } else {
                String errorMessage = "JNDI lookup of name " + name + " returned a " + object.getClass().getName() +
                        " while a " + clazz + " was expected";
                log.error(errorMessage);
                throw new NamingException(errorMessage);
            }
        }
    }

    /**
     * Change a jms message to carbon message
     * @param message JMS message that need to be changed as carbon message
     * @return the carbon message converted from jms message
     */
    public static CarbonMessage createJMSCarbonMessage(Message message) throws JMSServerConnectorException {
        CarbonMessage jmsCarbonMessage;
        try {
            if (message instanceof TextMessage) {
                jmsCarbonMessage = new TextCarbonMessage(((TextMessage) message).getText());
                jmsCarbonMessage.setProperty(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.TEXT_MESSAGE_TYPE);
                String messageId = message.getJMSMessageID();
                if (messageId != null) {
                    jmsCarbonMessage.setHeader(JMSConstants.JMS_MESSAGE_ID, messageId);
                }
                jmsCarbonMessage
                        .setHeader(JMSConstants.JMS_DELIVERY_MODE, String.valueOf(message.getJMSDeliveryMode()));
                jmsCarbonMessage.setHeader(JMSConstants.JMS_PRIORITY, String.valueOf(message.getJMSPriority()));
                jmsCarbonMessage.setHeader(JMSConstants.JMS_RE_DELIVERED, String.valueOf(message.getJMSRedelivered()));
                jmsCarbonMessage.setHeader(JMSConstants.JMS_TIME_STAMP, String.valueOf(message.getJMSTimestamp()));

                Enumeration<String> properties = message.getPropertyNames();

                while (properties.hasMoreElements()) {
                    String name = properties.nextElement();
                    jmsCarbonMessage.setHeader(name, message.getStringProperty(name));
                }
                return jmsCarbonMessage;
            }
            log.warn("Other message types are currently not supported. Only text messages are supported.");
            return null;
        } catch (JMSException e) {
            log.error("Error while changing the jms message to carbon message");
            throw new JMSServerConnectorException("Error while changing the jms message to carbon message", e);
        }
    }

    /**
     * To set the relevant transport headers to the jms message
     * @param message Relevant message to set the header
     * @param headerMap Header that need to be set
     */
    public static void setTransportHeaders(Message message, Map<String, Object> headerMap) {
        try {
            if (headerMap != null) {
                Iterator iterator = headerMap.keySet().iterator();

                while (true) {
                    String name;
                    do {
                        if (!iterator.hasNext()) {
                            return;
                        }

                        Object headerName = iterator.next();
                        name = (String) headerName;
                    } while (name.startsWith("JMSX") && !name.equals("JMSXGroupID") && !name.equals("JMSXGroupSeq"));

                    if ("JMS_COORELATION_ID".equals(name)) {
                        message.setJMSCorrelationID((String) headerMap.get("JMS_COORELATION_ID"));
                    } else {
                        Object value;
                        if ("JMS_DELIVERY_MODE".equals(name)) {
                            value = headerMap.get("JMS_DELIVERY_MODE");
                            if (value instanceof Integer) {
                                message.setJMSDeliveryMode(((Integer) value).intValue());
                            } else if (value instanceof String) {
                                try {
                                    message.setJMSDeliveryMode(Integer.parseInt((String) value));
                                } catch (NumberFormatException var8) {

                                }
                            } else {

                            }
                        } else if ("JMS_EXPIRATION".equals(name)) {
                            message.setJMSExpiration(Long.parseLong((String) headerMap.get("JMS_EXPIRATION")));
                        } else if ("JMS_MESSAGE_ID".equals(name)) {
                            message.setJMSMessageID((String) headerMap.get("JMS_MESSAGE_ID"));
                        } else if ("JMS_PRIORITY".equals(name)) {
                            message.setJMSPriority(Integer.parseInt((String) headerMap.get("JMS_PRIORITY")));
                        } else if ("JMS_TIMESTAMP".equals(name)) {
                            message.setJMSTimestamp(Long.parseLong((String) headerMap.get("JMS_TIMESTAMP")));
                        } else if ("JMS_MESSAGE_TYPE".equals(name)) {
                            message.setJMSType((String) headerMap.get("JMS_MESSAGE_TYPE"));
                        } else {
                            value = headerMap.get(name);
                            if (value instanceof String) {
                                message.setStringProperty(name, (String) value);
                            } else if (value instanceof Boolean) {
                                message.setBooleanProperty(name, ((Boolean) value).booleanValue());
                            } else if (value instanceof Integer) {
                                message.setIntProperty(name, ((Integer) value).intValue());
                            } else if (value instanceof Long) {
                                message.setLongProperty(name, ((Long) value).longValue());
                            } else if (value instanceof Double) {
                                message.setDoubleProperty(name, ((Double) value).doubleValue());
                            } else if (value instanceof Float) {
                                message.setFloatProperty(name, ((Float) value).floatValue());
                            }
                        }
                    }
                }
            }
        } catch (JMSException exception) {

        }
    }
}
