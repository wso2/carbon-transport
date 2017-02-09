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

package org.wso2.carbon.transport.jms.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.jms.exception.JMSServerConnectorException;
import org.wso2.carbon.transport.jms.utils.JMSConstants;
import org.wso2.carbon.transport.jms.utils.JMSUtils;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * JMSConnectionFactory that handles the JMS Connection, Session creation, closing
 */
public class JMSConnectionFactory implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory {
    private static final Log logger = LogFactory.getLog(JMSConnectionFactory.class.getName());
    private Context ctx;
    private ConnectionFactory connectionFactory;
    private String connectionFactoryString;
    private JMSConstants.JMSDestinationType destinationType;
    private Destination destination;
    private String destinationName;
    private boolean transactedSession = false;
    private int sessionAckMode = Session.AUTO_ACKNOWLEDGE;
    private String jmsSpec;
    private boolean isDurable;
    private boolean noPubSubLocal;
    private String clientId;
    private String subscriptionName;
    private String messageSelector;
    private boolean isSharedSubscription;

    /**
     * Initialization of JMS ConnectionFactory with the user specified properties
     *
     * @param properties Properties to be added to the initial context
     */
    public JMSConnectionFactory(Properties properties) throws JMSServerConnectorException {
        try {
            ctx = new InitialContext(properties);
        } catch (NamingException e) {
            logger.error("NamingException while obtaining initial context. " + e.getMessage());
            throw new JMSServerConnectorException("NamingException while obatining initial context. " + e.getMessage());
        }

        String connectionFactoryType = properties.getProperty(JMSConstants.CONNECTION_FACTORY_TYPE);
        if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(connectionFactoryType)) {
            this.destinationType = JMSConstants.JMSDestinationType.TOPIC;
        } else {
            this.destinationType = JMSConstants.JMSDestinationType.QUEUE;
        }

        if (properties.getProperty(JMSConstants.PARAM_JMS_SPEC_VER) == null || JMSConstants.JMS_SPEC_VERSION_1_1
                .equals(properties.getProperty(JMSConstants.PARAM_JMS_SPEC_VER))) {
            jmsSpec = JMSConstants.JMS_SPEC_VERSION_1_1;
        } else if (JMSConstants.JMS_SPEC_VERSION_2_0.equals(properties.getProperty(JMSConstants.PARAM_JMS_SPEC_VER))) {
            jmsSpec = JMSConstants.JMS_SPEC_VERSION_2_0;
        } else {
            jmsSpec = JMSConstants.JMS_SPEC_VERSION_1_0;
        }

        isSharedSubscription = "true"
                .equalsIgnoreCase(properties.getProperty(JMSConstants.PARAM_IS_SHARED_SUBSCRIPTION));

        noPubSubLocal = Boolean.valueOf(properties.getProperty(JMSConstants.PARAM_PUBSUB_NO_LOCAL));

        clientId = properties.getProperty(JMSConstants.PARAM_DURABLE_SUB_CLIENT_ID);
        subscriptionName = properties.getProperty(JMSConstants.PARAM_DURABLE_SUB_NAME);

