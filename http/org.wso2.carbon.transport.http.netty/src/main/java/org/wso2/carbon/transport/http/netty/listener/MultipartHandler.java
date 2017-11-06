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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;

import java.io.IOException;

/**
 * {@code MultipartHandler} handles multipart requests.
 */
public class MultipartHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MultipartHandler.class);

    private final ServerConnectorFuture serverConnectorFuture;
    private final String interfaceId;
    private HttpPostRequestDecoder postRequestDecoder;
    private HttpRequest httpRequest;
    private String readData;
    private ByteBuf readBytes;

    public MultipartHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId) throws Exception {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = httpRequest = (HttpRequest) msg;
            postRequestDecoder = new HttpPostRequestDecoder(request);
            //postRequestDecoder.setDiscardThreshold(0);
            ctx.fireChannelRead(msg);
        }

        if (postRequestDecoder != null && postRequestDecoder.isMultipart()) {
            if (msg instanceof HttpContent) {
                HttpContent chunk = (HttpContent) msg;
                postRequestDecoder.offer(chunk);
                // Read data as it becomes available, chunk by chunk.
                readChunkByChunk();
                if (chunk instanceof LastHttpContent) {
                    readChunkByChunk();
                    sendResponse(ctx);
                    resetPostRequestDecoder();
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readChunkByChunk() {
        try {
            while (postRequestDecoder.hasNext()) {
                InterfaceHttpData data = postRequestDecoder.next();
                if (data != null) {
                    try {
                        processChunk(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e) {
            // No more data to decode, that's fine
        }
    }

    private void processChunk(InterfaceHttpData data) {
        log.debug("HTTP Data Name: {}, Type: {}", data.getName(), data.getHttpDataType());

        switch (data.getHttpDataType()) {
            case Attribute:
                Attribute attrib = (Attribute) data;
               /* if (!"json".equals(attrib.getName())) {
                    log.debug("Received unknown attribute: {}", attrib.getName());
                    handleInvalidRequest(ctx, fullHttpRequest, BAD_REQUEST,
                            copiedBuffer("Unknown Part Name: " + attrib.getName(), CharsetUtil.UTF_8));
                    return;
                }*/
                try {
                    // readData = attrib.getByteBuf().toString(CharsetUtil.UTF_8);
                    readBytes = attrib.getByteBuf();
                    log.debug("Content Size: {}, Content: {}", attrib.getByteBuf().readableBytes(), readData);
                } catch (IOException e) {
                    log.error("Unable to read attribute content", e);
                }

                break;
            case FileUpload:
                FileUpload fileUpload = (FileUpload) data;
                log.debug("File upload.");
                break;
            default:
                log.warn("Received unknown attribute type. Skipping.");
                break;
        }
    }

    private void resetPostRequestDecoder() {
        httpRequest = null;
        readData = null;
        postRequestDecoder.destroy();
        postRequestDecoder = null;
    }

    private void sendResponse(ChannelHandlerContext ctx) {
        // Serialize Response
        ByteBuf outputContent = Unpooled.buffer();
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(outputContent);
        try {
            // bbos.writeBytes(readData);
            byteBufOutputStream.write(readBytes.array());
        } catch (IOException e) {
            log.error("Unable to serialize response", e);
        }

        // Create HTTP Response
      /*  FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, outputContent);
        response.headers().set(CONTENT_TYPE, CONTENT_TYPE_JSON);
        setContentLength(response, outputContent.readableBytes());*/

        // Send HTTP Response
        // sendHttpResponse(ctx, fullHttpRequest, response);
    }

  /*  private void handleInvalidRequest(ChannelHandlerContext ctx, HttpRequest request, HttpResponseStatus
  responseStatus,
            ByteBuf errorMessage) {
        sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, responseStatus, errorMessage));
        return;
    }*/

   /* private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest request, FullHttpResponse response) {
        if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
            setContentLength(response, response.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(response);
        if (!isKeepAlive(request) || response.getStatus().code() != OK.code()) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void setContentLength(FullHttpResponse response, int length) {
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, length);
    }*/

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

}
