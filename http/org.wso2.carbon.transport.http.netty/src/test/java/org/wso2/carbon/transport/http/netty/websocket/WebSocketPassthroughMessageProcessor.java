/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.websocket.Session;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class WebSocketPassthroughMessageProcessor implements CarbonMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketPassthroughMessageProcessor.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ClientConnector clientConnector;

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String protocol = (String) carbonMessage.getProperty(Constants.PROTOCOL);
                if (protocol.equals(Constants.WEBSOCKET_PROTOCOL)) {
                    if (carbonMessage instanceof TextCarbonMessage) {
                        String text = ((TextCarbonMessage) carbonMessage).getText();
                        Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SESSION);
                        try {
                            session.getBasicRemote().sendText(text.toUpperCase());
                        } catch (IOException e) {
                            logger.error("Error occurred when sending the message back");
                        }
                    }
                }
            }
        });

        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
        this.clientConnector = clientConnector;
    }

    @Override
    public String getId() {
        return "passthrough";
    }
}
