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
package org.wso2.carbon.transport.http.netty.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;
import org.wso2.carbon.messaging.exceptions.EndPointTimeOut;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A class responsible for handling responses coming from BE.
 */
public class TargetHandler extends ReadTimeoutHandler {
    private static Logger log = LoggerFactory.getLogger(TargetHandler.class);

    protected CarbonCallback callback;
    protected CarbonMessage cMsg;
    protected ConnectionManager connectionManager;
    protected TargetChannel targetChannel;
    protected CarbonMessage incomingMsg;

    public TargetHandler(int timeoutSeconds) {
        super(timeoutSeconds);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        NettyTransportContextHolder.getInstance().getHandlerExecutor()
                .executeAtTargetConnectionInitiation(Integer.toString(ctx.hashCode()));


        super.channelActive(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {

            cMsg = setUpCarbonMessage(ctx, msg);


            if (cMsg.getHeaders().get(Constants.HTTP_CONTENT_LENGTH) != null
                    || cMsg.getHeaders().get(Constants.HTTP_TRANSFER_ENCODING) != null) {
              //  ringBuffer.publishEvent(new CarbonEventPublisher(cMsg));
               // callback.done(cMsg);

                NettyTransportContextHolder.getInstance().getMessageProcessor().receive(cMsg, callback);
            }
        } else {
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    HttpContent httpContent = (LastHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                    NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtTargetResponseSending(cMsg);
                    targetChannel.setRequestWritten(false);
                    connectionManager.returnChannel(targetChannel);

                    if (cMsg.getHeaders().get(Constants.HTTP_CONTENT_LENGTH) == null
                        && cMsg.getHeaders().get(Constants.HTTP_TRANSFER_ENCODING) == null) {
                        cMsg.getHeaders().put(Constants.HTTP_CONTENT_LENGTH,
                                String.valueOf(((NettyCarbonMessage) cMsg).getMessageBodyLength()));
                       // ringBuffer.publishEvent(new CarbonEventPublisher(cMsg));
                        NettyTransportContextHolder.getInstance().getMessageProcessor().receive(cMsg, callback);
                    }
                } else {
                    HttpContent httpContent = (DefaultHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        NettyTransportContextHolder.getInstance().getHandlerExecutor()
                .executeAtTargetConnectionTermination(Integer.toString(ctx.hashCode()));
        log.debug("Target channel closed.");
    }

    public void setCallback(CarbonCallback callback) {
        this.callback = callback;
    }


    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setIncomingMsg(CarbonMessage incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {

        ctx.channel().close();

        if (targetChannel.isRequestWritten()) {
            String payload = "<errorMessage>" + "ReadTimeoutException occurred for endpoint " + targetChannel.
                       getHttpRoute().toString() + "</errorMessage>";
            FaultHandler faultHandler = incomingMsg.getFaultHandlerStack().pop();

            if (faultHandler != null) {
                faultHandler.handleFault("504", new EndPointTimeOut(payload), incomingMsg, callback);
                incomingMsg.getFaultHandlerStack().push(faultHandler);
            } else {
                NettyTransportContextHolder.getInstance().getMessageProcessor().
                           receive(createErrorMessage(payload), callback);
              //  ringBuffer.publishEvent(new CarbonEventPublisher(createErrorMessage(payload)));
            }
        }
    }


    protected CarbonMessage setUpCarbonMessage(ChannelHandlerContext ctx, Object msg) {
        cMsg = new NettyCarbonMessage();
        NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtTargetResponseReceiving(cMsg);
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        cMsg.setProperty(Constants.CALL_BACK, callback);
        HttpResponse httpResponse = (HttpResponse) msg;

        cMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
        cMsg.setHeaders(Util.getHeaders(httpResponse));
        return cMsg;

    }

    protected CarbonMessage createErrorMessage(String payload) {
        DefaultCarbonMessage response = new DefaultCarbonMessage();

        response.setStringMessageBody(payload);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
        transportHeaders.put(Constants.HTTP_CONTENT_ENCODING, Constants.GZIP);
        transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_XML);
        transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, 504);
        response.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        response.setProperty(Constants.CALL_BACK, callback);
        return response;

    }
}
