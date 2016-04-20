/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * A Class responsible for handle  incoming message through netty inbound pipeline.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(SourceHandler.class);
    protected ChannelHandlerContext ctx;
    protected NettyCarbonMessage cMsg;
    protected ConnectionManager connectionManager;
    private Map<String, TargetChannel> channelFutureMap = new HashMap<>();

    protected Map<String, GenericObjectPool> targetChannelPool;

    public SourceHandler(ConnectionManager connectionManager) throws Exception {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer

        NettyTransportContextHolder.getInstance().getHandlerExecutor()
                .executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        this.ctx = ctx;
        this.targetChannelPool = connectionManager.getTargetChannelPool();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {

            cMsg = (NettyCarbonMessage) setUPCarbonMessage(msg);


            ResponseCallback responseCallback = new ResponseCallback(this.ctx);
            cMsg.setProperty(Constants.CALL_BACK, responseCallback);

            //todo executeRequestContinuationValidator

            NettyTransportContextHolder.getInstance().getMessageProcessor().receive(cMsg, responseCallback);
            

        } else {
            if (cMsg != null) {
                if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;
                    cMsg.addHttpContent(httpContent);
                    if (msg instanceof LastHttpContent) {
                        NettyTransportContextHolder.getInstance().getHandlerExecutor()
                                .executeAtSourceRequestSending(cMsg);
                        cMsg.setEndOfMsgAdded(true);
                    }
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // Stop the connector timer

        NettyTransportContextHolder.getInstance().getHandlerExecutor()
                .executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));

        connectionManager.notifyChannelInactive();
    }

    public void addTargetChannel(HttpRoute route, TargetChannel targetChannel) {
        channelFutureMap.put(route.toString(), targetChannel);
    }

    public void removeChannelFuture(HttpRoute route) {
        log.debug("Removing channel future from map");
        channelFutureMap.remove(route.toString());
    }

    public TargetChannel getChannel(HttpRoute route) {
        return channelFutureMap.get(route.toString());
    }

    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return targetChannelPool;
    }

    public ChannelHandlerContext getInboundChannelContext() {
        return ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("Exception caught in Netty Source handler", cause);
    }


    protected CarbonMessage setUPCarbonMessage(Object msg) {
        cMsg = new NettyCarbonMessage();
        NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        HttpRequest httpRequest = (HttpRequest) msg;

        cMsg.setProperty(Constants.TO, httpRequest.getUri());
        cMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
        cMsg.setProperty(Constants.SRC_HNDLR, this);
        cMsg.setProperty(Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
        cMsg.setProperty(Constants.HTTP_METHOD, httpRequest.getMethod().name());
        cMsg.setProperty(Constants.LISTENER_PORT, ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        cMsg.setProperty(Constants.PROTOCOL, httpRequest.getProtocolVersion().protocolName());
        cMsg.setHeaders(Util.getHeaders(httpRequest));
        return cMsg;
    }

}



