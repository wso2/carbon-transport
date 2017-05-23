/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.security;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.transport.http.netty.common.Util;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.testng.AssertJUnit.assertTrue;

public class HttpResponseSplittingTestCase {
    public static final String TEST_HEADER_NAME = "TestHeader";
    public static final String TEST_HEADER_VALID_VALUE = "TestHeaderValue";
    public static final String TEST_HEADER_ATTACK_VALUE = "TestHeaderValue\r\nInjectedHeader: InjectedValue";
    public static final String TEST_HEADER_ATTACK_SANITIZED_VALUE = "TestHeaderValue  InjectedHeader: InjectedValue";

    public static final String TEST_HEADER_VALID_NAME = "TestHeader";
    public static final String TEST_HEADER_ATTACK_NAME = "TestHeader: Value\r\nInjectedHeader: InjectedValue\r\nValue";
    public static final String TEST_HEADER_VALUE = "TestHeaderValue";
    public static final String TEST_HEADER_ATTACK_SANITIZED_NAME = "TestHeader: Value  InjectedHeader: " +
            "InjectedValue  Value";

    @Test
    public void headerValueValidationWithoutAttackTestCase() {
        // Directly testing utility methods since with test application there is no straight-forward way of
        // setting response headers based on request characteristics.
        HttpVersion httpVersion = new HttpVersion(HTTP_1_1.text(), true);
        int statusCode = 200;
        String reasonPhrase = HttpResponseStatus.valueOf(statusCode).reasonPhrase();
        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode, reasonPhrase);

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        Headers headers = new Headers();
        headers.set(TEST_HEADER_NAME, TEST_HEADER_VALID_VALUE);
        Util.setHeaders(outgoingResponse, headers);

        String testHeaderValue = outgoingResponse.headers().get(TEST_HEADER_NAME);
        assertTrue(testHeaderValue.equals(TEST_HEADER_VALID_VALUE));
    }

    @Test
    public void headerValueValidationWithAttackTestCase() {
        // Directly testing utility methods since with test application there is no straight-forward way of
        // setting response headers based on request characteristics.
        HttpVersion httpVersion = new HttpVersion(HTTP_1_1.text(), true);
        int statusCode = 200;
        String reasonPhrase = HttpResponseStatus.valueOf(statusCode).reasonPhrase();
        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode, reasonPhrase);

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        Headers headers = new Headers();
        headers.set(TEST_HEADER_NAME, TEST_HEADER_ATTACK_VALUE);
        Util.setHeaders(outgoingResponse, headers);

        String testHeaderValue = outgoingResponse.headers().get(TEST_HEADER_NAME);
        assertTrue(testHeaderValue.equals(TEST_HEADER_ATTACK_SANITIZED_VALUE));
    }

    @Test
    public void headerNameValidationWithoutAttackTestCase() {
        // Directly testing utility methods since with test application there is no straight-forward way of
        // setting response headers based on request characteristics.
        HttpVersion httpVersion = new HttpVersion(HTTP_1_1.text(), true);
        int statusCode = 200;
        String reasonPhrase = HttpResponseStatus.valueOf(statusCode).reasonPhrase();
        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode, reasonPhrase);

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        Headers headers = new Headers();
        headers.set(TEST_HEADER_VALID_NAME, TEST_HEADER_VALUE);
        Util.setHeaders(outgoingResponse, headers);

        String testHeaderValue = outgoingResponse.headers().get(TEST_HEADER_VALID_NAME);
        assertTrue(testHeaderValue.equals(TEST_HEADER_VALUE));
    }

    @Test
    public void headerNameValidationWithAttackTestCase() {
        // Directly testing utility methods since with test application there is no straight-forward way of
        // setting response headers based on request characteristics.
        HttpVersion httpVersion = new HttpVersion(HTTP_1_1.text(), true);
        int statusCode = 200;
        String reasonPhrase = HttpResponseStatus.valueOf(statusCode).reasonPhrase();
        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode, reasonPhrase);

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        Headers headers = new Headers();
        headers.set(TEST_HEADER_ATTACK_NAME, TEST_HEADER_VALUE);
        Util.setHeaders(outgoingResponse, headers);

        String testHeaderValue = outgoingResponse.headers().get(TEST_HEADER_ATTACK_SANITIZED_NAME);
        assertTrue(testHeaderValue.equals(TEST_HEADER_VALUE));
    }
}

