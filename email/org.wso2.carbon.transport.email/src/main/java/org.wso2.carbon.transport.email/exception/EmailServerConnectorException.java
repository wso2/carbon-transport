package org.wso2.carbon.transport.email.exception;

import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

/**
 * Created by chathurika on 6/29/17.
 */
public class EmailServerConnectorException extends ServerConnectorException {
    public EmailServerConnectorException(String message) {
        super(message);
    }

    public EmailServerConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
