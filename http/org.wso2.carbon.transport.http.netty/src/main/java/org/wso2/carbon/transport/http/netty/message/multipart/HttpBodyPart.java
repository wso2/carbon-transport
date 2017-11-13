package org.wso2.carbon.transport.http.netty.message.multipart;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one body part of multipart message.
 */
public class HttpBodyPart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] content;
    private final String contentType;
    private final String partName;
    private final String fileName;
    private final int size;
    private Map<String, Object> headers = new HashMap<>();

    public HttpBodyPart(String partName, byte[] content, String contentType, int size) {
        this(partName, null, content, contentType, size);
    }

    public HttpBodyPart(String partName, String fileName, byte[] content, String contentType, int size) {
        this.partName = partName;
        this.fileName = fileName;
        this.content = Arrays.copyOf(content, content.length);
        this.contentType = contentType;
        this.size = size;
    }

    public String getContentType() {
        return contentType;
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
