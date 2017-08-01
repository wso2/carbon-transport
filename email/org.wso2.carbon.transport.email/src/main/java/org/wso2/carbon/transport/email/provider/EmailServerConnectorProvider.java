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

package org.wso2.carbon.transport.email.provider;

import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.ServerConnectorProvider;
import org.wso2.carbon.transport.email.receiver.EmailServerConnector;
import org.wso2.carbon.transport.email.utils.EmailConstants;
import java.util.List;
import java.util.Map;

import javax.mail.search.SearchTerm;

/**
 * Server connector provider for email.
 */
public class EmailServerConnectorProvider extends ServerConnectorProvider {
    /**
     * Creates a server connector provider for email with the protocol name.
     */
    //TODO change to mail
    public EmailServerConnectorProvider() {
        super(EmailConstants.PROTOCOL_EMAIL);
    }

    /**
     * Creates a server connector provider with the protocol name.
     *
     * @param protocol Name of the protocol
     */
    public EmailServerConnectorProvider(String protocol) {
        super(protocol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServerConnector> initializeConnectors() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerConnector createConnector(String id, Map<String, String> emailProperties) {
        return new EmailServerConnector(id, emailProperties);
    }

    /**
     * Return instance of EmailServerConnector
     *
     * @param id Unique service id
     * @param emailProperties Email property Map
     * @param emailSearchTerm SearchTerm instance to search email
     * @return EmailServer connector instance
     */
    public ServerConnector createConnector(String id, Map<String, String> emailProperties, SearchTerm emailSearchTerm) {
        return new EmailServerConnector(id, emailProperties, emailSearchTerm);
    }

    /**
     * Return instance of EmailServerConnector
     *
     * @param id Unique service id
     * @param emailProperties Email property Map
     * @param stringEmailSearchTerm String which contains the condition to Search the email.
     *                              String search term should define ':' separated key and value
     *                              with ',' separated key value pairs. Currently, this string search term
     *                              only supported search from subject, from, to, bcc, and cc.
     *                              As an example: " subject:DAS , from:carbon , bcc:wso2 "
     *                              string search term create a
     *                              search term instance which filter emails contain 'DAS' in the subject,
     *                              'carbon' in the from address and 'wso2' in one of the bcc addresses.
     *                              It does sub string matching which is case insensitive.
     * @return EmailServer connector instance
     */
    public ServerConnector createConnector(String id, Map<String, String> emailProperties,
            String stringEmailSearchTerm) {
        return new EmailServerConnector(id, emailProperties, stringEmailSearchTerm);
    }
}
