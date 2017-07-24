package org.wso2.carbon.transport.email.config.generator.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.transport.email.config.generator.EmailTransportConfiguration;

/**
 * Created by chathurika on 7/20/17.
 */
public class ConfigurationServiceComponent {
    private static final Logger logger = LoggerFactory
            .getLogger(org.wso2.carbon.transport.email.config.generator.internal.ConfigurationServiceComponent.class);
    private ConfigProvider configProvider;

    @Activate
    public void start() throws ConfigurationException {
        EmailTransportConfiguration emailTransportConfiguration = configProvider
                .getConfigurationObject(EmailTransportConfiguration.class);
        logger.info("Email Transport Configuration - {}", emailTransportConfiguration);
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
    }
}
