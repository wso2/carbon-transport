package org.wso2.carbon.transport.email.internal.config.provider;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

/**
 * Created by chathurika on 7/19/17.
 */
public class ConfigurationServiceComponent {
    private static final Logger logger = LoggerFactory
            .getLogger(ConfigurationServiceComponent.class);
    private ConfigProvider configProvider;

    @Activate
    public void start() throws ConfigurationException {
        ParentConfiguration parentConfiguration = configProvider
                .getConfigurationObject(ParentConfiguration.class);
        logger.info("Parent configuration - {}", parentConfiguration);
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
