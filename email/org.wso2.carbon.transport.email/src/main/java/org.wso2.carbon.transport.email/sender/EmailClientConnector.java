package org.wso2.carbon.transport.email.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.email.utils.EmailConstants;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

/**
 * Created by chathurika on 7/18/17.
 */
public class EmailClientConnector implements ClientConnector {
    private static final Logger logger = LoggerFactory.getLogger(EmailClientConnector.class);

    private Session session;
    private Properties prop;

    @Override public boolean send(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws ClientConnectorException {
        return false;
    }

    @Override public synchronized boolean send(CarbonMessage carbonMessage, CarbonCallback carbonCallback,
            Map<String, String> emailProperties) throws ClientConnectorException {
        String username;
        String password;
        String hostName;
        Properties serverProperties = new Properties();

        if (emailProperties.get(EmailConstants.MAIL_SENDER_USERNAME) != null) {
            username = emailProperties.get(EmailConstants.MAIL_SENDER_USERNAME);
        } else {
            throw new ClientConnectorException(
                    "Username (email address) of the email account is" + " a mandatory parameter."
                            + "It is not given in the email property map");
        }

        if (emailProperties.get(EmailConstants.MAIL_SENDER_PASSWORD) != null) {
            password = emailProperties.get(EmailConstants.MAIL_SENDER_PASSWORD);
        } else {
            throw new ClientConnectorException("Password of the email account is" + " a mandatory parameter."
                    + "It is not given in the email property map");
        }

        if (emailProperties.get(EmailConstants.MAIL_SENDER_HOST_NAME) != null) {
            hostName = emailProperties.get(EmailConstants.MAIL_SENDER_HOST_NAME);
        } else {
            throw new ClientConnectorException("HostName of the email account is" + " a mandatory parameter."
                    + "It is not given in the email property map");
        }

        if (carbonMessage instanceof TextCarbonMessage) {
            String textData = ((TextCarbonMessage) carbonMessage).getText();
        } else {
            throw new ClientConnectorException("Email client connector is support Text Carbon Message only.");
        }

        Properties properties = new Properties();

        for (Map.Entry<String, String> entry : emailProperties.entrySet()) {
            if (entry.getKey().startsWith("mail.")) {
                serverProperties.put(entry.getKey(), entry.getValue());
            }
        }
        properties.putAll(serverProperties);
        /*properties.put("mail.smtp.port", "587"); //TLS Port
        properties.put("mail.smtp.auth", "true"); //enable authentication
        properties.put("mail.smtp.starttls.enable", "true");
        properties = getProperties(properties);
        properties.put("mail.smtp.from" , "dck.amarathunga@gmail.com");*/

       /* properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465"); */

        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = createMessage(session, carbonMessage, emailProperties);

        try {
            Transport transport = session.getTransport();
            transport.connect(hostName, username, password);
            transport.send(message);
            transport.close();

        } catch (MessagingException e) {
            throw new ClientConnectorException("Error occurred while sending the message:", e);
        }

        if (logger.isDebugEnabled()) {
            logger.info("Message is send successfully" + message.toString());
        }
        return false;
    }

    private Message createMessage(Session session, CarbonMessage carbonMessage, Map<String, String> emailProperties)
            throws ClientConnectorException {
        Message message = new MimeMessage(session);
        String contentType;

        if (emailProperties.get(EmailConstants.MAIL_HEADER_CONTENT_TYPE) != null) {
            if (emailProperties.get(EmailConstants.MAIL_HEADER_CONTENT_TYPE).equalsIgnoreCase("text/plain")) {
                contentType = "text/plain";
            } else if (emailProperties.get(EmailConstants.MAIL_HEADER_CONTENT_TYPE).equalsIgnoreCase("text/html")) {
                contentType = "text/html";
            } else {
                throw new ClientConnectorException(
                        "Email content type should be either 'text/plain' or 'text/html'. But found '"
                                + emailProperties.get(EmailConstants.CONTENT_TYPE) + "'");
            }
        } else {
            logger.warn("Email content type is not given. Default value:'text/plain' is taken as a content type");
            contentType = "text/plain";
        }

        String textData = ((TextCarbonMessage) carbonMessage).getText();

        try {
            message.setContent(textData, contentType);
            if (emailProperties.get(EmailConstants.MAIL_HEADER_SUBJECT) != null) {
                message.setSubject(emailProperties.get(EmailConstants.MAIL_HEADER_SUBJECT));
            } else {
                throw new ClientConnectorException("Subject of the email is not given");
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_TO) != null) {
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(emailProperties.get(EmailConstants.MAIL_HEADER_TO)));
            } else {
                throw new ClientConnectorException("RecipientType 'to' of the email is not given. It is a mandotory ");
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_BCC) != null) {
                message.setRecipients(Message.RecipientType.BCC,
                        InternetAddress.parse(emailProperties.get(EmailConstants.MAIL_HEADER_BCC)));
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_CC) != null) {
                message.setRecipients(Message.RecipientType.CC,
                        InternetAddress.parse(emailProperties.get(EmailConstants.MAIL_HEADER_CC)));
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_FROM) != null) {
                message.setFrom( new InternetAddress(emailProperties.get(EmailConstants.MAIL_HEADER_FROM)));
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_REPLY_TO) != null) {
                InternetAddress[] addresses = {new InternetAddress(emailProperties.get(EmailConstants.MAIL_HEADER_REPLY_TO))};
                message.setReplyTo(addresses);
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_IN_REPLY_TO) != null) {
                message.setHeader(EmailConstants.MAIL_HEADER_IN_REPLY_TO, emailProperties.get(EmailConstants.MAIL_HEADER_IN_REPLY_TO) );
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_MESSAGE_ID) != null) {
                message.setHeader(EmailConstants.MAIL_HEADER_MESSAGE_ID, emailProperties.get(EmailConstants.MAIL_HEADER_MESSAGE_ID) );
            }

            if (emailProperties.get(EmailConstants.MAIL_HEADER_REFERENCES) != null) {
                message.setHeader(EmailConstants.MAIL_HEADER_REFERENCES, emailProperties.get(EmailConstants.MAIL_HEADER_REFERENCES) );
            }

        } catch (MessagingException e) {
            throw new ClientConnectorException("Error occurred while setting the message content." + e.toString());
        }

        return message;

    }

    @Override public String getProtocol() {
        return null;
    }

    @Override public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {

    }
}
