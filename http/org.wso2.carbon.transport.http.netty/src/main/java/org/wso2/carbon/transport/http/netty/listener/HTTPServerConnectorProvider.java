/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.transport.http.netty.listener;

/**
 * {@code HTTPServerConnectorProvider} is responsible for providing and managing HTTP Listeners
 */
//public class HTTPServerConnectorProvider extends ServerConnectorProvider {
public class HTTPServerConnectorProvider {

//    private ServerConnectorBootstrap serverConnectorBootstrap;
//
//    public HTTPServerConnectorProvider() {
//        super(Constants.PROTOCOL_NAME);
//    }
//
//    public List<ServerConnector> initializeConnectors(TransportsConfiguration trpConfig) {
//
////        List<ServerConnector> connectors = new ArrayList<>();
////
////        createServerConnectorController(trpConfig);
////
////        Set<ListenerConfiguration> listenerConfigurationSet = trpConfig.getListenerConfigurations();
////
////        listenerConfigurationSet.forEach(config -> {
////            HTTPServerConnector connector = new HTTPServerConnector(config.getId());
////            connector.setListenerConfiguration(config);
////            connector.setServerConnectorBootstrap(serverConnectorBootstrap);
////            if (config.isBindOnStartup()) {
////                serverConnectorBootstrap.bindInterface(connector);
////            }
////            connectors.add(connector);
////        });
////
////        return connectors;
//    }
//
//    @Override
//    public List<ServerConnector> initializeConnectors() {
//        return initializeConnectors(ConfigurationBuilder.getInstance().getConfiguration());
//    }
//
//    @Override
//    public ServerConnector createConnector(String serviceName, Map<String, String> properties) {
//        TransportsConfiguration trpConfig = ConfigurationBuilder.getInstance().getConfiguration();
//        ListenerConfiguration config = buildListenerConfig(serviceName, properties);
//
//        EventLoopGroup bossGroup = HTTPTransportContextHolder.getInstance().getBossGroup();
//        EventLoopGroup workerGroup = HTTPTransportContextHolder.getInstance().getWorkerGroup();
//
//        serverConnectorBootstrap = new ServerConnectorBootstrap(trpConfig);
//        serverConnectorBootstrap.initialize(bossGroup, workerGroup);
//
//        ServerConnector serverConnector = serverConnectorBootstrap.getServerConnector();
//
////        HTTPServerConnector connector = new HTTPServerConnector(config.getId());
////        connector.setListenerConfiguration(config);
////        connector.setServerConnectorBootstrap(serverConnectorBootstrap);
//
//
////        if (config.isBindOnStartup()) {
////            serverConnectorBootstrap.bindInterface(connector);
////        }
//        return serverConnector;
//    }
//
//    /**
//     * Helper method to initialize server connector controller for the provider.
//     *
//     * @param trpConfig to be used for initialization
//     */
//    private synchronized void createServerConnectorController(TransportsConfiguration trpConfig) {
//        if (serverConnectorBootstrap == null) {
//            serverConnectorBootstrap = new ServerConnectorBootstrap(trpConfig);
//            serverConnectorBootstrap.initialize();
//        }
//    }
//
//    /**
//     * Method to build listener configuration using provided properties map.
//     *
//     * @param id            HTTPConnectorListener id
//     * @param properties    Property map
//     * @return              listener config
//     */
//    private ListenerConfiguration buildListenerConfig(String id, Map<String, String> properties) {
//        String host = properties.get(Constants.HTTP_HOST) != null ?
//                properties.get(Constants.HTTP_HOST) : Constants.HTTP_DEFAULT_HOST;
//        int port = Integer.parseInt(properties.get(Constants.HTTP_PORT));
//        ListenerConfiguration config = new ListenerConfiguration(id, host, port);
//        String schema = properties.get(Constants.HTTP_SCHEME);
//        if (schema != null && schema.equals("https")) {
//            config.setScheme(schema);
//            config.setKeyStoreFile(properties.get(Constants.HTTP_KEY_STORE_FILE));
//            config.setKeyStorePass(properties.get(Constants.HTTP_KEY_STORE_PASS));
//            config.setCertPass(properties.get(Constants.HTTP_CERT_PASS));
//            //todo fill truststore stuff
//        }
//        return config;
//    }
}
