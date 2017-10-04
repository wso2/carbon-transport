/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.transport.http.netty.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;


/**
 * JAXB representation of the Netty transport sender configuration.
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
@Configuration(description = "HTTP Sender configuration")
public class SenderConfiguration {

    public static final String DEFAULT_KEY = "netty";


    public static SenderConfiguration getDefault() {
        SenderConfiguration defaultConfig;
        defaultConfig = new SenderConfiguration(DEFAULT_KEY);
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    @Element(description = "Id", required = true)
    private String id;

    @XmlAttribute
    @Element(description = "Scheme")
    private String scheme = "http";

    @XmlAttribute
    @Element(description = "Keystore file path")
    private String keyStoreFile;

    @XmlAttribute
    @Element(description = "Keystore password")
    private String keyStorePass;

    @XmlAttribute
    @Element(description = "Truststore file path")
    private String trustStoreFile;

    @XmlAttribute
    @Element(description = "Truststore password")
    private String trustStorePass;

    @XmlAttribute
    @Element(description = "Certificate password")
    private String certPass;

    @XmlAttribute
    @Element(description = "Socket idle timeout")
    private int socketIdleTimeout;

    @XmlAttribute
    @Element(description = "is HTTP trace logs enable")
    private boolean httpTraceLogEnabled;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    @Element(description = "Additional parameters")
    private List<Parameter> parameters;

    private boolean followRedirect;

    private int maxRedirectCount;

    public SenderConfiguration() {
    }

    public SenderConfiguration(String id) {
        this.id = id;

    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }

    public SSLConfig getSslConfig() {
        if (scheme == null || !scheme.equalsIgnoreCase("https")) {
            return null;
        }
        return Util.getSSLConfigForSender(certPass, keyStorePass, keyStoreFile, trustStoreFile, trustStorePass,
                parameters);
    }

    public int getSocketIdleTimeout(int defaultValue) {
        if (socketIdleTimeout == 0) {
            return defaultValue;
        }
        return socketIdleTimeout;
    }

    public void setSocketIdleTimeout(int socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
    }

    public boolean isHttpTraceLogEnabled() {
        return httpTraceLogEnabled;
    }

    public void setHttpTraceLogEnabled(boolean httpTraceLogEnabled) {
        this.httpTraceLogEnabled = httpTraceLogEnabled;
    }

    public boolean isFollowRedirect() {
        return followRedirect;
    }

    public void setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
    }

    public int getMaxRedirectCount() {
        return maxRedirectCount;
    }

    public void setMaxRedirectCount(int maxRedirectCount) {
        this.maxRedirectCount = maxRedirectCount;
    }
}
