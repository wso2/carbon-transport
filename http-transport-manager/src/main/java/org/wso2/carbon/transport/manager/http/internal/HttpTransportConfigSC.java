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

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPConnectorUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * ServiceComponet which listens for ConfigProvider.
 * This is responsible of registering all the ServerConnectors.
 */
@Component(
        name = "org.wso2.carbon.transport.manager.http.internal.HttpTransportConfigSC",
        immediate = true,
        property = {
                "componentName=wso2-http-transport-config-sc"
        }
)
public class HttpTransportConfigSC implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(HttpTransportConfigSC.class);

    @Reference(
            name = "configProvider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
        StartupServiceUtils.updateServiceCache("wso2-http-transport-config-sc", ConfigProvider.class);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        final ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
        try {
            final TransportsConfiguration transportsConfiguration =
                    configProvider.getConfigurationObject(TransportsConfiguration.class);
            Set<ListenerConfiguration> listenerConfigurations =
                    transportsConfiguration.getListenerConfigurations();
            if (listenerConfigurations.isEmpty()) {
                listenerConfigurations = new HashSet<>();
                listenerConfigurations.add(ListenerConfiguration.getDefault());
            }

            ServerBootstrapConfiguration serverBootstrapConfiguration =
                    HTTPConnectorUtil.getServerBootstrapConfiguration(transportsConfiguration.getTransportProperties());
            HttpWsConnectorFactory connectorFactory = new HttpWsConnectorFactoryImpl();
            listenerConfigurations.forEach(listenerConfiguration -> {
                listenerConfiguration.setId(listenerConfiguration.getHost() == null ? "0.0.0.0" :
                                            listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort());

                final ServerConnector serverConnector =
                        connectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfiguration);
                FrameworkUtil.getBundle(HttpTransportSC.class).getBundleContext()
                             .registerService(ServerConnector.class, serverConnector, null);
            });

        } catch (ConfigurationException e) {
            log.error("Error while loading TransportsConfiguration", e);
            throw new RuntimeException("Error while loading TransportsConfiguration", e);
        }
    }
}
