/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.email.utils;

/**
 * This class contains the constants related to File transport.
 */
public class EmailConstants {

    /**
     * Connector property
     */
    public static final String PROTOCOL_EMAIL = "email";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final String SERVICE_NAME = "serviceName";
    public static final String MAX_RETRY_COUNT = "maxRetryCount";
    public static final String RETRY_INTERVAL = "reconnectInterval";
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
     * Action that has to carry out after processing the email.
     */
    public static final String ACTION_AFTER_PROCESSED = "actionAfterProcessed";

    /**
     * Folder to move the processed email if action after processed if MOVE.
     */
    public static final String MOVE_TO_FOLDER = "moveToFolder";

    /**
     * property whether to Auto Acknowledge or not. If false then wait for acknowledge.
     */
    public static final String AUTO_ACKNOWLEDGE = "autoAcknowledge";

    /**
     * String search term to give the conditions to filter the messages
     */
     public static final String SEARCH_TERM = "searchTerm";

    /**
     * Properties which are included in carbon message other than headers
     * in email receiver side
     */
    public static final String MAIL_PROPERTY_FLAGS = "flags";
    public static final String MAIL_PROPERTY_MESSAGE_NUMBER = "messageNumber";
    public static final String MAIL_PROPERTY_UID = "messageUID";


    /**
     * Email sender properties
     */
    public static final String MAIL_SENDER_USERNAME = "sender.username";
    public static final String MAIL_SENDER_PASSWORD = "sender.password";
    public static final String MAIL_SENDER_HOST_NAME = "sender.hostName";

    /**
     * Mail Headers which has to be set in the message to be send at the email
     * client connector
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
     * Enum for action
     */
    public enum ActionAfterProcessed {
        MOVE, SEEN, DELETE, FLAGGED, ANSWERED, NONE
    }

}
