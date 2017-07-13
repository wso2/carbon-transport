package org.wso2.carbon.transport.email.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
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
 * Created by chathurika on 7/9/17.
 */
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
    private SearchTerm emailSearchTerm;
    private Map<String, String> emailProperties;
    private CarbonMessageProcessor emailMessageProcessor;
    private String seviceName;
    private Properties systemProperties = new Properties();
    private Session session;
    private Folder folder;
    private long startUIDNumber = 1;
    private Store store;
    private String host;
    private String username;
    private String password;
    private String storeType;
    private int maxRetryCount = 0;
    private Long retryInterval = 0L;
    private String folderName;
    private String contentType;
    private EmailConstants.ActionAfterProcessed action;
    private Folder moveToFolder = null;
    private ActionForProcessedMail actionForProcessedMail;
    private boolean isFirstTimeConnect = true;

    protected EmailConsumer(String id, Map<String, String> properties, SearchTerm emailSearchTerm,
            CarbonMessageProcessor emailMessageProcessor) throws EmailServerConnectorException {
        this.seviceName = id;
        this.emailProperties = properties;
        this.emailSearchTerm = emailSearchTerm;
        this.emailMessageProcessor = emailMessageProcessor;

        this.host = emailProperties.get(EmailConstants.HOST_NAME);
        this.username = emailProperties.get(EmailConstants.USERNAME);
        this.password = emailProperties.get(EmailConstants.PASSWORD);
        this.storeType = emailProperties.get(EmailConstants.STORE_TYPE);

        if (emailProperties.get(EmailConstants.MAX_RETRY_COUNT) != null) {
            try {
                this.maxRetryCount = Integer.parseInt(emailProperties.get(EmailConstants.MAX_RETRY_COUNT));
            } catch (NumberFormatException e) {
                log.error("Could not parse parameter: " + emailProperties.get(EmailConstants.MAX_RETRY_COUNT)
                        + " to numeric type: Integer." + " Get default" +
                        emailProperties.get(EmailConstants.MAX_RETRY_COUNT) + ": " + maxRetryCount);
            }
        }

        if (emailProperties.get(EmailConstants.RETRY_INTERVEL) != null) {
            try {
                this.retryInterval = Long.parseLong(emailProperties.get(EmailConstants.RETRY_INTERVEL));
            } catch (NumberFormatException e) {
                log.error("Could not parse parameter: " + emailProperties.get(EmailConstants.RETRY_INTERVEL)
                        + " to numeric type: Long." + " Get default"
                        + emailProperties.get(EmailConstants.RETRY_INTERVEL) + ": " + retryInterval);
            }
        }

        if (emailProperties.get(EmailConstants.CONTENT_TYPE) != null ){
        if (emailProperties.get(EmailConstants.CONTENT_TYPE).equalsIgnoreCase("text/html")) {
                this.contentType = "text/html";
        } else if (emailProperties.get(EmailConstants.CONTENT_TYPE).equalsIgnoreCase("text/plain")) {
               contentType = "text/plain";
        }
        } else {
               log.warn("Email content type is not defined. Get default content type: text/plain");
               this.contentType = "text/plain";
        }

        if (emailProperties.get(EmailConstants.FOLDER_NAME) != null) {
            this.folderName = emailProperties.get(EmailConstants.FOLDER_NAME);

        } else {
            this.folderName = "INBOX";
            log.warn("Folder to fetch mails is not specified." +
                    "Get default folder: " + folderName);
        }

        systemProperties.putAll(emailProperties);

        session = Session.getDefaultInstance(systemProperties);

        if (storeType != null) {
            try {
                store = session.getStore(storeType);
            } catch (NoSuchProviderException e) {
                throw new EmailServerConnectorException("Couldn't initialize the store: " + storeType, e);
            }
        } else {
            throw new EmailServerConnectorException("Store type can't be null");
        }

        actionForProcessedMail = new ActionForProcessedMail();
    }

    protected void emailConsumer() throws EmailServerConnectorException {
        try {
            openFolder(folder);
        } catch (EmailServerConnectorException e) {
            throw new EmailServerConnectorException(e.toString());
        }

        List<Message> messageList = fetchEmails();

        if (messageList != null) {

            for (int i = 0; i < messageList.size(); i++) {
                try {
                    String content = getEmailContent(messageList.get(i));
                    CarbonMessage emailCarbonMessage;
                    emailCarbonMessage = EmailUtils
                            .createEmailCarbonMessage(messageList.get(i), folder, content, seviceName);
                    emailMessageProcessor.receive(emailCarbonMessage, null);
                    actionForProcessedMail.carryOutAction(messageList.get(i), folder, action, moveToFolder);

                } catch (MessageRemovedException e) {
                    // check and put message number
                    log.warn(" Mail has been deleted by another thread." + " Couldn't process the mail further");
                    continue;
                } catch (Exception e) {
                    log.warn("Couldn't process the Mail" + e.toString());
                }
            }

        }
        closeFolder(folder);
    }


    protected void connectToEmailStore() throws EmailServerConnectorException {
        int retryCount = 0;
        while (!store.isConnected()) {
            try {
                retryCount++;

                if (log.isDebugEnabled()) {
                    log.debug("Attempting to connect to POP3/IMAP server for : "
                            + emailProperties.get(EmailConstants.USERNAME) + " using " + session.getProperties());
                }

                store.connect(host, username, password);

            } catch (Exception e) {
                log.error("Error connecting to mail server for address : " + username, e);
                if (maxRetryCount <= retryCount) {
                    throw new EmailServerConnectorException("Error connecting to mail server for the address: "
                            + username, e);
                }
            }

            if (store.isConnected()) {
                if (log.isDebugEnabled()) {
                    log.debug("Connect to the server: " + store);
                }
                if (emailProperties.get(EmailConstants.FOLDER_NAME) != null) {
                    folderName = emailProperties.get(EmailConstants.FOLDER_NAME);

                } else {
                    folderName = "INBOX";
                    log.warn("Folder to fetch mails is not specified." +
                            "Get default folder: " + folderName);
                }

                if (isFirstTimeConnect) {
                try {
                    folder = store.getFolder(folderName);
                    isFirstTimeConnect = false;
                } catch (MessagingException e) {
                    log.warn(e.toString());
                }
                }
            }

            if (!store.isConnected()) {
                try {
                    log.warn("Connection to mail server for account : " + username + " failed. Retrying in : "
                            + retryInterval / 1000 + " seconds");
                    Thread.sleep(retryCount);
                } catch (InterruptedException ignore) {

                }
            }
        }
    }


    protected void setAction() throws EmailServerConnectorException {
        action = EmailUtils.getActionAfterProcessed(emailProperties.get(EmailConstants.ACTION_AFTER_PROCESSED) ,
                folder);

        if (action.equals(EmailConstants.ActionAfterProcessed.MOVE)) {
            if (emailProperties.get(EmailConstants.MOVE_TO_FOLDER) != null) {
                try {
                    moveToFolder = store.getFolder(emailProperties.get(EmailConstants.MOVE_TO_FOLDER));
                    if (!moveToFolder.exists()) {
                        moveToFolder.create(Folder.HOLDS_MESSAGES);
                    }
                    openFolder(moveToFolder);
                } catch (MessagingException e) {
                    throw new EmailServerConnectorException("couldn't process the folder '"
                            + emailProperties.get(EmailConstants.MOVE_TO_FOLDER), e);
                }
            } else {
                throw new EmailServerConnectorException(EmailConstants.MOVE_TO_FOLDER + "is a mandatory parameter "
                        + "if the processed emails have to MOVE to a another parameter. It couldn't be null.");
            }
        }
    }
    protected void closeFolder(Folder folder) throws EmailServerConnectorException {
        if (folder.isOpen()) {
            try {
                folder.close(true);
            } catch (MessagingException e) {
                log.warn("Couldn't close the folder: " + folderName , e);
            }
        }
    }

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
                log.error("Couldn't open the folder: " + folderName + " in READ_WRITE mode", e);
            }
        } else {
            try {
                connectToEmailStore();
                folder.open(Folder.READ_WRITE);
            } catch (MessagingException e) {
                log.error("Couldn't open the folder: " + folderName + " in READ_WRITE mode", e);
            }
        }
        if (folder.isOpen()){
            if (log.isDebugEnabled()) {
                log.debug("Folder is open: " + folderName);
            }
        }

    }

    private List<Message> fetchEmails() throws EmailServerConnectorException {

        List<Message> emailMessages = null;

        if (store.isConnected()) {
            if (log.isDebugEnabled()) {
                log.debug("connection available inside fetch method" + store);
            }
        }

        if (folder.isOpen()) {
            if (log.isDebugEnabled()) {
                log.debug("Folder is open : " + folder);
            }
        }
        if (folder instanceof UIDFolder) {
                if (emailSearchTerm != null) {
                    try {
                        Message[] messages = folder.search(emailSearchTerm,
                                ((UIDFolder) folder).getMessagesByUID(startUIDNumber, UIDFolder.LASTUID));
                        if (messages != null) {
                            startUIDNumber = ((UIDFolder) folder).getUID(messages[messages.length - 1]) + 1;
                            emailMessages = Arrays.asList(messages);
                        }

                    } catch (MessagingException e) {
                        log.error("Error is count while fetching mails using Search Term from the folder '"
                                + folderName + "'" , e);
                    }
                } else {

                    log.warn("Conditions(Search Term) is not specified. All the mails in the folder '"
                            + folderName + "' will be fetched");
                    try {
                        Message[] messages = ((UIDFolder) folder).getMessagesByUID(startUIDNumber, UIDFolder.LASTUID);
                        if (messages != null) {
                             startUIDNumber = ((UIDFolder) folder).getUID(messages[messages.length - 1]) + 1;
                             emailMessages = Arrays.asList(messages);
                        }


                    } catch (MessagingException e) {
                        log.error("Error is count while fetching all mails from the folder '" + folderName + "'" , e);
                    }
                }

        } else {
            if (emailSearchTerm != null) {
                try {
                    Message[] messages = folder.search(emailSearchTerm);
                    emailMessages = Arrays.asList(messages);
                } catch (MessagingException e) {
                    log.error("Error is count while fetching mails using Search Term from the folder '"
                            + folderName + "'" , e);
                }
            } else {
                try {
                    log.warn("Conditions(Search Term) is not specified. All the mails in the folder '"
                            + folderName + "' will be fetched");
                    Message[] messages = folder.getMessages();
                    emailMessages = Arrays.asList(messages);
                } catch (MessagingException e) {
                    log.error("Error is count while fetching all emails from the folder '"
                            + folderName + "'" , e);
                }
            }
        }

        return emailMessages;
    }

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
            throw new MessageRemovedException(e.toString());
        } catch (Exception e) {
            throw new EmailServerConnectorException(e.toString());
        }
    }

    private String getMultiPart(MimeMultipart mimePart)
            throws MessageRemovedException, Exception {
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
            throw new MessageRemovedException(e.toString());
        } catch (Exception e) {
            throw new Exception(e.toString());
        }
        return content;
    }




}


