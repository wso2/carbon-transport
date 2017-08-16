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
import org.wso2.carbon.transport.email.utils.EmailConstants;

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
 * This is the class implementing email server connector. The email server connector has ability
 * to poll the email account and search for the new mails which satisfy the conditions given
 * in the email searchTerm. Email server connector supports receiving email through 'imap' or 'pop3' server.
 * If mail receiving server is pop3, then it supports only 'delete' for the processed mails.
 * For the imap server, it supports different actions for processed mails like setting flags,
 * deleting or moving to another folder.
 */

public class EmailServerConnector extends PollingServerConnector {
    private static final Logger log = LoggerFactory.getLogger(EmailServerConnector.class);

    /**
     * The {@link CarbonMessageProcessor} instance represents the carbon message processor that handles the out going
     * messages.
     */
    private CarbonMessageProcessor emailMessageProcessor;

    /**
     * The instance of SearchTerm class in javax.mail Api  that used to filter relevant emails from the email folder.
     */
    private SearchTerm emailSearchTerm;

    /**
     * The String in a formatted way which is going to convert to the Search Term object.
     */
    private String stringEmailSearchTerm = null;

    /**
     * UID number to start searching emails.
     */
    private Long startUIDNumber = 1L;

    /**
     * Default value that used for polling interval. The value is override if it is provided in email property map.
     */
    private Long emailConnectorDefaultPollingInterval = 360000L;

    /**
     * The instance of the EmailConsumer class which carryout task related to the email receiver.
     */
    private EmailConsumer emailConsumer = null;

    /**
     * Creates a email server connector with the id.
     *
     * @param id         Unique identifier for the server connector.
     * @param properties Map which contains data needed to initialize the email server connector
     */
    public EmailServerConnector(String id, Map<String, String> properties) {
        super(id, properties);
        this.stringEmailSearchTerm = properties.get(EmailConstants.SEARCH_TERM);
        interval = emailConnectorDefaultPollingInterval; //this might be overridden in super.start()
    }

    /**
     * {@inheritDoc}
     */
    @Override public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.emailMessageProcessor = carbonMessageProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void init() throws ServerConnectorException {
        /*
        Not needed for email, as this will be called in server start-up. We will not know about
        the destination at server start-up. We will get to know about that in service deployment.
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override public void start() throws ServerConnectorException {

        if (stringEmailSearchTerm != null) {
            //convert string search term to SearchTerm instance
            this.emailSearchTerm = stringToSearchTermConverter(stringEmailSearchTerm);
        }
        emailConsumer = new EmailConsumer(id, getProperties(), emailSearchTerm, emailMessageProcessor);

        //This is important if email store is 'imap'. By setting the UID, it start to process mail at the point
        //it stop in the previous polling cycle. Initial startUID is 1.
        emailConsumer.setStartUIDNumber(startUIDNumber);
        emailConsumer.connectToEmailStore();
        emailConsumer.setAction();
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void poll() {
        try {
            emailConsumer.consume();
        } catch (EmailServerConnectorException e) {
            log.error(" Error is encountered while executing the polling cycle of email "
                    + "server connector for service: " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void destroy() throws ServerConnectorException {
        try {
            if (emailConsumer != null) {
                emailConsumer.closeAll();

            }
        } finally {
            emailConsumer = null;
            startUIDNumber = 1L;
        }
        stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override public void stop() throws ServerConnectorException {
        super.stop();
        if (emailConsumer != null) {
            this.startUIDNumber = emailConsumer.getStartUIDNumber();
        }
    }

    /**
     * Convert string search term to 'SearchTerm' class instance provided by javax.mail api.
     *
     * @param stringSearchTerm String which includes conditions as a key value pairs to search emails
     *                         String search term should define ':' separated key and
     *                         value with ',' separated key value pairs.
     *                         Currently, this string search term only supported keys: subject, from, to, bcc, and cc.
     *                         As an example: " subject:DAS , from:carbon , bcc:wso2 " string search term create
     *                         a search term instance which filter emails contain 'DAS' in the subject, 'carbon'
     *                         in the from address and 'wso2' in one of the bcc addresses. It does sub string matching
     *                         which is case insensitive. But if '@' contains in the given value except for
     *                         'subject' key, then it check whether address is equal or not. As a example "from: abc@"
     *                         string search term check whether 'from' address is equal to 'abc' before '@' Symbol.
     * @return SearchTerm instance created by converting string search term.
     */
    private SearchTerm stringToSearchTermConverter(String stringSearchTerm) throws EmailServerConnectorException {

        SearchTerm searchTerm = null;
        Map<String, String> searchConditionMap = new HashMap<>();
        List<SearchTerm> searchTermsList = new ArrayList<>();
        String pattern = "^(([ ]*[a-zA-Z]*[ ]*:[^:,]*,[ ]*)*[ ]*[a-zA-Z]*[ ]*:[^:,]*$)";

        if (!(stringSearchTerm.matches(pattern))) {
            throw new EmailServerConnectorException(
                    "String search term '" + stringSearchTerm + "' is not in correct format.");
        }

        String condition[] = stringSearchTerm.split(",");

        for (int i = 0; i < condition.length; i++) {
            String[] nameValuePair = condition[i].split(":");
            if (nameValuePair.length == 2) {
                searchConditionMap.put(nameValuePair[0].trim().toUpperCase(Locale.ENGLISH), nameValuePair[1].trim());
            } else {
                throw new EmailServerConnectorException("The given key value pair '" + nameValuePair[i]
                        + "' in string search term is not in the correct format.");
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
                        log.error("Error is encountered while searching messages using subject."
                                + " in the email server connector with id:" + id, e);
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
                                     if (fromAddress.contains("@")) {
                                        //if given address term consists '@', then should check for equality.
                                        if (fromAd.startsWith(fromAddress)) {
                                            return true;
                                        }
                                     } else {
                                        //check whether address contains given substring in address term.
                                        if (fromAd.contains(fromAddress)) {
                                            return true;
                                        }
                                     }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using From address"
                                        + " in the email server connector with id:" + id, e);
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
                                        if (toAddress.contains("@")) {
                                            if (toAd.startsWith(toAddress)) {
                                                return true;
                                            }
                                        } else {
                                            if (toAd.contains(toAddress)) {
                                                return true;
                                            }
                                        }
                                    }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using To address"
                                        + " in the email server connector with id:" + id, e);
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
                                        if (bccAddress.contains("@")) {
                                            if (bccAd.startsWith(bccAddress)) {
                                                return true;
                                            }
                                        } else {
                                            if (bccAd.contains(bccAddress)) {
                                                return true;
                                            }
                                        }

                                    }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using bcc address"
                                        + " in the email server connector with id:" + id, e);
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
                                        if (ccAddress.contains("@")) {
                                            if (ccAd.startsWith(ccAddress)) {
                                                return true;
                                            }
                                        } else {
                                            if (ccAd.contains(ccAddress)) {
                                                return true;
                                            }
                                        }
                                    }
                                }

                            } catch (MessagingException e) {
                                log.error("Error is encountered while searching the message using Cc address"
                                        + " in the email server connector with id:" + id, e);
                            }

                            return false;
                        }
                    };
                    searchTermsList.add(ccAddressTerm);
                    break;

                default:
                    throw new EmailServerConnectorException(
                            "The given key '" + entry.getKey() + "' in the String email search term "
                                    + "is not supported by" + " the email transport");
                }
            }
        }

        searchTerm = new AndTerm(searchTermsList.toArray(new SearchTerm[searchTermsList.size()]));
        return searchTerm;
    }

}

