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
import org.wso2.carbon.transport.jms.message.JMSMessage;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;


/**
 * Message Listener
 */
public class JMSMessageListener implements javax.jms.MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(JMSMessageListener.class);
    private CarbonMessageProcessor carbonMessageProcessor;

    public JMSMessageListener(CarbonMessageProcessor messageProcessor) {
        this.carbonMessageProcessor = messageProcessor;
    }

    /**
     * Override this method and add the operation which is needed to be done when a message is arrived
     *
     * @param message - the next received message
     */
    @Override public void onMessage(Message message) {
        try {
            TextMessage receivedMessage = (TextMessage) message;
            ByteBuffer byteBuffer = str_to_bb(receivedMessage.getText(), Charset.forName("UTF-8"));
            CarbonMessage carbonMessage = new JMSMessage();
            carbonMessage.addMessageBody(byteBuffer);
            carbonMessage.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, "JMS");
            carbonMessageProcessor.receive(carbonMessage, null);
            LOG.info("Got the message ==> " + receivedMessage.getText());
        } catch (JMSException e) {

        } catch (Exception e) {
            LOG.info("saasas");
        }

    }

    public static ByteBuffer str_to_bb(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    public static String bb_to_str(ByteBuffer buffer, Charset charset) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, charset);
    }

}
