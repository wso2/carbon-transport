package org.wso2.carbon.transport.email.test.Utils;

/**
 * Created by chathurika on 7/11/17.
 */
public class EmailTestConstant {

    /**
     * Connector property
     */
    public static final String PROTOCOL_EMAIL = "email";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final String SERVICE_NAME = "serviceName";
    public static final String MAX_RETRY_COUNT = "maxRetryCount";
    public static final String RETRY_INTERVAL = "reconnectIntervel";
    public static final String CONTENT_TYPE = "contentType";

    /**
     * Email Receiver properties
     */
    public static final String MAIL_RECEIVER_USERNAME = "receiver.username";
    public static final String MAIL_RECEIVER_PASSWORD = "receiver.password";
    public static final String MAIL_RECEIVER_STORE_TYPE = "receiver.storeType";
    public static final String MAIL_RECEIVER_HOST_NAME = "receiver.hostName";
    public static final String MAIL_RECEIVER_FOLDER_NAME = "receiver.folderName";

    /**
     * Email Receiver properties
     */
    public static final String MAIL_SENDER_USERNAME = "sender.username";
    public static final String MAIL_SENDER_PASSWORD = "sender.password";
    public static final String MAIL_SENDER_HOST_NAME = "sender.hostName";

    /**
     * Mail Headers which has to set in the message to be send
     */
    public static final String MAIL_HEADER_TO = "To";
    public static final String MAIL_HEADER_FROM = "From";
    public static final String MAIL_HEADER_CC = "Cc";
    public static final String MAIL_HEADER_BCC = "Bcc";
    public static final String MAIL_HEADER_REPLY_TO = "Reply-To";
    public static final String MAIL_HEADER_IN_REPLY_TO = "In-Reply-To";
    public static final String MAIL_HEADER_SUBJECT = "Subject";
    public static final String MAIL_HEADER_MESSAGE_ID = "Message-ID";
    public static final String MAIL_HEADER_REFERENCES = "References";
    public static final String MAIL_HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Properties which are included in carbon message other than headers
     */
    public static final String MAIL_PROPERTY_FLAGS = "flags";
    public static final String MAIL_PROPERTY_MESSAGE_NUMBER = "messageNumber";
    public static final String MAIL_PROPERTY_UID = "messageUID";
    public static final String MAIL_PROPERTY_REPLY_TO = "replyTo";
    public static final String MAIL_PROPERTY_RECEIVED_DATE = "receivedDate";

    public static final String ACTION_AFTER_PROCESSED = "actionAfterProcessed";
    public static final String MOVE_TO_FOLDER = "moveToFolder";

    public static final String AUTO_ACKNOWLEDGE = "autoAcknowledge";

}
