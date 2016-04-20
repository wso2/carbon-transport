/*
<<<<<<< HEAD
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
=======
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
>>>>>>> upstream/master
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
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.PoolConfiguration;

import java.util.Map;

/**
 * A class that responsible for create server side channels.
 */
public class CarbonNettyServerInitializer implements CarbonTransportInitializer {

    private static final Logger log = LoggerFactory.getLogger(CarbonNettyServerInitializer.class);
    private ConnectionManager connectionManager;

    private ListenerConfiguration listenerConfiguration;


    public CarbonNettyServerInitializer(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
    }

    @Override
    public void setup(Map<String, String> parameters) {

        PoolConfiguration.createPoolConfiguration(parameters);
        try {
            connectionManager = ConnectionManager.getInstance();
        } catch (Exception e) {
            log.error("Error initializing the transport ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initChannel(Object ch) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing source channel pipeline");
        }
        ChannelPipeline p = ((SocketChannel) ch).pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("compressor", new HttpContentCompressor());
        p.addLast("chunkWriter", new ChunkedWriteHandler());
        try {
            p.addLast("handler", new SourceHandler(connectionManager));

        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }

    @Override
    public boolean isServerInitializer() {
        return true;
    }

}
