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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.clients.websocket.WebSocketClient;
import org.wso2.carbon.transport.http.netty.util.clients.websocket.WebSocketClientHandler;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Test class for WebSocket Upgrade
 */
public class PassTroughWebSocketUpgradeTestCase {

    Logger logger = LoggerFactory.getLogger(PassTroughWebSocketUpgradeTestCase.class);
    private List<HTTPServerConnector> serverConnectors;
    private HTTPServer httpServer;
    private WebSocketClient client = new WebSocketClient();
    private static final String testValue = "Test Message";

    @BeforeClass
    public void setup() {
        TransportsConfiguration configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");
        serverConnectors = TestUtil.startConnectors(configuration, new WebSocketPassthroughMessageProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT, testValue, Constants.TEXT_PLAIN);
    }

    @Test(description = "Test the handshake of WebSocket")
    public void testHandshake() throws Exception {
        try {
            client.handshake(TestUtil.TEST_SERVER_PORT);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Assert.assertTrue(false, "Interrupt exception occurred.");
            }
            Assert.assertTrue(WebSocketClientHandler.isHandshakeSuccessful());
            logger.info("Handshake test completed.");
        } catch (InterruptedException e) {
            logger.error("Handshake interruption.");
            assertTrue(false);
        }
    }

    @Test(description = "Send and receive text messages")
    public void testTextMessage() {
        String testText = "test";
        client.sendText(testText);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Assert.assertTrue(false, "Interrupt exception occurred.");
        }
        String receivesText = WebSocketClientHandler.getReceivedText();
        logger.info("Sent text : " + testText + " -> " + "received text : " + receivesText);
        Assert.assertEquals(receivesText, testText, "Not received the same text sent");
    }

    @AfterClass
    public void cleaUp() throws ServerConnectorException {
        TestUtil.cleanUp(serverConnectors, httpServer);
    }
}
