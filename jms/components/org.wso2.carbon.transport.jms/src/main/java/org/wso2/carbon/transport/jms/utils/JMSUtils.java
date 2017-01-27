/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.messaging.TextJMSCarbonMessage;

import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;


/**
 * Maintain the common methods used by inbound JMS protocol
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

            } catch (NamingException x) {
                log.warn("Cannot locate destination : " + destinationName);
                throw x;
            }
        } catch (NamingException e) {
            log.warn("Cannot locate destination : " + destinationName, e);
            throw e;
        }
    }

    private static <T> T lookup(Context context, Class<T> clazz, String name) throws NamingException {

        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            // Instead of a ClassCastException, throw an exception with some
            // more information.
            if (object instanceof Reference) {
                Reference ref = (Reference) object;
                handleException("JNDI failed to de-reference Reference with name " + name + "; is the factory " + ref
                        .getFactoryClassName() + " in your classpath?");
                return null;
            } else {
                handleException(
                        "JNDI lookup of name " + name + " returned a " + object.getClass().getName() + " while a "
                                + clazz + " was expected");
                return null;
            }
        }
    }

    protected static void handleException(String s) throws NamingException {
        log.error(s);
        throw new NamingException(s);
    }

    public static CarbonMessage createJMSCarbonMessage(Message message) {
        CarbonMessage jmsCarbonMessage = null;
        try {
            if (message instanceof TextMessage) {
                jmsCarbonMessage = new TextJMSCarbonMessage(((TextMessage) message).getText());
                jmsCarbonMessage.setProperty(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.TEXT_MESSAGE_TYPE);

            } else if (message instanceof BytesMessage) {

            } else if (message instanceof MapMessage) {

            } else if (message instanceof ObjectMessage) {

            } else if (message instanceof StreamMessage) {

            }
        } catch (JMSException e) {
            log.error("Error while chaging the jms message to carbon message");
        }
        return jmsCarbonMessage;
    }
}
