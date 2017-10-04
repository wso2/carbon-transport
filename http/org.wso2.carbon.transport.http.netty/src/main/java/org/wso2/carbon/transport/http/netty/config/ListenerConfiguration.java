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
@Configuration(description = "HTTP Listener configuration")
public class ListenerConfiguration {

    public static final String DEFAULT_KEY = "default";

    public static ListenerConfiguration getDefault() {
        ListenerConfiguration defaultConfig;
        defaultConfig = new ListenerConfiguration(DEFAULT_KEY, "0.0.0.0", 8080);
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    @Element(description = "Id", required = true)
    private String id;

    @XmlAttribute
    @Element(description = "Host", required = true)
    private String host;

    @XmlAttribute(required = true)
    @Element(description = "Port", required = true)
    private int port;

    @XmlAttribute
    @Element(description = "Bind on startup")
    private boolean bindOnStartup = false;

    @XmlAttribute
    @Element(description = "Scheme")
    private String scheme = "http";

    @XmlAttribute
    @Element(description = "is HTTP2")
    private boolean http2 = false;

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
    @Element(description = "Message processor Id")
    private String messageProcessorId;

    @XmlAttribute
    @Element(description = "is HTTP trace logs enable")
    private boolean httpTraceLogEnabled;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    @Element(description = "Additional parameters")
    private List<Parameter> parameters = getDefaultParameters();

    private RequestSizeValidationConfiguration requestSizeValidationConfig;

    public ListenerConfiguration() {
    }

    public ListenerConfiguration(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
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

    public boolean isHttpTraceLogEnabled() {
        return httpTraceLogEnabled;
    }

    public void setHttpTraceLogEnabled(boolean httpTraceLogEnabled) {
        this.httpTraceLogEnabled = httpTraceLogEnabled;
    }

    public RequestSizeValidationConfiguration getRequestSizeValidationConfig() {
        return requestSizeValidationConfig;
    }

    public void setRequestSizeValidationConfig(RequestSizeValidationConfiguration requestSizeValidationConfig) {
        this.requestSizeValidationConfig = requestSizeValidationConfig;
    }
}
