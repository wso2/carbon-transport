/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.security;

import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.wso2.carbon.transport.http.netty.common.Constants.HTTP_PUBLIC_KEY_PINS_HEADER;
import static org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STRICT_TRANSPORT_SECURITY_HEADER;

/**
 * A test class for transport security headers
 */
public class TransportSecurityHeadersTestCase {

    private List<HTTPServerConnector> serverConnectors;

    private static final String hstsTestValue = "max-age=15768000; includeSubDomains";

    private static final String hpkpTestValue = "pin-sha256='2pCcYrG90hDFxwOCsVya7wpbQjqhBy3OPsFyyT+7108=';" +
            " max-age=15766000; includeSubDomains";
    //the key used here is the public key of the default wso2carbon certificate.
    private HTTPServer httpServer;

    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8490));

    private CarbonMessageProcessor carbonMessageProcessor;

    @BeforeClass
    public void setUp() {
        TransportsConfiguration configuration = ConfigurationBuilder.getInstance()
                .getConfiguration("src/test/resources/simple-test-config/netty-transports.yml");
        carbonMessageProcessor = new TransportSecurityHeadersProcessor();
        serverConnectors = TestUtil.startConnectors(configuration, carbonMessageProcessor);
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT);
    }

    @Test
    public void hstsHeaderTest() {
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.GET.name(), true);
            String hstsHeader = urlConn.getHeaderField(HTTP_STRICT_TRANSPORT_SECURITY_HEADER);
            assertEquals(hstsTestValue, hstsHeader);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running hstsHeaderTest", e);
        }
    }

    @Test
    public void hpkpHeaderTest() {
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.GET.name(), true);
            String hpkpHeader = urlConn.getHeaderField(HTTP_PUBLIC_KEY_PINS_HEADER);
            assertEquals(hpkpTestValue, hpkpHeader);
            urlConn.disconnect();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running hpkpHeaderTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(serverConnectors, httpServer);
        TestUtil.removeMessageProcessor(carbonMessageProcessor);
    }
}
