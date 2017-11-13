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

package org.wso2.carbon.transport.http.netty.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.message.multipart.HttpBodyPart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test case for multipart handling.
 */
public class MultipartTestCase {

    private final HttpDataFactory dataFactory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private final String jsonContent = "{key:value, key2:value2}";
    private EmbeddedChannel channel;
    private HttpWsServerConnectorFuture httpWsServerConnectorFuture = new HttpWsServerConnectorFuture();
    private MultipartConnectionListener multipartConnectionListener;

    @BeforeClass
    public void setup() throws Exception {
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(new HttpResponseDecoder());
        channel.pipeline().addLast(new HttpRequestEncoder());
        channel.pipeline().addLast(new SourceHandler(httpWsServerConnectorFuture, null));
        multipartConnectionListener = new MultipartConnectionListener();
        httpWsServerConnectorFuture.setHttpConnectorListener(multipartConnectionListener);
    }

    @Test
    public void testMultipart() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(dataFactory, request, true);
        request.headers().add(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.MULTIPART_FORM_DATA);
        encoder.addBodyHttpData(createJSONAttribute(request, dataFactory));
        encoder.addBodyHttpData(createFileUpload(request, dataFactory));
        request = encoder.finalizeRequest();
        sendMultipartRequest(request, encoder);

        List<HttpBodyPart> httpBodyParts = multipartConnectionListener.getMultiparts();
        assertNotNull(httpBodyParts, "Received http body parts are null");
        String jsonPart = new String(httpBodyParts.get(0).getContent());
        assertEquals(jsonPart, jsonContent, "Received body Part value doesn't match with the sent value.");
        assertEquals(httpBodyParts.get(1).getContentType(), "plain/text", "Incorrect content type received.");
        assertEquals(httpBodyParts.get(1).getPartName(), "file", "Incorrect part name.");
    }

    /**
     * Write multipart request to inbound channel.
     *
     * @param request HttpRequest
     * @param encoder HttpPostRequestEncoder
     * @throws Exception
     */
    private void sendMultipartRequest(HttpRequest request, HttpPostRequestEncoder encoder) throws Exception {
        channel.writeInbound(request);
        if (!channel.isOpen()) {
            encoder.cleanFiles();
            return;
        }
        HttpContent content;
        while (!encoder.isEndOfInput()) {
            content = encoder.readChunk(ByteBufAllocator.DEFAULT);
            channel.writeInbound(content);
        }
        channel.flush();
        encoder.cleanFiles();
    }

    /**
     * Create a json body part.
     *
     * @param request HttpRequest
     * @param factory HttpDataFactory
     * @return InterfaceHttpData
     * @throws IOException
     */
    private InterfaceHttpData createJSONAttribute(HttpRequest request, HttpDataFactory factory) throws IOException {
        ByteBuf content = Unpooled.buffer();
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(content);
        byteBufOutputStream.writeBytes(jsonContent);
        return factory.createAttribute(request, "json", content.toString(CharsetUtil.UTF_8));
    }

    /**
     * Include a file as a body part.
     *
     * @param request HttpRequest
     * @param factory HttpDataFactory
     * @return InterfaceHttpData
     * @throws IOException
     */
    private InterfaceHttpData createFileUpload(HttpRequest request, HttpDataFactory factory) throws IOException {
        File file = File.createTempFile("upload", ".txt");
        file.deleteOnExit();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write("Example file to be posted");
        bufferedWriter.close();
        FileUpload fileUpload = factory
                .createFileUpload(request, "file", file.getName(), "plain/text", "7bit", null, file.length());
        fileUpload.setContent(file);
        return fileUpload;
    }

}
