/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.transport.jms.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TextJMSCarbonMessage;
import org.wso2.carbon.transport.jms.utils.JMSConstants;
import org.wso2.carbon.transport.jms.utils.JMSUtils;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Message Listener
 */
class JMSMessageListener implements javax.jms.MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(JMSMessageListener.class);
    private CarbonMessageProcessor carbonMessageProcessor;
    private String serviceId;

    public JMSMessageListener(CarbonMessageProcessor messageProcessor, String serviceId) {
        this.carbonMessageProcessor = messageProcessor;
        this.serviceId = serviceId;
    }

    /**
     * Override this method and add the operation which is needed to be done when a message is arrived
     *
     * @param message - the next received message
     */
    @Override
    public void onMessage(Message message) {
        try {
            CarbonMessage jmsCarbonMessage = JMSUtils.createJMSCarbonMessage(message);
            jmsCarbonMessage.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
            jmsCarbonMessage.setProperty(JMSConstants.JMS_SERVICE_ID, serviceId);
            carbonMessageProcessor.receive(jmsCarbonMessage, null);
            TextJMSCarbonMessage textJMSCarbonMessage = (TextJMSCarbonMessage) jmsCarbonMessage;
            LOG.info("Got the message ==> " + textJMSCarbonMessage.getText());
        } catch (JMSException e) {
            throw new RuntimeException("Error while getting the message from jms server");
        } catch (Exception e) {
            throw new RuntimeException("Error while sending the messages to message processor");
        }
    }


}
