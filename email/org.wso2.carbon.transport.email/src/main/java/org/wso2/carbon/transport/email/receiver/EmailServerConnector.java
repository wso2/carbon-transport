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
 * Class implementing email server connector.
 */

public class EmailServerConnector extends PollingServerConnector {
    private static final Logger log = LoggerFactory.getLogger(EmailServerConnector.class);
    /**
     * The instance of SearchTerm class in javax.mail Api  that used to filter the relevant email from the email folder.
     */
    private SearchTerm emailSearchTerm;

    /**
     * The {@link CarbonMessageProcessor} instance represents the carbon message processor that handles the out going
     * messages.
     */
    private CarbonMessageProcessor emailMessageProcessor;

    /**
     * Default value that used for polling interval if user does't provide it in email property map.
     */
    private Long emailConnectorDefaultPollingInterval = 360000L;

    /**
     * The instance of the EmailConsumer class which carry out task related to the email receiver.
     */
    private EmailConsumer emailConsumer = null;

    /**
     * Emails which have an UID equal or greater than the startUIDNumber are only taken to process.
     * This is only applicable if and only if, folder is a UIDFolder (IMAP folder).
     * Default UID is 1.
     */
    private Long startUIDNumber = 1L;

    /**
     * Creates a email server connector with the id.
     *
     * @param id Unique identifier for the server connector.
     * @param properties Map which contain data needed to initialize the email server connector
     */
    public EmailServerConnector(String id, Map<String, String> properties) {
        this(id, properties, (SearchTerm) null);
    }

    /**
     * Creates a email server connector with the id.
     *
     * @param id Unique identifier for the server connector.
     * @param properties Map which contain data needed to initialize the email server connector
     * @param stringEmailSearchTerm String which contains the condition to Search the email.
     *                              String search term should define ':' separated key and value
     *                              with ',' separated key value pairs. Currently, this string search term
     *                              only supported keys: subject, from, to, bcc, and cc.
     *                              As an example: " subject:DAS , from:carbon , bcc:wso2 " string search term create a
     *                              search term instance which filter emails which contain DAS in the subject,
     *                              carbon in the from address and wso2 in one of the bcc addresses.
     */
    public EmailServerConnector(String id, Map<String, String> properties, String stringEmailSearchTerm) {
        this(id, properties, stringToSearchTermConverter(stringEmailSearchTerm));
    }

    /**
     *
     * @param id Unique identifier for the server connector.
     * @param properties Map which contain data needed to initialize the email server connector
     * @param emailSearchTerm Instance of the SearchTerm class
     *                              implemented by javax.mail api to filter the emails.
     */
    public EmailServerConnector(String id, Map<String, String> properties, SearchTerm emailSearchTerm) {
        super(id, properties);
        this.emailSearchTerm = emailSearchTerm;
        interval = emailConnectorDefaultPollingInterval;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.emailMessageProcessor = carbonMessageProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() throws ServerConnectorException {
         /*
        not needed for email, as this will be called in server start-up. We will not know about
        the destination at server start-up. We will get to know about that in service deployment.
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws ServerConnectorException {
        emailConsumer = new EmailConsumer(id, getProperties(), emailSearchTerm, emailMessageProcessor);
        emailConsumer.setStartUIDNumber(this.startUIDNumber);
        emailConsumer.connectToEmailStore();
        emailConsumer.setAction();
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void poll() {
        try {
            emailConsumer.emailConsumer();
        } catch (EmailServerConnectorException e) {
            log.error(" Error is encountered while executing the polling cycle of email "
                    + "server connector for service: " + id, e);
        }
    }

    //TODO check regex, if it is not in correct format then throw exception

    /**
     * Convert string search term to SearchTerm class instance.
     *
     * @param stringSearchTerm String which includes conditions as a key value pairs to search emails
     * @return SearchTerm instance of string search term.
     */
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
            log.error("All Key value pairs in the given String email search term are not in correct format.");
        }

        if (searchTermsList.size() > 0) {
            SearchTerm[] searchTerms = searchTermsList.toArray(new SearchTerm[searchTermsList.size()]);
            searchTerm = new AndTerm(searchTerms);
        } else {
            log.error("All Key value pairs in the given string email search term are not in correct format."
                    + " Therefore, return null email search term ");
        }

        return searchTerm;

    }

    /**
     * Set the start UID number
     *
     * @param startUIDNumber Value to set as a start UID number.
     */
    protected void setStartUIDNumber(Long startUIDNumber) {
        this.startUIDNumber = startUIDNumber;
    }

    /**
     * Get the start UID number
     *
     * @return start uid number
     */
    protected Long getStartUIDNumber() {
        if (emailConsumer != null) {
            this.startUIDNumber = emailConsumer.getStartUIDNumber();
        }
        return startUIDNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void destroy() throws ServerConnectorException {

    }

    /**
     * {@inheritDoc}
     */
    @Override public void stop() throws ServerConnectorException {

    }
}

