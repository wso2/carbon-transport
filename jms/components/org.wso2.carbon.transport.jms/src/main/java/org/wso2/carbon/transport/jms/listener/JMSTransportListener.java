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

package org.wso2.carbon.transport.jms.listener;

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
import javax.jms.Session;


/**
 * This is a transport listener for JMS
 */
public class JMSTransportListener extends PollingTransportListener {
    private CarbonMessageProcessor carbonMessageProcessor;
    private JMSConnectionFactory jmsConnectionFactory = null;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer messageConsumer;

    public JMSTransportListener(String id) {
        super(id);
    }

    public JMSTransportListener() {
        super("jmsTransportListener");
    }

    @Override
    public void poll(Map<String, String> map) {
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
            if (connection != null) {
                jmsConnectionFactory.start(connection);
                session = jmsConnectionFactory.getSession(connection);
                session.recover();
                destination = jmsConnectionFactory.getDestination(session);
                messageConsumer = jmsConnectionFactory.createMessageConsumer(session, destination);
                messageConsumer.setMessageListener(
                        new JMSMessageListener(carbonMessageProcessor, id, session.getAcknowledgeMode(), session));
            } else {
                throw new RuntimeException("Cannot connect to the JMS Server. Check the connection and try again");
            }
        } catch (JMSException e) {
            throw new RuntimeException("Client libs are added to class path. Please check and try again");
        }
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.carbonMessageProcessor = carbonMessageProcessor;
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
    @SuppressWarnings("unchecked")
    protected void start() {
        // Not needed for JMS Transport
    }

    @Override
    public void stop() {
        jmsConnectionFactory.closeConnection(connection);
        jmsConnectionFactory.closeSession(session);
        jmsConnectionFactory.closeMessageConsumer(messageConsumer);
    }

    @Override
    protected void beginMaintenance() {
        jmsConnectionFactory.stop(connection);
    }

    @Override
    protected void endMaintenance() {
        jmsConnectionFactory.start(connection);
    }
}
