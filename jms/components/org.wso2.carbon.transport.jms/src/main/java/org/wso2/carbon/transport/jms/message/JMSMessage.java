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

package org.wso2.carbon.transport.jms.message;

import org.wso2.carbon.messaging.CarbonMessage;

import javax.jms.Destination;

/**
 * JMS based representation for CarbonMessage
 */
public class JMSMessage extends CarbonMessage {
    private static final String JMS_MESSAGE_ID = "JMS_MESSAGE_ID";
    private static final String JMS_TIME_STAMP = "JMS_TIME_STAMP";
    private static final String JMS_CORRELATION_ID_AS_BYTES = "JMS_CORRELATION_ID_AS_BYTES";
    private static final String JMS_CORRELATION_ID = "JMS_CORRELATION_ID";
    private static final String JMS_REPLY_TO = "JMS_REPLY_TO";
    private static final String JMS_DESTINATION = "JMS_DESTINATION";
    private static final String JMS_DELIVERY_MODE = "JMS_DELIVERY_MODE";
    private static final String JMS_TYPE = "JMS_TYPE";

    public void setJMSMessageID(String messageID) {
        setProperty(JMS_MESSAGE_ID, messageID);
    }

    public void setJMSTimeStamp(long timeStamp) {
        setProperty(JMS_TIME_STAMP, timeStamp);
    }

    public void setJMSCorrelationIDAsBytes(byte[] correlationId) {
        setProperty(JMS_CORRELATION_ID_AS_BYTES, correlationId);
    }

    public void setJMSCorrelationID(String correlationID) {
        setProperty(JMS_CORRELATION_ID, correlationID);
    }

    public void setJMSReplyTo(Destination destination) {
        setProperty(JMS_REPLY_TO, destination);
    }

    public void setJMSDestination(Destination destination) {
        setProperty(JMS_DESTINATION, destination);
    }

    public void setJMSDeliveryMode(int deliveryMode) {
        setProperty(JMS_DELIVERY_MODE, deliveryMode);
    }

    public void setJMSType(String jmsType) {
        setProperty(JMS_TYPE, jmsType);
    }

}
