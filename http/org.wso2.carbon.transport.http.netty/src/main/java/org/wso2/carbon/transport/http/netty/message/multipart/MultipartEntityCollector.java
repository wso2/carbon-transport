package org.wso2.carbon.transport.http.netty.message.multipart;

import java.util.Collection;

/**
 * Contains multipart http body.
 */
public class MultipartEntityCollector {
    private final Collection<MultipartMessageDataSource> bodyParts;

    public MultipartEntityCollector(Collection<MultipartMessageDataSource> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public Collection<MultipartMessageDataSource> getBodyParts() {
        return bodyParts;
    }
}
