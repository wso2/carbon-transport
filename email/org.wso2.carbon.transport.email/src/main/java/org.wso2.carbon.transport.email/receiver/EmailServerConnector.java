package org.wso2.carbon.transport.email.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.connector.framework.server.polling.PollingServerConnector;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.email.exception.EmailServerConnectorException;

import java.util.Map;
import javax.mail.search.SearchTerm;
/**
 * Created by chathurika on 7/9/17.
 */
public class EmailServerConnector extends PollingServerConnector {
    private static final Logger log = LoggerFactory.getLogger(EmailServerConnector.class);
    private SearchTerm emailSearchTerm = null;
    private CarbonMessageProcessor emailMessageProcessor;
    private static final long EMAIL_CONNECTOR_DEFAULT_INTERVAL = 10000L;
    private EmailConsumer emailConsumer = null;

    public EmailServerConnector(String id, Map<String, String> properties) {
        super(id, properties);
        interval = EMAIL_CONNECTOR_DEFAULT_INTERVAL;
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
            log.error(" Exit from the poll due to :" + e.toString());
        }
    }

    @Override protected void destroy() throws ServerConnectorException {

    }

    ////////////

    @Override public void stop() throws ServerConnectorException {

    }
}

