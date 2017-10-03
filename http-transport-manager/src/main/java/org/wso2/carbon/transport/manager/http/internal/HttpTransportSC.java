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
package org.wso2.carbon.transport.manager.http.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.manager.http.HttpTransportManager;

import java.util.Map;

/**
 * Service component for the org.wso2.carbon.transport.manager.http bundle.
 * This will start the http transport when all the required components are satisfied.
 */
@Component(name = "org.wso2.carbon.transport.manager.http.internal.HttpTransportSC",
        immediate = true,
        property = {
                "componentName=wso2-server-connector-sc"
        })
public class HttpTransportSC implements RequiredCapabilityListener {
    private static final Logger log = LoggerFactory.getLogger(HttpTransportSC.class);
    private HttpTransportManager httpTransportManager = new HttpTransportManager();

    @Reference(
            name = "serverConnector",
            service = ServerConnector.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServerConnector"
    )
    protected void registerServerConnector(ServerConnector serverConnector) {
        httpTransportManager.addServerConnector(serverConnector);
    }

    protected void unregisterServerConnector(ServerConnector serverConnector) {
        httpTransportManager.removeServerConnector(serverConnector);
    }

    @Reference(
            name = "carbonRuntime",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCarbonRuntime"
    )
    protected void registerCarbonRuntime(CarbonRuntime carbonRuntime, Map properties) {
        // No use of the CarbonRuntime reference. We just need this for OSGi startup order resolving.
    }

    protected void unregisterCarbonRuntime(CarbonRuntime carbonRuntime, Map properties) {
    }

    @Reference(
            name = "configProvider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        // Need to load transport config
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
    }

    @Reference(
            name = "httpConnectorListener",
            service = HttpConnectorListener.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterHttpConnectorListener"
    )
    protected void registerHttpConnectorListener(HttpConnectorListener httpConnectorListener) {
        httpTransportManager.addHttpConnectorListener(httpConnectorListener);
    }

    protected void unregisterHttpConnectorListener(HttpConnectorListener httpConnectorListener) {
        httpTransportManager.addHttpConnectorListener(null);
    }

    @Reference(
            name = "webSocketConnectorListener",
            service = WebSocketConnectorListener.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterWebSocketConnectorListener"
    )
    protected void registerWebSocketConnectorListener(WebSocketConnectorListener webSocketConnectorListener) {
        httpTransportManager.addWebSocketConnectorListener(webSocketConnectorListener);
    }

    protected void unregisterWebSocketConnectorListener(WebSocketConnectorListener webSocketConnectorListener) {
        httpTransportManager.addWebSocketConnectorListener(null);
    }

    public void onAllRequiredCapabilitiesAvailable() {
        httpTransportManager.startConnectors();
    }
}
