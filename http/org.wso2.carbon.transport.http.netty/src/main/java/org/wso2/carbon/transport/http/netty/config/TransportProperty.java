package org.wso2.carbon.transport.http.netty.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * represents transport property.
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
@Configuration(description = "Transport Property")
public class TransportProperty {

    @XmlAttribute
    @Element(description = "Property name")
    protected String name;

    @XmlValue
    @Element(description = "Property value")
    protected Object value;

    public static TransportProperty getDefault() {
        return new TransportProperty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
