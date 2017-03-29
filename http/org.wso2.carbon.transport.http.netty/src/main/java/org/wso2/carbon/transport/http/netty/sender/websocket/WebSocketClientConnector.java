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

package org.wso2.carbon.transport.http.netty.sender.websocket;

import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLException;

/**
 * WebSocket Client connector which connects to a remote WebSocket Endpoint.This store the WebSocket Clients
 * against a unique ID given by the user. So make sure to adda unique ID the carbon message in order to use
 * this properly.
 */
public class WebSocketClientConnector implements ClientConnector {

    // Map<uniqueID, webSocketClient>
    Map<String, WebSocketClient> webSocketClientMap = new ConcurrentHashMap<>();

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback) throws ClientConnectorException {
        if (msg instanceof StatusCarbonMessage) {
            StatusCarbonMessage statusCarbonMessage = (StatusCarbonMessage) msg;
            String status = statusCarbonMessage.getStatus();
            if (org.wso2.carbon.messaging.Constants.STATUS_OPEN.equals(status)) {
                handleHandshake(statusCarbonMessage);
            } else if (org.wso2.carbon.messaging.Constants.STATUS_CLOSE.equals(status)) {
                shutdownClient(statusCarbonMessage);
            }
        } else if (msg instanceof TextCarbonMessage) {
            TextCarbonMessage textCarbonMessage = (TextCarbonMessage) msg;
            sendText(textCarbonMessage);
        } else if (msg instanceof ControlCarbonMessage) {
            ControlCarbonMessage controlCarbonMessage = (ControlCarbonMessage) msg;
            sendPing(controlCarbonMessage);
        } else if (msg instanceof BinaryCarbonMessage) {
            BinaryCarbonMessage binaryCarbonMessage = (BinaryCarbonMessage) msg;
            sendBinary(binaryCarbonMessage);
        }
        return true;
    }

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback, Map<String, String> parameters)
            throws ClientConnectorException {
        send(msg, callback);
        return true;
    }

    @Override
    public String getProtocol() {
        return Constants.WEBSOCKET_PROTOCOL_NAME;
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor messageProcessor) {
        HTTPTransportContextHolder.getInstance().setMessageProcessor(messageProcessor);
    }

    private boolean handleHandshake(StatusCarbonMessage carbonMessage) throws ClientConnectorException {
        String url = (String) carbonMessage.getProperty(Constants.TO);
        String clientId = (String) carbonMessage.getProperty(Constants.WEBSOCKET_CLIENT_ID);
        WebSocketClient webSocketClient = new WebSocketClient(clientId, url);
        boolean handshakeDone;
        try {
            handshakeDone = webSocketClient.handhshake();
            if (handshakeDone) {
                webSocketClientMap.put(clientId, webSocketClient);
            }
        } catch (InterruptedException e) {
            throw new ClientConnectorException("Handshake interrupted", e);
        } catch (URISyntaxException e) {
            throw new ClientConnectorException("SSL Exception occurred during handshake", e);
        } catch (SSLException e) {
            throw new ClientConnectorException("URI Syntax exception occurred during handshake", e);
        }
        return handshakeDone;
    }

    private WebSocketClient getClient(CarbonMessage carbonMessage) throws ClientConnectorException {
        String id = (String) carbonMessage.getProperty(Constants.WEBSOCKET_CLIENT_ID);
        WebSocketClient webSocketClient = webSocketClientMap.get(id);
        if (webSocketClient == null) {
            throw new ClientConnectorException("Cannot find a WebSocket Client for ID: " + id);
        }
        return webSocketClient;
    }

    private void shutdownClient(StatusCarbonMessage statusCarbonMessage) throws ClientConnectorException {
        WebSocketClient websocketClient = getClient(statusCarbonMessage);
        try {
            websocketClient.shutDown(statusCarbonMessage.getStatusCode(), statusCarbonMessage.getReasonText());
        } catch (InterruptedException e) {
            throw new ClientConnectorException("Interruption occurred while shutting down the WebSocket Client.", e);
        } finally {
            removeClient(statusCarbonMessage);
        }
    }

    private void removeClient(CarbonMessage carbonMessage) {
        String id = (String) carbonMessage.getProperty(Constants.WEBSOCKET_CLIENT_ID);
        webSocketClientMap.remove(id);
    }

    private void sendText(TextCarbonMessage textCarbonMessage) throws ClientConnectorException {
        String text = textCarbonMessage.getText();
        WebSocketClient webSocketClient = getClient(textCarbonMessage);
        try {
            webSocketClient.sendText(text);
        } catch (IOException e) {
            throw new ClientConnectorException("Interruption occurred while sending text message to " +
                                                       "the WebSocket Client.", e);
        }
    }

    private void sendBinary(BinaryCarbonMessage binaryCarbonMessage) throws ClientConnectorException {
        ByteBuffer byteBuf = binaryCarbonMessage.readBytes();
        WebSocketClient webSocketClient = getClient(binaryCarbonMessage);
        try {
            webSocketClient.sendBinary(byteBuf);
        } catch (IOException e) {
            throw new ClientConnectorException("Interruption occurred while sending binary message to " +
                                                       "the WebSocket Client.", e);
        }
    }

    private void sendPing(ControlCarbonMessage controlCarbonMessage) throws ClientConnectorException {
        ByteBuffer buffer = controlCarbonMessage.readBytes();
        WebSocketClient webSocketClient = getClient(controlCarbonMessage);
        try {
            webSocketClient.sendPing(buffer);
        } catch (IOException e) {
            throw new ClientConnectorException("Interruption occurred while sending ping message to" +
                                                       " the WebSocket Client.", e);
        }
    }
}
