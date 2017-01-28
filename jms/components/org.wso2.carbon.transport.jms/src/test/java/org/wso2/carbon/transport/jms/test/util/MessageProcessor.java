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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TextJMSCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;

/**
 * Message processor for testing purposes
 */
public class MessageProcessor implements CarbonMessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (carbonMessage instanceof TextJMSCarbonMessage) {
            TextJMSCarbonMessage textJMSCarbonMessage = (TextJMSCarbonMessage) carbonMessage;
            LOGGER.info(textJMSCarbonMessage.getText());
        }
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {}

    @Override
    public String getId() {
        return null;
    }
}
