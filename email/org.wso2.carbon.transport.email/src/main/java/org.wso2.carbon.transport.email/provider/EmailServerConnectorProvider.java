package org.wso2.carbon.transport.email.provider;

import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.ServerConnectorProvider;
import org.wso2.carbon.transport.email.receiver.EmailServerConnector;
import org.wso2.carbon.transport.email.utils.EmailConstants;
import java.util.List;
import java.util.Map;

import javax.mail.search.SearchTerm;

/**
 * Created by chathurika on 7/9/17.
 */
public class EmailServerConnectorProvider extends ServerConnectorProvider {
    public EmailServerConnectorProvider() {
        super(EmailConstants.PROTOCOL_EMAIL);
    }

    public EmailServerConnectorProvider(String protocol) {
        super(protocol);
    }

    @Override public List<ServerConnector> initializeConnectors() {
        return null;
    }

    @Override public ServerConnector createConnector(String id, Map<String, String> emailProperties) {
        return new EmailServerConnector(id, emailProperties);
    }

    public ServerConnector createConnector(String id, Map<String, String> emailProperties, SearchTerm emailSearchTerm) {
        return new EmailServerConnector(id, emailProperties, emailSearchTerm);
    }
}
