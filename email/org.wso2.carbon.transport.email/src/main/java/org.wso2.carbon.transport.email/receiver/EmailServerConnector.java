package org.wso2.carbon.transport.email.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.connector.framework.server.polling.PollingServerConnector;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.email.exception.EmailServerConnectorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;


/**
 * Created by chathurika on 7/9/17.
 */
public class EmailServerConnector extends PollingServerConnector {
    private static final Logger log = LoggerFactory.getLogger(EmailServerConnector.class);
    private SearchTerm emailSearchTerm;
    private CarbonMessageProcessor emailMessageProcessor;
    private static final long EMAIL_CONNECTOR_DEFAULT_INTERVAL = 10000L;
    private EmailConsumer emailConsumer = null;

    public EmailServerConnector(String id, Map<String, String> properties) {
        this(id, properties, (SearchTerm) null);
        //super(id, properties);
        // interval = EMAIL_CONNECTOR_DEFAULT_INTERVAL;
    }

    public EmailServerConnector(String id, Map<String, String> properties, String stringEmailSearchTerm) {
        this(id, properties, stringToSearchTermConverter(stringEmailSearchTerm));
    }

    public EmailServerConnector(String id, Map<String, String> emailProperties, SearchTerm emailSearchTerm) {
        super(id, emailProperties);
        this.emailSearchTerm = emailSearchTerm;
        interval = EMAIL_CONNECTOR_DEFAULT_INTERVAL;

    }

    @Override public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.emailMessageProcessor = carbonMessageProcessor;
    }

    @Override protected void init() throws ServerConnectorException {

    }

    @Override public void start() throws ServerConnectorException {

        emailConsumer = new EmailConsumer(id, getProperties(), emailSearchTerm, emailMessageProcessor);
        emailConsumer.connectToEmailStore();
        emailConsumer.setAction();
        super.start();
    }

    @Override protected void poll() {
        try {
            emailConsumer.emailConsumer();
        } catch (EmailServerConnectorException e) {
            log.error(" Error is encountered while executing the polling cycle of email "
                    + "server connector for service: " + id, e);
        }
    }

    private static SearchTerm stringToSearchTermConverter(String stringSearchTerm) {

        SearchTerm searchTerm = null;
        Map<String, String> searchConditionMap = new HashMap<>();
        List<SearchTerm> searchTermsList = new ArrayList<>();

        stringSearchTerm = stringSearchTerm.replaceAll("\\s+", "");
        String condition[] = stringSearchTerm.split(",");

        for (int i = 0; i < condition.length; i++) {
            String[] nameValuePair = condition[i].split(":");
            if (nameValuePair.length == 2) {
                searchConditionMap.put(nameValuePair[0].toUpperCase(Locale.ENGLISH), nameValuePair[1]);
            } else {

                     log.warn("The given key value pair '" + nameValuePair[i]
                         + "' in String search term is not in the correct format");
            }
        }

        if (searchConditionMap.size() > 0) {

            for (Map.Entry<String, String> entry : searchConditionMap.entrySet()) {
                switch (entry.getKey()) {
                case "SUBJECT":
                    try {
                        SearchTerm subjectTerm = new SubjectTerm(entry.getValue());
                        searchTermsList.add(subjectTerm);
                    } catch (Exception e) {
                        log.error("Error is encountered while searching the message using subject", e);
                    }

                    break;

                case "FROM":
                    String fromAddress = entry.getValue().toLowerCase(Locale.ENGLISH);
                    SearchTerm fromAddressTerm = new SearchTerm() {
                        @Override public boolean match(Message message) {

                            try {
                                Address[] from = message.getFrom();
                                for (Address ad : from) {
                                    String fromAd = ((InternetAddress) ad).getAddress();
                                        if (fromAd.contains(fromAddress)) {
                                            return true;
                                        }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using From address", e);
                            }

                            return false;
                        }
                    };
                    searchTermsList.add(fromAddressTerm);
                    break;

                case "TO":
                    String toAddress = entry.getValue().toLowerCase(Locale.ENGLISH);
                    SearchTerm toAddressTerm = new SearchTerm() {
                        @Override public boolean match(Message message) {

                            try {
                                if (message.getRecipients(Message.RecipientType.TO) != null) {
                                    Address[] to = message.getRecipients(Message.RecipientType.TO);
                                    for (Address ad : to) {
                                        String toAd = ((InternetAddress) ad).getAddress();
                                            if (toAd.contains(toAddress)) {
                                                return true;
                                        }
                                    }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using To address", e);
                            }

                            return false;
                        }
                    };
                    searchTermsList.add(toAddressTerm);
                    break;

                case "BCC":
                    String bccAddress = entry.getValue().toLowerCase(Locale.ENGLISH);
                    SearchTerm bccAddressTerm = new SearchTerm() {
                        @Override public boolean match(Message message) {

                            try {
                                if (message.getRecipients(Message.RecipientType.BCC) != null) {
                                    Address[] bcc = message.getRecipients(Message.RecipientType.BCC);
                                    for (Address ad : bcc) {
                                        String bccAd = ((InternetAddress) ad).getAddress();
                                            if (bccAd.contains(bccAddress)) {
                                                return true;
                                        }
                                    }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using from address", e);
                            }

                            return false;
                        }
                    };
                    searchTermsList.add(bccAddressTerm);
                    break;

                case "CC":
                    String ccAddress = entry.getValue().toLowerCase(Locale.ENGLISH);
                    SearchTerm ccAddressTerm = new SearchTerm() {
                        @Override public boolean match(Message message) {

                            try {
                                if (message.getRecipients(Message.RecipientType.CC) != null) {
                                    Address[] cc = message.getRecipients(Message.RecipientType.CC);
                                    for (Address ad : cc) {
                                        String ccAd = ((InternetAddress) ad).getAddress();
                                            if (ccAd.contains(ccAddress)) {
                                                return true;

                                        }
                                    }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using Cc address", e);
                            }

                            return false;
                        }
                    };
                    searchTermsList.add(ccAddressTerm);
                    break;

                default:
                    log.error("The given key '" + entry.getKey() + "' in the String email search term "
                            + "is not supported by the email transport ");
                }
            }
        } else {
            log.error(" All Key value pairs in the given String email search term are not in correct format.");
        }

        if (searchTermsList.size() > 0) {
            SearchTerm[] searchTerms = searchTermsList.toArray(new SearchTerm[searchTermsList.size()]);
            searchTerm = new AndTerm(searchTerms);
        } else {
            log.error(" All Key value pairs in the given string email search term are not in correct format."
                    + " Therefore, return null email search term ");
        }

        return searchTerm;

    }

    @Override protected void destroy() throws ServerConnectorException {

    }

    @Override public void stop() throws ServerConnectorException {

    }
}

