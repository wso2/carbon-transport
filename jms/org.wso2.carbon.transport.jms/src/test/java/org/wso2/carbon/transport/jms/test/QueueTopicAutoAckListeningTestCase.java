/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.jms.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.jms.listener.JMSServerConnector;
import org.wso2.carbon.transport.jms.test.util.JMSServer;
import org.wso2.carbon.transport.jms.test.util.JMSTestConstants;
import org.wso2.carbon.transport.jms.test.util.TestMessageProcessor;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;

/**
 * A test class for testing queue listening and topic listening in auto ack mode.
 */
public class QueueTopicAutoAckListeningTestCase {
    private JMSServer jmsServer;
    private TestMessageProcessor queueTestMessageProcessor;
    private TestMessageProcessor topicTestMessageProcessor;
    private Map<String, String> queueListeningParameters;
    private Map<String, String> topicListeningParameters;
    private JMSServerConnector jmsQueueTransportListener;
    private JMSServerConnector jmsTopicTransportListener;
    private static final Logger logger = LoggerFactory.getLogger(QueueTopicAutoAckListeningTestCase.class);

    @BeforeClass(groups = "jmsListening", description = "Setting up the server, JMS listener and message processor")
    public void setUp() throws ServerConnectorException {
        queueListeningParameters = new HashMap<>();
        queueListeningParameters.put(JMSConstants.DESTINATION_PARAM_NAME, JMSTestConstants.QUEUE_NAME);
        queueListeningParameters
                .put(JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME, JMSTestConstants.QUEUE_CONNECTION_FACTORY);
        queueListeningParameters
                .put(JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_FACTORY_INITIAL);
        queueListeningParameters.put(JMSConstants.PROVIDER_URL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_PROVIDER_URL);
        queueListeningParameters
                .put(JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME, JMSConstants.DESTINATION_TYPE_QUEUE);

        topicListeningParameters = new HashMap<>();
        topicListeningParameters.put(JMSConstants.DESTINATION_PARAM_NAME, JMSTestConstants.TOPIC_NAME);
        topicListeningParameters
                .put(JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME, JMSTestConstants.TOPIC_CONNECTION_FACTORY);
        topicListeningParameters
                .put(JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_FACTORY_INITIAL);
        topicListeningParameters.put(JMSConstants.PROVIDER_URL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_PROVIDER_URL);
        topicListeningParameters
                .put(JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME, JMSConstants.DESTINATION_TYPE_TOPIC);
        jmsServer = new JMSServer();
        jmsServer.startServer();

        // Create a queue transport listener
        jmsQueueTransportListener = new JMSServerConnector("1");
        queueTestMessageProcessor = new TestMessageProcessor();
        jmsQueueTransportListener.setMessageProcessor(queueTestMessageProcessor);

        // Create a topic transport listener
        jmsTopicTransportListener = new JMSServerConnector("2");
        topicTestMessageProcessor = new TestMessageProcessor();
        jmsTopicTransportListener.setMessageProcessor(topicTestMessageProcessor);
    }

    /**
     * JMS Server connector will start listening to a queue in auto ack mode and there will be a publisher publishing to
     * the queue. After publishing the messages to queue, check whether the count of messages received is equal to
     * number of messages sent.
     */
    @Test(groups = "jmsListening", description = "Testing whether queue listening is working correctly without any "
            + "exceptions in auto ack mode")
    public void queueListeningTestCase() throws ServerConnectorException, InterruptedException, JMSException {
        jmsQueueTransportListener.start(queueListeningParameters);
        logger.info("JMS Transport Listener is starting to listen to the queue " + JMSTestConstants.QUEUE_NAME);
        jmsServer.publishMessagesToQueue(JMSTestConstants.QUEUE_NAME);
        Assert.assertEquals(queueTestMessageProcessor.getCount(), 10,
                "Expected message count is not received when " + "listening to queue "
                        + JMSTestConstants.QUEUE_NAME);
        jmsQueueTransportListener.stop();
    }

    /**
     * JMS Server connector will start listening to a topic in auto ack mode and there will be a publisher publishing
     * to the queue.After publishing the messages to queue, check whether the count of messages received is equal to
     * number of messages sent.
     */
    @Test(groups = "jmsListening", description = "Testing whether topic listening is working correctly without any "
            + "exceptions in auto ack mode")
    public void topicListeningTestCase() throws InterruptedException, JMSException, ServerConnectorException {
        jmsTopicTransportListener.start(topicListeningParameters);
        logger.info("JMS Transport Listener is starting to listen to the topic " + JMSTestConstants.TOPIC_NAME);
        jmsServer.publishMessagesToTopic(JMSTestConstants.TOPIC_NAME);
        Assert.assertEquals(topicTestMessageProcessor.getCount(), 10,
                "Expected message count is not received when " + "listening to topic "
                        + JMSTestConstants.TOPIC_NAME);
        jmsTopicTransportListener.stop();
    }

}
