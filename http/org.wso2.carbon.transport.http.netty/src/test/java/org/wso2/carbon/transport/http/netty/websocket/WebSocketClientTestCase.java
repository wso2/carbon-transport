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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.sender.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.util.List;

/**
 * Test cases for the WebSocket Client implementation.
 */
public class WebSocketClientTestCase {

    private static final Logger log = LoggerFactory.getLogger(WebSocketClientTestCase.class);

    WebSocketMessageProcessor messageProcessor = new WebSocketMessageProcessor();
    ClientConnector clientConnector = new WebSocketClientConnector();
    private List<HTTPServerConnector> serverConnectors;
    private final String url = "ws://localhost:8490/websocket";
    private final String clientId1 = "clientId1";
    private final String clientId2 = "clientId2";
    private final int sleepTime = 100;

    @BeforeClass
    public void setup() {
        log.info(System.lineSeparator() + "-------WebSocket Client Connector Test Cases-------");
        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(configuration, messageProcessor);
        clientConnector.setMessageProcessor(messageProcessor);
    }

    @Test
    public void testTextReceived() throws ClientConnectorException, InterruptedException {
        handshake(clientId1);
        String text = "textText";
        TextCarbonMessage textCarbonMessage = new TextCarbonMessage(text);
        textCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId1);
        clientConnector.send(textCarbonMessage, null);
        Thread.sleep(sleepTime);
        Assert.assertEquals(messageProcessor.getReceivedTextToClient(), text);
        shutDownClient(clientId1);
    }

    private void handshake(String clientId) throws ClientConnectorException {
        StatusCarbonMessage statusCarbonMessage = new StatusCarbonMessage(
                org.wso2.carbon.messaging.Constants.STATUS_OPEN, 0, null);
        statusCarbonMessage.setProperty(Constants.TO, url);
        statusCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId);
        clientConnector.send(statusCarbonMessage, null);
    }

    private void shutDownClient(String clientId) throws ClientConnectorException {
        StatusCarbonMessage statusCarbonMessage = new StatusCarbonMessage(
                org.wso2.carbon.messaging.Constants.STATUS_CLOSE, 1001, "Normal Closure");
        statusCarbonMessage.setProperty(Constants.TO, url);
        statusCarbonMessage.setProperty(Constants.WEBSOCKET_CLIENT_ID, clientId);
        clientConnector.send(statusCarbonMessage, null);
    }
}
