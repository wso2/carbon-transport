package org.wso2.carbon.transport.jms.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.PollingTransportListener;
import org.wso2.carbon.transport.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This is a transport listener for JMS
 */
public class JMSTransportListener extends PollingTransportListener {
    private Logger logger = LoggerFactory.getLogger(JMSTransportListener.class);
    private CarbonMessageProcessor carbonMessageProcessor;
    private String serviceId;
    private JMSConnectionFactory jmsConnectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer messageConsumer;

    //TODO :Remove these. These are added for testing purposes
    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private int id;
    String userName = "admin";
    String password = "admin";
    private static final String CARBON_CLIENT_ID = "carbon";
    private static final String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static final String CARBON_DEFAULT_HOSTNAME = "localhost";
    private static final String CARBON_DEFAULT_PORT = "5672";
    String topicName = "foo.bar";
    String queueName = "queue";

    public JMSTransportListener(String id) {
        super(id);
        this.serviceId = id;
    }

    @Override
    public void listen(Map<String, String> map) {
        try {
            Properties properties = new Properties();
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> entry : set) {
                String mappedParameter = JMSConstants.MAPPING_PARAMETERS.get(entry.getKey());
                if (mappedParameter != null) {
                    properties.put(mappedParameter, entry.getValue());
                }
            }
            jmsConnectionFactory = new JMSConnectionFactory(properties);
            connection = jmsConnectionFactory.getConnection();
            jmsConnectionFactory.start(connection);
            session = jmsConnectionFactory.getSession(connection);
            destination = jmsConnectionFactory.getDestination(session);
            messageConsumer = jmsConnectionFactory.createMessageConsumer(session, destination);
            messageConsumer.setMessageListener(
                    new JMSMessageListener(carbonMessageProcessor));
            publishMessagesToQueue();
        } catch (NamingException e) {
            logger.error("Error in initing the message listener");
        } catch (JMSException e) {
            logger.error("Error in initing the message listener");
        } catch (InterruptedException e) {
            logger.error("Error in initing the message listener");
        }

    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.carbonMessageProcessor = carbonMessageProcessor;
    }

    @Override
    public CarbonMessageProcessor getMessageProcessor() {
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
    public String getProtocol() {
        return JMSConstants.PROTOCOL_JMS;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void start() {
        // Not needed for JMS Transport
    }

    @Override
    protected void stop() {
        // Not needed for JMS Transport
    }

    @Override
    protected void beginMaintenance() {

    }

    @Override
    protected void endMaintenance() {

    }

    private void publishMessagesToQueue() throws NamingException, JMSException, InterruptedException {
        InitialContext ctx = initQueue();
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
        QueueConnection connection = connFactory.createQueueConnection();
        connection.start();
        QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

        Queue queue = (Queue) ctx.lookup(queueName);
        javax.jms.QueueSender queueSender = session.createSender(queue);
        for (int i = 0; i < 10; i++) {
            TextMessage queueMessage = session.createTextMessage(" Queue Message " + id + "-" + (i + 1));
            queueSender.send(queueMessage);
            Thread.sleep(1000);
        }

        connection.close();
        session.close();
        queueSender.close();
    }

    private InitialContext initQueue() throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));
        properties.put("queue." + queueName, queueName);
        properties.put("topic." + topicName, topicName);
        InitialContext ctx = new InitialContext(properties);
        return ctx;
    }

    private String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer().append("amqp://").append(username).append(":").append(password).append("@")
                .append(CARBON_CLIENT_ID).append("/").append(CARBON_VIRTUAL_HOST_NAME).append("?brokerlist='tcp://")
                .append(CARBON_DEFAULT_HOSTNAME).append(":").append(CARBON_DEFAULT_PORT).append("'").toString();
    }
}
