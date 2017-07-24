package org.wso2.carbon.transport.email.internal.config.provider;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Locale;

/**
 * Created by chathurika on 7/19/17.
 */

@Configuration(description = "Child configuration")
public class ChildConfiguration {
    @Element(description = "A boolean field")
    private boolean isEnabled = false;

    @Element(description = "A string field")
    private String destination = "destination-name";

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "destination : %s, isEnabled : %s",
                destination, isEnabled);
    }
}
