/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.transport.manager.http;

import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code HttpTransportManager} is responsible for managing all the server connectors.
 */
public class HttpTransportManager {

    private Map<String, ServerConnector> serverConnectors = new HashMap<>();
    private HttpConnectorListener httpConnectorListener;
    private WebSocketConnectorListener webSocketConnectorListener;

    /**
     * Start all the server Connectors.
     *
     * */
    public void startConnectors() {
        if (httpConnectorListener == null) {
            throw new RuntimeException("HttpConnectorListener is null.");
        }
        if (webSocketConnectorListener == null) {
            throw new RuntimeException("WebSocketConnectorListener is null.");
        }

        for (ServerConnector serverConnector : serverConnectors.values()) {
            final ServerConnectorFuture serverConnectorFuture = serverConnector.start();
            serverConnectorFuture.setHttpConnectorListener(httpConnectorListener);
            serverConnectorFuture.setWSConnectorListener(webSocketConnectorListener);
        }
    }

    public void addServerConnector(ServerConnector serverConnector) {
        serverConnectors.put(serverConnector.getConnectorID(), serverConnector);
    }

    public void removeServerConnector(ServerConnector serverConnector) {
        serverConnectors.remove(serverConnector.getConnectorID());
    }

    public void addHttpConnectorListener(HttpConnectorListener httpConnectorListener) {
        this.httpConnectorListener = httpConnectorListener;
    }

    public void addWebSocketConnectorListener(WebSocketConnectorListener webSocketConnectorListener) {
        this.webSocketConnectorListener = webSocketConnectorListener;
    }
}
