package org.wso2.carbon.transport.http.netty.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MultipartTestCase {

    private final HttpDataFactory dataFactory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private final String jsonContent = "{key:value, key2:value2}";

    private EmbeddedChannel channel;
    //   private MyServerHandler handler;

    @BeforeClass
    public void setup() throws Exception {
        // handler = new MyServerHandler(dataFactory);
        channel = new EmbeddedChannel(new HttpObjectAggregator(1048576));
        channel.pipeline().addLast(new HttpResponseDecoder());
        channel.pipeline().addLast(new HttpRequestEncoder());
        channel.pipeline().addLast(new SourceHandler(null, null));
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

        FullHttpResponse response = (FullHttpResponse) channel.readOutbound();
        assertNotNull(response, "HTTP Response");
        assertEquals(response.getStatus(), HttpResponseStatus.OK,
                "Unexpected response code. Reason: " + response.content().toString(CharsetUtil.UTF_8) + ".");
        assertEquals(response.content().toString(CharsetUtil.UTF_8), jsonContent);
    }

    private void sendMultipartRequest(HttpRequest request, HttpPostRequestEncoder encoder) throws Exception {
        channel.writeInbound(request);

        if (!channel.isOpen()) {
            // Channel was closed early due to a bad request being written, so don't bother to write the chunks
            encoder.cleanFiles();
            return;
        }

        HttpContent content;
        while (!encoder.isEndOfInput()) {
            content = encoder.readChunk(new ByteBufAllocator() {
                @Override
                public ByteBuf buffer() {
                    return null;
                }

                @Override
                public ByteBuf buffer(int initialCapacity) {
                    return null;
                }

                @Override
                public ByteBuf buffer(int initialCapacity, int maxCapacity) {
                    return null;
                }

                @Override
                public ByteBuf ioBuffer() {
                    return null;
                }

                @Override
                public ByteBuf ioBuffer(int initialCapacity) {
                    return null;
                }

                @Override
                public ByteBuf ioBuffer(int initialCapacity, int maxCapacity) {
                    return null;
                }

                @Override
                public ByteBuf heapBuffer() {
                    return null;
                }

                @Override
                public ByteBuf heapBuffer(int initialCapacity) {
                    return null;
                }

                @Override
                public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
                    return null;
                }

                @Override
                public ByteBuf directBuffer() {
                    return null;
                }

                @Override
                public ByteBuf directBuffer(int initialCapacity) {
                    return null;
                }

                @Override
                public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
                    return null;
                }

                @Override
                public CompositeByteBuf compositeBuffer() {
                    return null;
                }

                @Override
                public CompositeByteBuf compositeBuffer(int maxNumComponents) {
                    return null;
                }

                @Override
                public CompositeByteBuf compositeHeapBuffer() {
                    return null;
                }

                @Override
                public CompositeByteBuf compositeHeapBuffer(int maxNumComponents) {
                    return null;
                }

                @Override
                public CompositeByteBuf compositeDirectBuffer() {
                    return null;
                }

                @Override
                public CompositeByteBuf compositeDirectBuffer(int maxNumComponents) {
                    return null;
                }

                @Override
                public boolean isDirectBufferPooled() {
                    return false;
                }

                @Override
                public int calculateNewCapacity(int minNewCapacity, int maxCapacity) {
                    return 0;
                }
            });
            channel.writeInbound(content);
        }
        channel.flush();

        encoder.cleanFiles();
    }

    private InterfaceHttpData createJSONAttribute(HttpRequest request, HttpDataFactory factory) throws IOException {
        ByteBuf content = Unpooled.buffer();
        ByteBufOutputStream bbos = new ByteBufOutputStream(content);
        bbos.writeBytes(jsonContent);
        return factory.createAttribute(request, "json", content.toString(CharsetUtil.UTF_8));
    }

    private InterfaceHttpData createFileUpload(HttpRequest request, HttpDataFactory factory) throws IOException {
        File file = File.createTempFile("upload", ".txt");
        file.deleteOnExit();

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("Example file to be posted");
        bw.close();

        FileUpload fileUpload = factory
                .createFileUpload(request, "file", file.getName(), "plain/text", "7bit", null, file.length());
        fileUpload.setContent(file);

        return fileUpload;
    }

}
