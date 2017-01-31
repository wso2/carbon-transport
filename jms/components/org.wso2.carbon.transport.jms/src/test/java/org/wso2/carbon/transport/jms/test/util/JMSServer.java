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

package org.wso2.carbon.transport.jms.test.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

/**
 * A simple jms server using Activemq embedded broker.
 */
public class JMSServer {
    private Logger logger = LoggerFactory.getLogger(JMSServer.class);
    private ConnectionFactory connectionFactory;

    /**
     * To start the embedded activemq server
     */
    public void startServer() {
        connectionFactory = new ActiveMQConnectionFactory(JMSTestConstants.ACTIVEMQ_PROVIDER_URL);
    }

    /**
     * To publish the messages to a queue
     *
     * @throws JMSException         JMS Exception
     * @throws InterruptedException Interrupted exception while waiting in between messages
     */
    public void publishMessagesToQueue() throws JMSException, InterruptedException {
        QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Destination destination = queueSession.createQueue(JMSTestConstants.QUEUE_NAME);
        MessageProducer queueSender = queueSession.createProducer(destination);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int index = 0; index < 10; index++) {
            String queueText = "Queue Message : " + (index + 1);
            TextMessage queueMessage = queueSession.createTextMessage(queueText);
            queueSender.send(queueMessage);
            logger.info("Publishing " + queueText + " to queue " + JMSTestConstants.QUEUE_NAME);
            Thread.sleep(1000);
        }
        queueConn.close();
        queueSession.close();
        queueSender.close();
    }

    /**
     * To publish the messages to a queue
     *
     * @throws JMSException         JMS Exception
     * @throws InterruptedException Interrupted exception while waiting in between messages
     */
    public void publishMessagesToTopic() throws JMSException, InterruptedException {
        TopicConnection topicConnection = (TopicConnection) connectionFactory.createConnection();
        topicConnection.start();
        TopicSession topicSession = topicConnection.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Destination destination = topicSession.createTopic(JMSTestConstants.TOPIC_NAME);
        MessageProducer topicSender = topicSession.createProducer(destination);
        topicSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int index = 0; index < 10; index++) {
            String topicText = "Topic Message : " + (index + 1);
            TextMessage topicMessage = topicSession.createTextMessage(topicText);
            topicSender.send(topicMessage);
            logger.info("Publishing " + topicText + " to topic " + JMSTestConstants.TOPIC_NAME);
            Thread.sleep(1000);
        }
        topicConnection.close();
        topicSession.close();
        topicSender.close();
    }
}
