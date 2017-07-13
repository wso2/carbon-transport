package org.wso2.carbon.transport.email.utils;

/**
 * This class contains the constants related to File transport.
 */
public class EmailConstants {

    //transport
    public static final String PROTOCOL_EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String STORE_TYPE = "storeType";
    public static final String HOST_NAME = "hostName";
    public static final String FOLDER_NAME = "folderName";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final String SERVICE_NAME = "serviceName";
    public static final String MAX_RETRY_COUNT = "maxRetryCount";
    public static final String RETRY_INTERVEL = "reconnectIntervel";


    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String BCC = "bcc";
    public static final String CC = "cc";
    public static final String MESSAGE_UID = "messageID";
    public static final String REPLY_TO = "replyTo";
    public static final String SEND_DATE = "sendDate";
    public static final String RECEIVED_DATE = "receivedDate";
    public static final String FLAGS = "flags";
    public static final String MESSAGE_NUMBER = "messageNumber";
    public static final String IS_EXPUNGED = "isExpunged";
    public static final String SUBJECT = "subject";
    public static final String CONTENT_TYPE = "contentType";

    public static final String ACTION_AFTER_PROCESSED = "actionAfterProcessed";
    public static final String MOVE_TO_FOLDER = "moveToFolder";

    /**
     * Enum for action
     */
    public enum ActionAfterProcessed {
        MOVE, SEEN, DELET, FLAGGED, ANSWERED, KEEPSAME
    }

}
