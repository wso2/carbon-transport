package org.wso2.carbon.transport.http.netty.message.multipart;

import org.wso2.carbon.messaging.MessageDataSource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one body part of multipart message.
 */
public class MultipartMessageDataSource implements MessageDataSource, Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] content;
    private final String contentType;
    private final String partName;
    private final String fileName;
    private final int size;
    private Map<String, Object> headers = new HashMap<>();

    public MultipartMessageDataSource(String partName, byte[] content, String contentType, int size) {
        this(partName, null, content, contentType, size);
    }

    public MultipartMessageDataSource(String partName, String fileName, byte[] content, String contentType, int size) {
        this.partName = partName;
        this.fileName = fileName;
        this.content = Arrays.copyOf(content, content.length);;
        this.contentType = contentType;
        this.size = size;
    }

    @Override
    public String getValueAsString(String s) {
        return null;
    }

    @Override
    public String getValueAsString(String s, Map<String, String> map) {
        return null;
    }

    @Override
    public Object getValue(String s) {
        return null;
    }

    @Override
    public Object getDataObject() {
        return null;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public void serializeData() {

    }

    @Override
    public String getMessageAsString() {
        return null;
    }

    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }

    public String getPartName() {
        return partName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getSize() {
        return size;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
