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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPConnectorUtil;

/**
 * BundleActivator for the org.wso2.carbon.transport.manager.http bundle.
 * This is responsible of registering all the ServerConnectors.
 */
public class HttpTransportBundleActivator implements BundleActivator {

    private static final String TRANSPORTS_NETTY_CONF = "transports.netty.conf";

    public void start(BundleContext bundleContext) throws Exception {
        HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
        String transportYaml = System.getProperty(TRANSPORTS_NETTY_CONF);
        if (transportYaml == null || transportYaml.isEmpty()) {
            ServerBootstrapConfiguration bootstrapConfiguration = ServerBootstrapConfiguration.getInstance();
            ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
            ServerConnector serverConnector =
                    connectorFactory.createServerConnector(bootstrapConfiguration, listenerConfiguration);
            bundleContext.registerService(ServerConnector.class, serverConnector, null);
        } else {
            TransportsConfiguration transportsConfiguration =
                    ConfigurationBuilder.getInstance().getConfiguration(transportYaml);
            ServerBootstrapConfiguration serverBootstrapConfiguration =
                    HTTPConnectorUtil.getServerBootstrapConfiguration(transportsConfiguration.getTransportProperties());
            for (ListenerConfiguration listenerConfiguration : transportsConfiguration.getListenerConfigurations()) {
                listenerConfiguration.setId(listenerConfiguration.getHost() == null ? "0.0.0.0" :
                                            listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort());
                ServerConnector serverConnector =
                        connectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfiguration);
                bundleContext.registerService(ServerConnector.class, serverConnector, null);
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }
}
