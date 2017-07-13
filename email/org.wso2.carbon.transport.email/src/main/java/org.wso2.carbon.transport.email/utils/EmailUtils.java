package org.wso2.carbon.transport.email.utils;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.email.exception.EmailServerConnectorException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;


/**
 * Created by chathurika on 7/10/17.
 */
public class EmailUtils {

    private static final Logger log = LoggerFactory.getLogger(EmailUtils.class);
    public static EmailConstants.ActionAfterProcessed getActionAfterProcessed(String action, Folder folder) {
        String actionInUpperCase;
        if (action != null){
             actionInUpperCase = action.toUpperCase();
        } else {
             actionInUpperCase = "";
        }

        EmailConstants.ActionAfterProcessed actionAfterProcessed = null;
        if (folder instanceof IMAPFolder) {
            switch (actionInUpperCase) {
            case "SEEN":
                return EmailConstants.ActionAfterProcessed.SEEN;
            case "FLAGGED":
                return EmailConstants.ActionAfterProcessed.FLAGGED;
            case "ANSWERED":
                return EmailConstants.ActionAfterProcessed.ANSWERED;
            case "DELETE":
                return EmailConstants.ActionAfterProcessed.DELET;
            case "KEEPSAME":
                return EmailConstants.ActionAfterProcessed.KEEPSAME;
            case "MOVE":
                return EmailConstants.ActionAfterProcessed.MOVE;
            case "":
                log.warn(" action after processd mail parameter is not defined" + " Get default action : SEEN");
                return EmailConstants.ActionAfterProcessed.SEEN;
            default:
                log.warn(" action '" + action + "' is not supported by IMAPFolder."
                        + " Get default action: KEEPSAME");
                return EmailConstants.ActionAfterProcessed.KEEPSAME;


            }
        } else {
            switch (actionInUpperCase) {
            case "DELET":
                return EmailConstants.ActionAfterProcessed.DELET;
            case "KEEPSAME":
                return EmailConstants.ActionAfterProcessed.KEEPSAME;
            case "":
                log.warn(" action after processd mail parameter is not defined"
                        + " Get default action : SEEN");
                return EmailConstants.ActionAfterProcessed.KEEPSAME;
            default:
                log.warn(" action '" + action + "' is not supported by POP3Folder."
                        + " Get default action: KEEPSAME");
                return EmailConstants.ActionAfterProcessed.KEEPSAME;

            }
        }
    }

    public static CarbonMessage createEmailCarbonMessage (Message message, Folder folder ,
            String emailMessageContent , String serviceName)
            throws MessageRemovedException, EmailServerConnectorException {
        try {
            CarbonMessage carbonMessage = new TextCarbonMessage(emailMessageContent);
            carbonMessage.setProperty(EmailConstants.SERVICE_NAME, serviceName);
            carbonMessage.setProperty(EmailConstants.FROM, message.getFrom());
            carbonMessage.setProperty(EmailConstants.SEND_DATE, message.getSentDate());
            carbonMessage.setProperty(EmailConstants.MESSAGE_NUMBER, message.getMessageNumber());
            carbonMessage.setProperty(EmailConstants.IS_EXPUNGED, message.isExpunged());
            carbonMessage.setProperty(EmailConstants.TO, message.getRecipients(MimeMessage.RecipientType.TO));
            carbonMessage.setProperty(EmailConstants.BCC, message.getRecipients(Message.RecipientType.BCC));
            carbonMessage.setProperty(EmailConstants.CC, message.getRecipients(Message.RecipientType.CC));
            carbonMessage.setProperty(EmailConstants.SUBJECT, message.getSubject());

            if (message.getRecipients(Message.RecipientType.BCC) != null) {
                carbonMessage.setProperty(EmailConstants.BCC, message.getRecipients(Message.RecipientType.BCC));
            }

            if (message.getRecipients(Message.RecipientType.CC) != null) {
                carbonMessage.setProperty(EmailConstants.CC, message.getRecipients(Message.RecipientType.CC));
            }

            if (message.getReplyTo() != null) {
                carbonMessage.setProperty(EmailConstants.REPLY_TO, message.getReplyTo());
            }

            if (folder instanceof UIDFolder) {
                carbonMessage.setProperty(EmailConstants.FLAGS, message.getFlags());
                carbonMessage.setProperty(EmailConstants.RECEIVED_DATE, message.getReceivedDate());
                carbonMessage.setProperty(EmailConstants.MESSAGE_UID, ((UIDFolder) folder).getUID(message));
            } else {
                if (folder instanceof POP3Folder) {
                carbonMessage.setProperty(EmailConstants.MESSAGE_UID, ((POP3Folder) folder).getUID(message));
                }
            }

                return carbonMessage;

        } catch (MessageRemovedException e) {
            throw new MessageRemovedException(e.toString());
        } catch (MessagingException e) {
            throw new EmailServerConnectorException(e.toString());
        }

    }
}
