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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.jms.listener.JMSTransportListener;
import org.wso2.carbon.transport.jms.test.util.JMSServer;
import org.wso2.carbon.transport.jms.test.util.JMSTestConstants;
import org.wso2.carbon.transport.jms.test.util.MessageProcessor;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

/**
 * A test class for testing queue listening
 */
public class QueueListeningTestCase {
    private JMSServer jmsServer;
    private JMSTransportListener jmsTransportListener;
    private Map<String, String> listeningParametes;
    private MessageProcessor messageProcessor;
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueListeningTestCase.class);

    @BeforeClass(groups = "queueListening", description = "Setting up the server, JMS listener and message processor")
    public void setUp() {
        listeningParametes = new HashMap<>();
        listeningParametes.put(JMSConstants.DESTINATION_PARAM_NAME, JMSTestConstants.QUEUE_NAME);
        listeningParametes
                .put(JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME, JMSTestConstants.QUEUE_CONNECTION_FACTORY);
        listeningParametes
                .put(JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_FACTORY_INITIAL);
        listeningParametes.put(JMSConstants.PROVIDER_URL_PARAM_NAME, JMSTestConstants.ACTIVEMQ_PROVIDER_URL);
        listeningParametes.put(JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME, JMSConstants.DESTINATION_TYPE_QUEUE);
        jmsServer = new JMSServer();
        jmsServer.startServer();
        jmsTransportListener = new JMSTransportListener("1");
        messageProcessor = new MessageProcessor();
        jmsTransportListener.setMessageProcessor(messageProcessor);
    }

    @Test(groups = "queueListening", description = "Testing whether queue listening is working correctly without any "
            + "exceptions")
    public void queueListeningTestCase() throws InterruptedException, JMSException {
        jmsTransportListener.listen(listeningParametes);
        LOGGER.info("JMS Transport Listener is starting to listen to the queue " + JMSTestConstants.QUEUE_NAME);
        jmsServer.publishMessagesToQueue();
    }

    @AfterClass(groups = "queueListening", description = "Closing the connection with the message broker")
    public void cleanUp() {
        jmsTransportListener.stopListening();
    }

}
