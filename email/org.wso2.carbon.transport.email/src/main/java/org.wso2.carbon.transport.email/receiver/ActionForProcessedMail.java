package org.wso2.carbon.transport.email.receiver;

import org.wso2.carbon.transport.email.exception.EmailServerConnectorException;
import org.wso2.carbon.transport.email.utils.EmailConstants;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

/**
 * Created by chathurika on 7/10/17.
 */
public class ActionForProcessedMail {

    public void carryOutAction(Message message, Folder folder,
            EmailConstants.ActionAfterProcessed action, Folder folderToMove)
            throws MessagingException, EmailServerConnectorException {
        // folder create at the constructor of the consume
        Message[] messages = { message };
        try {
            switch (action) {
            case MOVE:
                if (!folderToMove.isOpen()) {
                    folderToMove.open(Folder.READ_WRITE);
                }
                folder.copyMessages(messages, folderToMove);
                message.setFlag(Flags.Flag.DELETED, true);
                folder.expunge();
                break;
            case SEEN:
                message.setFlag(Flags.Flag.SEEN, true);
                break;
            case DELET:
                message.setFlag(Flags.Flag.DELETED, true);
                if (folder instanceof UIDFolder) {
                    folder.expunge();
                }
                break;
            case FLAGGED:
                message.setFlag(Flags.Flag.FLAGGED, true);
                break;
            case ANSWERED:
                message.setFlag(Flags.Flag.ANSWERED, true);
                break;
            case KEEPSAME:
                break;
            }
        } catch (MessageRemovedException e) {
            throw new MessageRemovedException(e.toString());
        } catch (NullPointerException e) {
            throw new MessageRemovedException(e.toString());
        } catch (Exception e) {
            throw new EmailServerConnectorException(e.toString(), e);
        }
    }
}
