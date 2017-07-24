package org.wso2.carbon.transport.email.internal.config.provider;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;

import java.util.Locale;

/**
 * Created by chathurika on 7/19/17.
 */

@Configuration(namespace = "wso2.configuration", description = "Parent configuration")
public class ParentConfiguration {
    @Element(description = "An example element for this configuration")
    private String name = "WSO2";

    @Element(description = "Another example element in the config", required = true)
    private int value = 10;

    // This value will not be visible in the configuration
    @Ignore
    private String ignored = "Ignored String";

    @Element(description = "Second level configuration")
    private ChildConfiguration childConfiguration = new ChildConfiguration();

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getIgnored() {
        return ignored;
    }

    public ChildConfiguration getChildConfiguration() {
        return childConfiguration;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "name : %s, value : %s, childConfiguration - %s",
                name, value, childConfiguration);
    }
}
