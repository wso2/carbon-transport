/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.transport.jms.listener.JMSTransportListener;
import org.wso2.carbon.transport.jms.test.util.JMSServer;
import org.wso2.carbon.transport.jms.test.util.JMSTestConstants;
import org.wso2.carbon.transport.jms.test.util.MessageProcessor;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * A test class for testing queue listening and topic listening
 */
public class QueueTopicListeningTestCase {
    private JMSServer jmsServer;
    private JMSTransportListener jmsQueueTransportListener;
    private Map<String, String> queueListeningParametes;
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueTopicListeningTestCase.class);

    @BeforeClass(groups = "jmsListening", description = "Setting up the server, JMS listener and message processor")
    public void setUp() {
        queueListeningParametes = new HashMap<>();
        queueListeningParametes.put(JMSConstants.DESTINATION_PARAM_NAME, JMSTestConstants.QUEUE_NAME);
        queueListeningParametes
                .put(JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME, JMSTestConstants.QUEUE_CONNECTION_FACTORY);
        queueListeningParametes
                .put(JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_FACTORY_INITIAL);
        queueListeningParametes.put(JMSConstants.PROVIDER_URL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_PROVIDER_URL);
        queueListeningParametes
                .put(JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME, JMSConstants.DESTINATION_TYPE_QUEUE);

        Map<String, String> topicListeningParametes = new HashMap<>();
        topicListeningParametes.put(JMSConstants.DESTINATION_PARAM_NAME, JMSTestConstants.TOPIC_NAME);
        topicListeningParametes
                .put(JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME, JMSTestConstants.TOPIC_CONNECTION_FACTORY);
        topicListeningParametes
                .put(JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_FACTORY_INITIAL);
        topicListeningParametes.put(JMSConstants.PROVIDER_URL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_PROVIDER_URL);
        topicListeningParametes
                .put(JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME, JMSConstants.DESTINATION_TYPE_TOPIC);
        jmsServer = new JMSServer();
        jmsServer.startServer();

        // Create a queue transport listener
        jmsQueueTransportListener = new JMSTransportListener("1");
        MessageProcessor messageProcessor = new MessageProcessor();
        jmsQueueTransportListener.setMessageProcessor(messageProcessor);
        jmsQueueTransportListener.poll(queueListeningParametes);

        // Create a topic transport listener
        JMSTransportListener jmsTopicTransportListener = new JMSTransportListener("2");
        jmsTopicTransportListener.setMessageProcessor(messageProcessor);
        jmsTopicTransportListener.poll(topicListeningParametes);
    }

    @Test(groups = "jmsListening", description = "Testing whether queue listening is working correctly without any "
            + "exceptions")
    public void queueListeningTestCase() {
        try {
            jmsQueueTransportListener.poll(queueListeningParametes);
            LOGGER.info("JMS Transport Listener is starting to listen to the queue " + JMSTestConstants.QUEUE_NAME);
            jmsServer.publishMessagesToQueue();
            Thread.sleep(10000);
        } catch (Exception e) {
            Assert.fail("Error while listing to queue");
        }
    }

    @Test(groups = "jmsListening", description = "Testing whether topic listening is working correctly without any "
            + "exceptions")
    public void topicListeningTestCase() {
        try {
            jmsQueueTransportListener.poll(queueListeningParametes);
            LOGGER.info("JMS Transport Listener is starting to listen to the topic " + JMSTestConstants.TOPIC_NAME);
            jmsServer.publishMessagesToTopic();
            Thread.sleep(10000);
        } catch (Exception e) {
            Assert.fail("Error while listing to topic");
        }
    }

}
