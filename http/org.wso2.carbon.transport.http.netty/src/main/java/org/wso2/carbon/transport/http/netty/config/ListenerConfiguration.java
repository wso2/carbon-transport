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

import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * JAXB representation of a transport listener.
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListenerConfiguration {

    public static final String DEFAULT_KEY = "default";
    public static final String DEFAULT_SCHEME = "https";
    public static final String DEFAULT_HTTP_STRICT_TRANSPORT_SECURITY_HEADER_VALUE = "max-age=15768000;" +
            " includeSubDomains";
    public static final String DEFAULT_PUBLIC_KEY_PINS_HEADER_VALUE =
            "pin-sha256='2pCcYrG90hDFxwOCsVya7wpbQjqhBy3OPsFyyT+7108='; max-age=15766000; includeSubDomains";
    /*The key used here is the public key of the default wso2carbon certificate. It is imported as a default value
    when running TransportSecurityHeadersTestCase*/
    public static ListenerConfiguration getDefault() {
        ListenerConfiguration defaultConfig;
        defaultConfig = new ListenerConfiguration(DEFAULT_KEY, "0.0.0.0", 8080, DEFAULT_SCHEME,
                DEFAULT_HTTP_STRICT_TRANSPORT_SECURITY_HEADER_VALUE, DEFAULT_PUBLIC_KEY_PINS_HEADER_VALUE);
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute
    private String host;

    @XmlAttribute(required = true)
    private int port;

    @XmlAttribute
    private boolean bindOnStartup = false;

    @XmlAttribute
    private String scheme = "http";

    @XmlAttribute
    private boolean http2 = false;

    @XmlAttribute
    private String keyStoreFile;

    @XmlAttribute
    private String keyStorePass;

    @XmlAttribute
    private String trustStoreFile;

    @XmlAttribute
    private String trustStorePass;

    @XmlAttribute
    private String certPass;

    @XmlAttribute
    private int socketIdleTimeout;

    @XmlAttribute
    private String messageProcessorId;

    @XmlAttribute
    private String strictTransportSecurityHeader;

    @XmlAttribute
    private String publicKeyPinsHeader;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private List<Parameter> parameters = getDefaultParameters();

    public ListenerConfiguration() {
    }

    public ListenerConfiguration(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public ListenerConfiguration(String id, String host, int port, String scheme, String hstsHeader,
                                 String hpkpHeader) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        this.strictTransportSecurityHeader = hstsHeader;
        this.publicKeyPinsHeader = hpkpHeader;
    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isBindOnStartup() {
        return bindOnStartup;
    }

    public void setBindOnStartup(boolean bindOnStartup) {
        this.bindOnStartup = bindOnStartup;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public boolean isHttp2() {
        return http2;
    }

    public void setHttp2(boolean http2) {
        this.http2 = http2;
    }

    public String getStrictTransportSecurityHeader() {
        return strictTransportSecurityHeader;
    }

    public void setStrictTransportSecurityHeader(String strictTransportSecurityHeader) {
        this.strictTransportSecurityHeader = strictTransportSecurityHeader;
    }

    public String getPublicKeyPinsHeader() {
        return publicKeyPinsHeader;
    }

    public void setPublicKeyPinsHeader(String publicKeyPinsHeader) {
        this.publicKeyPinsHeader = publicKeyPinsHeader;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public SSLConfig getSslConfig() {
        if (scheme == null || !scheme.equalsIgnoreCase("https")) {
            return null;
        }

        return Util.getSSLConfigForListener(certPass, keyStorePass, keyStoreFile, trustStoreFile, trustStorePass,
                parameters);
    }

    private List<Parameter> getDefaultParameters() {
        List<Parameter> defaultParams = new ArrayList<>();
        return defaultParams;

    }

    public int getSocketIdleTimeout(int defaultVal) {
        if (socketIdleTimeout == 0) {
            return defaultVal;
        }
        return socketIdleTimeout;
    }

    public String getMessageProcessorId() {
        return messageProcessorId;
    }

    public void setMessageProcessorId(String messageProcessorId) {
        this.messageProcessorId = messageProcessorId;
    }

    public void setSocketIdleTimeout(int socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
    }
}
