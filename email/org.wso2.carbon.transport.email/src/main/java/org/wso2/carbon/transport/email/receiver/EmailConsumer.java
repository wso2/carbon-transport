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

package org.wso2.carbon.transport.email.receiver;

import com.sun.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.transport.email.callback.EmailServerConnectorCallback;
import org.wso2.carbon.transport.email.exception.EmailServerConnectorException;
import org.wso2.carbon.transport.email.utils.EmailConstants;
import org.wso2.carbon.transport.email.utils.EmailUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;

/**
 * Class implemented to search and process emails.
 */
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
    private SearchTerm emailSearchTerm;
    private Map<String, String> emailProperties;
    private CarbonMessageProcessor emailMessageProcessor;
    private String serviceId;
    private Properties serverProperties = new Properties();
    private Session session;
    private Folder folder;
    private long startUIDNumber = 1;
    private Store store;
    private String host;
    private String username;
    private String password;
    private String storeType;
    private int maxRetryCount = 0;
    private Long retryInterval = 1000L;
    private String folderName;
    private String contentType = "text/plain";
    private EmailConstants.ActionAfterProcessed action;
    private Folder moveToFolder = null;
    private boolean isFirstTimeConnect = true;
    private boolean autoAcknowledge = true;
    private boolean isImapFolder;

    /**
     * Check the given parameters in the map and initialise the parameters needed for email server connector.
     *
     * @param id The service Id which this consumer belongs to
     * @param properties Map which contains parameters needed to initialize the email server connector
     * @param emailSearchTerm The search term which is going to use for fetch emails
     * @param emailMessageProcessor  The message processor who is going to process messages consumed from this
     * @throws EmailServerConnectorException
     */
    protected EmailConsumer(String id, Map<String, String> properties, SearchTerm emailSearchTerm,
            CarbonMessageProcessor emailMessageProcessor) throws EmailServerConnectorException {
        this.serviceId = id;
        this.emailProperties = properties;
        this.emailSearchTerm = emailSearchTerm;
        this.emailMessageProcessor = emailMessageProcessor;

        if (emailProperties.get(EmailConstants.MAIL_RECEIVER_USERNAME) != null) {
            this.username = emailProperties.get(EmailConstants.MAIL_RECEIVER_USERNAME);
        } else {
            throw new EmailServerConnectorException("Username (email address) of the email account is"
                    + " a mandatory parameter." + "It is not given in the email property map" +
                    "in the email server connector with service id '" + serviceId + "'");
        }

        if (emailProperties.get(EmailConstants.MAIL_RECEIVER_PASSWORD) != null) {
            this.password = emailProperties.get(EmailConstants.MAIL_RECEIVER_PASSWORD);
        } else {
            throw new EmailServerConnectorException("Password of the email account is"
                    + " a mandatory parameter." + "It is not given in the email property map"
                    + "in the email server connector with service id '" + serviceId + "'");
        }

        if (emailProperties.get(EmailConstants.MAIL_RECEIVER_HOST_NAME) != null) {
            this.host = emailProperties.get(EmailConstants.MAIL_RECEIVER_HOST_NAME);
        } else {
            throw new EmailServerConnectorException("HostName of the email account is"
                    + " a mandatory parameter." + "It is not given in the email property map"
                    + "in the email server connector with service id '" + serviceId + "'");
        }

        if (emailProperties.get(EmailConstants.MAIL_RECEIVER_STORE_TYPE) != null) {
            this.storeType = emailProperties.get(EmailConstants.MAIL_RECEIVER_STORE_TYPE);
        } else {
            throw new EmailServerConnectorException("Store type of the email account is"
                    + " a mandatory parameter." + "It is not given in the email property map" +
                    "in the email server connector with service id '" + serviceId + "'");
        }

        if (emailProperties.get(EmailConstants.MAX_RETRY_COUNT) != null) {
            try {
                this.maxRetryCount = Integer.parseInt(emailProperties.get(EmailConstants.MAX_RETRY_COUNT));
            } catch (NumberFormatException e) {
                log.error("Could not parse parameter '" + emailProperties.get(EmailConstants.MAX_RETRY_COUNT)
                        + "' to numeric type 'Integer'." + " Get default value '"
                        + maxRetryCount + "' for the email server connector with service id '" + serviceId + "'");
            }
        }

        if (emailProperties.get(EmailConstants.RETRY_INTERVAL) != null) {
            try {
                this.retryInterval = Long.parseLong(emailProperties.get(EmailConstants.RETRY_INTERVAL));
            } catch (NumberFormatException e) {
                log.error("Could not parse parameter '" + emailProperties.get(EmailConstants.RETRY_INTERVAL)
                        + " to numeric type 'Long'." + " Get default '" + retryInterval
                        + "' for the email server connector with service id '" + serviceId + "'");
            }
        }

        if (emailProperties.get(EmailConstants.CONTENT_TYPE) != null) {
        if (emailProperties.get(EmailConstants.CONTENT_TYPE).equalsIgnoreCase("text/html")) {
                this.contentType = "text/html";
        } else if (emailProperties.get(EmailConstants.CONTENT_TYPE).equalsIgnoreCase("text/plain")) {
               contentType = "text/plain";
        }
        } else {
               log.warn("Email content type is not given in the email property map."
                       + " Get default content type 'text/plain'" + "for the email server connector with service id '"
                       + serviceId + "'");
        }

        if (emailProperties.get(EmailConstants.MAIL_RECEIVER_FOLDER_NAME) != null) {
            this.folderName = emailProperties.get(EmailConstants.MAIL_RECEIVER_FOLDER_NAME);

        } else {
            this.folderName = "INBOX";
            log.warn("Folder to fetch mails is not given in the email property map." +
                    "Get default folder '" + folderName  + "' for the email server connector with service id '"
                    + serviceId + "'");
        }

        if (emailProperties.get(EmailConstants.AUTO_ACKNOWLEDGE) != null) {
            this.autoAcknowledge =  Boolean.parseBoolean(emailProperties.get(EmailConstants.AUTO_ACKNOWLEDGE));
        } else {
            log.warn("Auto Acknowledgement property is not given in the email property list." +
                    "Get default value 'false' " + "for the email server connector with service id '"
                    + serviceId + "'");
        }

        // other parameters relate to the email server start with 'mail.'. Check property map contain such parameters
        // and insert them to the serverProperty map.
        for (Map.Entry<String, String> entry : emailProperties.entrySet()) {
            if (entry.getKey().startsWith("mail.")) {
                serverProperties.put(entry.getKey(), entry.getValue());
            }
        }

        session = Session.getDefaultInstance(serverProperties);

        try {
                store = session.getStore(storeType);
        } catch (NoSuchProviderException e) {
                throw new EmailServerConnectorException("Couldn't initialize the store '" + storeType +
                        "' in the email server connector with service id '" + serviceId + "'" , e);
        }

    }


    protected void emailConsumer() throws EmailServerConnectorException {

        openFolder(folder);
        List<Message> messageList = fetchEmails();

        if (messageList != null) {

            for (int i = 0; i < messageList.size(); i++) {
                try {
                    String content = getEmailContent(messageList.get(i));

                    CarbonMessage emailCarbonMessage;
                    //create carbon message
                    emailCarbonMessage = EmailUtils.createEmailCarbonMessage(messageList.get(i),
                                         folder, content, serviceId);

                    if (autoAcknowledge) {
                        emailMessageProcessor.receive(emailCarbonMessage, null);
                    } else {
                        EmailServerConnectorCallback callback = new EmailServerConnectorCallback();
                        emailMessageProcessor.receive(emailCarbonMessage, callback);
                    }

                    ActionForProcessedMail.carryOutAction(messageList.get(i), folder, action, moveToFolder);

                } catch (MessageRemovedException e) {
                    log.warn("Skip the message #: " + messageList.get(i).getMessageNumber() +
                            " by further processing." , e);
                } catch (Exception e) {
                    log.warn("Couldn't process the Message due to: " , e);
                }
            }

        }

        closeFolder(folder);
        if (moveToFolder != null) {
            closeFolder(moveToFolder);
        }
    }

    /**
     * Connect to the email server(store). If couldn't connect to the store retry maxRetry counts. If not, throw email
     * server connector exception.
     * @throws EmailServerConnectorException
     */

    protected void connectToEmailStore() throws EmailServerConnectorException {
        int retryCount = 0;
        while (!store.isConnected()) {
            try {
                retryCount++;

                if (log.isDebugEnabled()) {
                    log.debug("Attempting to connect to '" + storeType + "' server for : "
                            + emailProperties.get(EmailConstants.MAIL_RECEIVER_USERNAME)
                            + " using " + session.getProperties());
                }

                store.connect(host, username, password);

            } catch (Exception e) {
                log.error("Error connecting to mail server for address '" + username
                        + "' in the email server connector with id" + serviceId + "'", e);
                if (maxRetryCount <= retryCount) {
                    throw new EmailServerConnectorException("Error connecting to mail server for the address '"
                            + username + "' in the email server connector with id" + serviceId + "'", e);
                }
            }

            if (store.isConnected()) {
                if (log.isDebugEnabled()) {
                    log.debug("Connected to the server: " + store);
                }
                if (emailProperties.get(EmailConstants.MAIL_RECEIVER_FOLDER_NAME) != null) {
                    folderName = emailProperties.get(EmailConstants.MAIL_RECEIVER_FOLDER_NAME);

                } else {
                    folderName = "INBOX";
                    log.warn("Folder to fetch mails is not specified." +
                            "Get default folder '" + folderName + "' for the email server connector with id '"
                            + serviceId);
                }

                // To keep the signal instance of the folder
                if (isFirstTimeConnect) {
                try {
                    folder = store.getFolder(folderName);
                    isFirstTimeConnect = false;
                    if (folder instanceof IMAPFolder) {
                        isImapFolder = true;
                    } else {
                        isImapFolder = false;
                    }
                } catch (MessagingException e) {
                    log.warn(e.toString());
                }
                }
            }

            if (!store.isConnected()) {
                try {
                    log.warn("Connection to mail server for account : " + username + " using service '" + serviceId +
                            "' is failed. Retrying in '" + retryInterval / 1000 + "' seconds");
                    Thread.sleep(retryCount);
                } catch (InterruptedException ignore) {

                }
            }
        }
    }

    /**
     * Set the action according to the action after processed parameter in the given property map.
     *
     * @throws EmailServerConnectorException
     */
    protected void setAction() throws EmailServerConnectorException {
        action = EmailUtils.getActionAfterProcessed(
                emailProperties.get(EmailConstants.ACTION_AFTER_PROCESSED) , isImapFolder);

        //If action is 'move' then, have to check the folder to move the processed mail is given.
        // If not exception is thrown
        if (action.equals(EmailConstants.ActionAfterProcessed.MOVE)) {
            if (emailProperties.get(EmailConstants.MOVE_TO_FOLDER) != null) {
                try {
                    moveToFolder = store.getFolder(emailProperties.get(EmailConstants.MOVE_TO_FOLDER));
                    if (!moveToFolder.exists()) {
                        moveToFolder.create(Folder.HOLDS_MESSAGES);
                    }
                    openFolder(moveToFolder);
                } catch (MessagingException e) {
                    throw new EmailServerConnectorException("Couldn't process the folder '"
                            + moveToFolder + "'which used to move the processed mail", e);
                }
            } else {
                throw new EmailServerConnectorException(EmailConstants.MOVE_TO_FOLDER + "is a mandatory parameter "
                        + "if the action for the processed mail is MOVE. It couldn't be null.");
            }
        }
    }

    /**
     * Open the email folder if the folder is not open.
     *
     * @param folder Instance of the folder which used to fetch the email
     * @throws EmailServerConnectorException
     */
    protected void openFolder(Folder folder) throws EmailServerConnectorException {
        log.info("Folder name: " + folder.getName());
        if (store.isConnected()) {
            try {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_WRITE);
                } else {
                    closeFolder(folder);
                    folder.open(Folder.READ_WRITE);
                }
            } catch (MessagingException e) {
                throw new EmailServerConnectorException("Couldn't open the folder '"
                        + folderName + " ' in READ_WRITE mode"
                        + "in the email server connector with id '" + serviceId + "'", e);
            }
        } else {
            try {
                connectToEmailStore();
                folder.open(Folder.READ_WRITE);
            } catch (MessagingException e) {
                throw new EmailServerConnectorException("Couldn't open the folder '"
                        + folderName + " ' in READ_WRITE mode"
                        + "in the email server connector with id '" + serviceId + "'", e);
            }
        }

        //TODO remove this
        if (folder.isOpen()) {
            if (log.isDebugEnabled()) {
                log.debug("Folder is open: " + folderName);
            }
        }

    }

    /**
     * Close the folder if it is open.
     *
     * @param folder Instance of the folder which used to fetch the email
     * @throws EmailServerConnectorException
     */
    protected void closeFolder(Folder folder) throws EmailServerConnectorException {
        if (folder.isOpen()) {
            try {
                folder.close(true);
            } catch (MessagingException e) {
                log.warn("Couldn't close the folder '" + folderName + "by the email server connector with service id '"
                        + serviceId + "'", e);
            }
        }
    }

    /**
     * Fetch emails which satisfy the conditions in the search term. If search term is 'null',
     * then fetch all the emails. If folder is IMAP folder, then fetch emails from the new emails.
     * If the folder is pop3, then fetch all the emails in the folder which satisfy the given conditions.
     *
     * @return List of messages which satisfy the search conditions.
     * @throws EmailServerConnectorException
     */
    private List<Message> fetchEmails() throws EmailServerConnectorException {

        List<Message> messageList = null;

        if (log.isDebugEnabled()) {
            log.debug("Start to fetch the emails");
        }

        if (isImapFolder) {
                if (emailSearchTerm != null) {
                    try {
                        Message[] messages = folder.search(emailSearchTerm,
                                ((UIDFolder) folder).getMessagesByUID(startUIDNumber, UIDFolder.LASTUID));

                        if (messages.length > 0) {
                            //update the startUID number
                            startUIDNumber = ((UIDFolder) folder).getUID(messages[messages.length - 1]) + 1;
                            messageList = Arrays.asList(messages);
                        }

                    } catch (MessagingException e) {
                        throw new EmailServerConnectorException("Error is encountered while fetching emails using "
                                + "search term from the folder '" + folderName + "'"
                                + "by the email server connector with id '" + serviceId + "'" , e);
                    }

                } else {
                    log.warn("Conditions(Search Term) is not specified. All the mails in the folder '"
                            + folderName + "' will be fetched" + "by the email server connector "
                            + "with id '" + serviceId + "'");
                    try {
                        Message[] messages = ((UIDFolder) folder).getMessagesByUID(startUIDNumber, UIDFolder.LASTUID);
                        if (messages.length > 0) {
                             startUIDNumber = ((UIDFolder) folder).getUID(messages[messages.length - 1]) + 1;
                             messageList = Arrays.asList(messages);
                        }


                    } catch (MessagingException e) {
                        throw new EmailServerConnectorException("Error is encountered while fetching emails using "
                                + "search term from the folder '" + folderName + "'"
                                + "by the email server connector with id '" + serviceId + "'" , e);
                    }
                }
        // when folder is pop3Folder
        } else {
            if (emailSearchTerm != null) {
                try {
                    Message[] messages = folder.search(emailSearchTerm);
                    messageList = Arrays.asList(messages);
                } catch (MessagingException e) {
                    throw new EmailServerConnectorException("Error is encountered while fetching emails using "
                            + "search term from the folder '" + folderName + "'"
                            + "by the email server connector with id '" + serviceId + "'" , e);
                }
            } else {
                log.warn("Conditions(Search Term) is not specified. All the mails in the folder '"
                        + folderName + "' will be fetched" + "by the email server connector "
                        + "with id '" + serviceId + "'");
                try {
                    Message[] messages = folder.getMessages();
                    messageList = Arrays.asList(messages);
                } catch (MessagingException e) {
                    throw new EmailServerConnectorException("Error is encountered while fetching emails using "
                            + "search term from the folder '" + folderName + "'"
                            + "by the email server connector with id '" + serviceId + "'" , e);
                }
            }
        }

        if (log.isDebugEnabled()) {
            if (messageList != null) {
                if (isImapFolder) {
                    log.debug("'" + messageList.size() + "' number of message are fetched."
                            + "Last UID of the mail is '"
                    + (startUIDNumber - 1) + "'");
                } else {
                    log.debug("'" + messageList.size() + "' number of message are fetched.");
                }
            }
        }

        return messageList;
    }

    /**
     * Get the start uid number.
     *
     * @return startUIDNumber
     */

    protected Long getStartUIDNumber() {
        return startUIDNumber;
    }

    /**
     * Set the start uid number.
     *
     * @param startUIDNumber value of start uid number
     */
    protected void setStartUIDNumber(Long startUIDNumber) {
        this.startUIDNumber = startUIDNumber;
    }

    /**
     * Read the content of the message according to the content type.
     *
     * @param  message Message to read the content
     * @return Message content
     * @throws EmailServerConnectorException
     * @throws MessageRemovedException When message is deleted by another thread.
     */
    private String getEmailContent(Message message)
            throws EmailServerConnectorException , MessageRemovedException {
        String content = "";
        try {
            if (message instanceof MimeMessage) {
                if (message.isMimeType("text/plain")) {
                    if (contentType.equals("text/plain")) {
                        content = message.getContent().toString();
                    }
                } else if (message.isMimeType("text/html")) {
                    if (contentType.equals("text/plain")) {
                        content = message.getContent().toString();
                    }
                } else if (message.isMimeType("multipart/*")) {
                    MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                    try {
                        content = getMultiPart(mimeMultipart);
                    } catch (EmailServerConnectorException e) {
                        throw new EmailServerConnectorException("" + e);
                    }
                }
                return content;
            } else {
                throw new EmailServerConnectorException(" Couldn't read the content of the email"
                        + " since message is not a instance of MimeMessage");
            }
        } catch (MessageRemovedException e) {

            if (log.isDebugEnabled()) {
                log.debug("Skipping message # : " + message.getMessageNumber() +
                        " as it has been DELETED by another thread after processing");
            }

            throw new MessageRemovedException("Couldn't read the content of the message #"
                    + message.getMessageNumber() + "by the email server connector with service id '" + serviceId
                    + "' since it has been DELETED by another thread.", e);

        } catch (Exception e) {
            throw new EmailServerConnectorException("Error is encountered while reading the content of a message"
            + "by the email server connector with service id '" + serviceId + "'" , e);
        }
    }


   private String getMultiPart(MimeMultipart mimePart)
            throws Exception {
        String content = "";
        try {
            for (int i = 0; i < mimePart.getCount(); i++) {
                MimeBodyPart bodyPart = (MimeBodyPart) mimePart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    if (contentType.equals("text/plain")) {
                    content = (String) bodyPart.getContent();
                    }
                } else if (bodyPart.isMimeType("html/plain")) {
                    if (contentType.equals("text/plain")) {
                        content = (String) bodyPart.getContent();
                    }
                } else if (bodyPart.getContent() instanceof Multipart) {
                    content = content + getMultiPart((MimeMultipart) bodyPart.getContent());
                }
            }

        } catch (MessageRemovedException e) {
            throw new MessageRemovedException("Couldn't read the content of the message "
                    + "by the email server connector with service id '" + serviceId
                    + "' since it has been DELETED by another thread.", e);
        } catch (Exception e) {
            throw new EmailServerConnectorException("Error is encountered while reading the content of a message"
                    + "by the email server connector with service id '" + serviceId + "'" , e);
        }
        return content;
    }




}


