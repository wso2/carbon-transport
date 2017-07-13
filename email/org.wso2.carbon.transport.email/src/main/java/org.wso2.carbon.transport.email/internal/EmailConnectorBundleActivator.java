package org.wso2.carbon.transport.email.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.messaging.ServerConnectorProvider;
import org.wso2.carbon.transport.email.provider.EmailServerConnectorProvider;

/**
 * Created by chathurika on 7/9/17.
 */
public class EmailConnectorBundleActivator implements BundleActivator {
    @Override public void start(BundleContext bundleContext) throws Exception {
        bundleContext
                .registerService(ServerConnectorProvider.class.toString(), new EmailServerConnectorProvider(), null);
    }

    @Override public void stop(BundleContext bundleContext) throws Exception {

    }
}
