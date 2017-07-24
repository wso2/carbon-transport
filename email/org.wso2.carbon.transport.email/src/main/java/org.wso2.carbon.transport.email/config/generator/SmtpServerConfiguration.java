package org.wso2.carbon.transport.email.config.generator;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Locale;

/**
 * Created by chathurika on 7/20/17.
 */

@Configuration(description = "SMTP Server Configurations")
public class SmtpServerConfiguration {
    @Element(description = "name of the parameter")
    private String name;

    @Element(description = "value of the parameter")
    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "name : %s, value : %s",
                name, value);
    }
}