        if (isSharedSubscription && subscriptionName == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Subscription name is not given. Therefor declaring a non-shared subscription");
            }
            isSharedSubscription = false;
        }

        String subDurable = properties.getProperty(JMSConstants.PARAM_SUB_DURABLE);
        if (subDurable != null) {
            isDurable = Boolean.parseBoolean(subDurable);
        }
        String msgSelector = properties.getProperty(JMSConstants.PARAM_MSG_SELECTOR);
        if (msgSelector != null) {
            messageSelector = msgSelector;
        }
        this.connectionFactoryString = properties.getProperty(JMSConstants.CONNECTION_FACTORY_JNDI_NAME);
        if (connectionFactoryString == null || "".equals(connectionFactoryString)) {
            connectionFactoryString = "QueueConnectionFactory";
        }

        this.destinationName = properties.getProperty(JMSConstants.DESTINATION_NAME);
        if (destinationName == null || "".equals(destinationName)) {
            destinationName = "QUEUE_" + System.currentTimeMillis();
        }

        String strSessionAck = properties.getProperty(JMSConstants.SESSION_ACK);
        if (null == strSessionAck) {
            sessionAckMode = Session.AUTO_ACKNOWLEDGE;
        } else if (strSessionAck.equals(JMSConstants.CLIENT_ACKNOWLEDGE_MODE)) {
            sessionAckMode = Session.CLIENT_ACKNOWLEDGE;
        } else if (strSessionAck.equals(JMSConstants.DUPS_OK_ACKNOWLEDGE_MODE)) {
            sessionAckMode = Session.DUPS_OK_ACKNOWLEDGE;
        } else if (strSessionAck.equals(JMSConstants.SESSION_TRANSACTED_MODE)) {
            sessionAckMode = Session.SESSION_TRANSACTED;
            transactedSession = true;
        }
        createConnectionFactory();
    }

    /**
     * To create the JMS Connection Factory
     *
     * @return JMS Connection Factory
     */
    private ConnectionFactory createConnectionFactory() throws JMSServerConnectorException {
        if (this.connectionFactory != null) {
            return this.connectionFactory;
        }
        try {
            if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                this.connectionFactory = (QueueConnectionFactory) ctx.lookup(this.connectionFactoryString);
            } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                this.connectionFactory = (TopicConnectionFactory) ctx.lookup(this.connectionFactoryString);
            }
        } catch (NamingException e) {
            logger.error(
                    "Naming exception while obtaining connection factory for '" + this.connectionFactoryString + "'."
                            + e.getMessage());
            throw new JMSServerConnectorException(
                    "Naming exception while obtaining connection factory for " + this.connectionFactoryString + ". " + e
                            .getMessage());
        }
        return this.connectionFactory;
    }

    @Override
    public Connection createConnection() throws JMSException {
        if (connectionFactory == null) {
            logger.error("Connection cannot be establish to the broker. Please check the broker libs provided.");
            return null;
        }
        Connection connection = null;
        try {

            if (JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec)) {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    connection = ((QueueConnectionFactory) (this.connectionFactory)).createQueueConnection();
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    connection = ((TopicConnectionFactory) (this.connectionFactory)).createTopicConnection();
                    if (isDurable) {
                        connection.setClientID(clientId);
                    }
                }
                return connection;
            } else {
                QueueConnectionFactory qConFac = null;
                TopicConnectionFactory tConFac = null;
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    qConFac = (QueueConnectionFactory) this.connectionFactory;
                } else {
                    tConFac = (TopicConnectionFactory) this.connectionFactory;
                }
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection();
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection();
                }
                if (isDurable && !isSharedSubscription) {
                    connection.setClientID(clientId);
                }
                return connection;
            }
        } catch (Exception e) {
            logger.error("JMS Exception while creating connection through factory " + this.connectionFactoryString);

            // Need to close the connection in the case if durable subscriptions
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    logger.error("Error while closing the connection");
                }
            }
            throw e;
        }

    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        Connection connection = null;
        try {
            if (JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec)) {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    connection = ((QueueConnectionFactory) (this.connectionFactory))
                            .createQueueConnection(userName, password);
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    connection = ((TopicConnectionFactory) (this.connectionFactory))
                            .createTopicConnection(userName, password);
                    if (isDurable) {
                        connection.setClientID(clientId);
                    }
                }
                return connection;
            } else {
                QueueConnectionFactory qConFac = null;
                TopicConnectionFactory tConFac = null;
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    qConFac = (QueueConnectionFactory) this.connectionFactory;
                } else {
                    tConFac = (TopicConnectionFactory) this.connectionFactory;
                }
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection(userName, password);
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection(userName, password);
                }
                if (isDurable && !isSharedSubscription) {
                    connection.setClientID(clientId);
                }
                return connection;
            }
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while creating connection through factory '" + this.connectionFactoryString + "' "
                            + e.getMessage());
            // Need to close the connection in the case if durable subscriptions
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    logger.error("Error while closing the connection");
                }
            }
            throw e;
        }
    }

    @Override public JMSContext createContext() {
        return connectionFactory.createContext();
    }

    @Override public JMSContext createContext(int sessionMode) {
        return connectionFactory.createContext(sessionMode);
    }

    @Override public JMSContext createContext(String userName, String password) {
        return connectionFactory.createContext(userName, password);
    }

    @Override public JMSContext createContext(String userName, String password, int sessionMode) {
        return connectionFactory.createContext(userName, password, sessionMode);
    }

    @Override public QueueConnection createQueueConnection() throws JMSException {
        try {
            return ((QueueConnectionFactory) (this.connectionFactory)).createQueueConnection();
        } catch (JMSException e) {
            logger.error("JMS Exception while creating queue connection through factory " + this.connectionFactoryString
                    + ". " + e.getMessage());
            throw e;
        }
    }

    @Override public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        try {
            return ((QueueConnectionFactory) (this.connectionFactory)).createQueueConnection(userName, password);
        } catch (JMSException e) {
            logger.error("JMS Exception while creating queue connection through factory " + this.connectionFactoryString
                    + ". " + e.getMessage());
            throw e;
        }
    }

    @Override public TopicConnection createTopicConnection() throws JMSException {
        try {
            return ((TopicConnectionFactory) (this.connectionFactory)).createTopicConnection();
        } catch (JMSException e) {
            logger.error("JMS Exception while creating topic connection through factory " + this.connectionFactoryString
                    + ". " + e.getMessage());
            throw e;
        }
    }

    @Override public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        try {
            return ((TopicConnectionFactory) (this.connectionFactory)).createTopicConnection(userName, password);
        } catch (JMSException e) {
            logger.error("JMS Exception while creating topic connection through factory " + this.connectionFactoryString
                    + ". " + e.getMessage());
            throw e;
        }
    }

    /**
     * To get the destination of the particular session
     *
     * @param session JMS session that we need to find the destination
     * @return destination the particular is related with
     */
    public Destination getDestination(Session session) throws JMSServerConnectorException {
        if (this.destination != null) {
            return this.destination;
        }
        return createDestination(session);
    }

    /**
     * Create a message consumer for particular session and destination
     *
     * @param session     JMS Session to create the consumer
     * @param destination JMS destination which the consumer should listen to
     * @return Message Consumer, who is listening in particular destination with the given session
     */
    public MessageConsumer createMessageConsumer(Session session, Destination destination)
            throws JMSServerConnectorException {
        try {
            if (JMSConstants.JMS_SPEC_VERSION_2_0.equals(jmsSpec) && isSharedSubscription) {
                if (isDurable) {
                    return session.createSharedDurableConsumer((Topic) destination, subscriptionName, messageSelector);
                } else {
                    return session.createSharedConsumer((Topic) destination, subscriptionName, messageSelector);
                }
            } else if ((JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec)) || (
                    JMSConstants.JMS_SPEC_VERSION_2_0.equals(jmsSpec) && !isSharedSubscription)) {
                if (isDurable) {
                    return session.createDurableSubscriber((Topic) destination, subscriptionName, messageSelector,
                            noPubSubLocal);
                } else {
                    return session.createConsumer(destination, messageSelector);
                }
            } else {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    return ((QueueSession) session).createReceiver((Queue) destination, messageSelector);
                } else {
                    if (isDurable) {
                        return ((TopicSession) session)
                                .createDurableSubscriber((Topic) destination, subscriptionName, messageSelector,
                                        noPubSubLocal);
                    } else {
                        return ((TopicSession) session).createSubscriber((Topic) destination, messageSelector, false);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("JMS Exception while creating consumer for the destination " + destinationName + ". " + e
                    .getMessage());
            throw new JMSServerConnectorException(
                    "JMS Exception while creating consumer for the destination " + destinationName, e);
        }
    }

    /**
     * Create a message producer for particular session and destination
     *
     * @param session     JMS Session to create the producer
     * @param destination JMS destination which the producer should publish to
     * @return MessageProducer, who publish messages to particular destination with the given session
     */
    public MessageProducer createMessageProducer(Session session, Destination destination)
            throws JMSServerConnectorException {
        try {
            if ((JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec)) || (JMSConstants.JMS_SPEC_VERSION_2_0
                    .equals(jmsSpec))) {
                return session.createProducer(destination);
            } else {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    return ((QueueSession) session).createProducer((Queue) destination);
                } else {
                    return ((TopicSession) session).createPublisher((Topic) destination);
                }
            }
        } catch (JMSException e) {
            logger.error("JMS Exception while creating producer for the dstination  " + destinationName + ". " + e
                    .getMessage());
            throw new JMSServerConnectorException(
                    "JMS Exception while creating the producer for the destination " + destinationName, e);
        }
    }

    /**
     * To create a destination for particular session
     *
     * @param session Specific session to create the destination
     * @return destination for particular session
     */
    private Destination createDestination(Session session) throws JMSServerConnectorException {
        this.destination = createDestination(session, this.destinationName);
        return this.destination;
    }

    /**
     * To create the destination
     *
     * @param session         relevant session to create the destion
     * @param destinationName Destination jms destionation
     * @return the destination that is created from session
     */
    private Destination createDestination(Session session, String destinationName) throws JMSServerConnectorException {
        Destination destination = null;
        try {
            if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                destination = JMSUtils.lookupDestination(ctx, destinationName, JMSConstants.DESTINATION_TYPE_QUEUE);
            } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                destination = JMSUtils.lookupDestination(ctx, destinationName, JMSConstants.DESTINATION_TYPE_TOPIC);
            }
        } catch (NameNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find destination '" + destinationName + "' on connection factory for '"
                        + this.connectionFactoryString + "'. " + e.getMessage());
                logger.debug("Creating destination '" + destinationName + "' on connection factory for '"
                        + this.connectionFactoryString + ".");
            }
            /**
             * If the destination is not found already, create the destination
             */
            try {
                if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                    destination = (Queue) session.createQueue(destinationName);
                } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.TOPIC)) {
                    destination = (Topic) session.createTopic(destinationName);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Created '" + destinationName + "' on connection factory for '"
                            + this.connectionFactoryString + "'.");
                }
            } catch (JMSException e1) {
                logger.error("Could not find nor create '" + destinationName + "' on connection factory for "
                        + this.connectionFactoryString + ". " + e1.getMessage());
                throw new JMSServerConnectorException(
                        "Could not find nor create '" + destinationName + "' on connection factory for "
                                + this.connectionFactoryString, e1);
            }
        } catch (NamingException e) {
            logger.error("Naming exception while looking up for the destination name " + destinationName + ". " + e
                    .getMessage());
            throw new JMSServerConnectorException(
                    "Naming exception while looking up for the destination name " + destinationName, e);
        }
        return destination;
    }

    /**
     * To create a session from the given connection
     *
     * @param connection Specific connection which we is needed for creating session
     * @return session created from the given connection
     */
    public Session createSession(Connection connection) throws JMSServerConnectorException {
        try {
            if (JMSConstants.JMS_SPEC_VERSION_1_1.equals(jmsSpec) || JMSConstants.JMS_SPEC_VERSION_2_0
                    .equals(jmsSpec)) {
                return connection.createSession(transactedSession, sessionAckMode);
            } else if (this.destinationType.equals(JMSConstants.JMSDestinationType.QUEUE)) {
                return (QueueSession) ((QueueConnection) (connection))
                        .createQueueSession(transactedSession, sessionAckMode);
            } else {
                return (TopicSession) ((TopicConnection) (connection))
                        .createTopicSession(transactedSession, sessionAckMode);

            }
        } catch (JMSException e) {
            logger.error("JMS Exception while obtaining session for factory " + this.connectionFactoryString + ". " + e
                    .getMessage());
            throw new JMSServerConnectorException(
                    "JMS Exception while obtaining session for factory " + connectionFactoryString, e);
        }
    }

    /**
     * Start the jms connection to start the message delivery
     *
     * @param connection Connection that need to be started
     */
    public void start(Connection connection) throws JMSServerConnectorException {
        try {
            connection.start();
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while starting connection for factory " + this.connectionFactoryString + ". " + e
                            .getMessage());
            throw new JMSServerConnectorException(
                    "JMS Exception while starting connection for factory " + this.connectionFactoryString, e);
        }
    }

    /**
     * Stop the jms connection to stop the message delivery
     *
     * @param connection JMS connection that need to be stopped
     */
    public void stop(Connection connection) throws JMSServerConnectorException {
        try {
            if (connection != null) {
                connection.stop();
            }
        } catch (JMSException e) {
            logger.error(
                    "JMS Exception while stopping connection for factory " + this.connectionFactoryString + ". " + e
                            .getMessage());
            throw new JMSServerConnectorException(
                    "JMS Exception while stopping the connection for factory " + this.connectionFactoryString, e);
        }
    }

    /**
     * Close the jms connection
     *
     * @param connection JMS connection that need to be closed
     */
    public void closeConnection(Connection connection) throws JMSServerConnectorException {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            logger.error("JMS Exception while closing the connection.");
            throw new JMSServerConnectorException("JMS Exception while closing the connection. " + e.getMessage());
        }
    }

    /**
     * To close the session
     *
     * @param session JMS session that need to be closed
     */
    public void closeSession(Session session) throws JMSServerConnectorException {
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            logger.error("JMS Exception while closing the session.");
            throw new JMSServerConnectorException("JMS Exception while closing the session. " + e.getMessage());
        }
    }

    /**
     * To close the message consumer
     *
     * @param messageConsumer Message consumer that need to be closed
     */
    public void closeMessageConsumer(MessageConsumer messageConsumer) throws JMSServerConnectorException {
        try {
            if (messageConsumer != null) {
                messageConsumer.close();
            }
        } catch (JMSException e) {
            logger.error("JMS Exception while closing the subscriber.");
            throw new JMSServerConnectorException("JMS Exception while closing the subscriber. " + e.getMessage());
        }
    }
}
