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
package org.wso2.carbon.transport.jms.sender;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Message Utils for JMS.
 */

public class JMSMessageUtils {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String TEXT_PLAIN = "text/plain";

 /*   public static StorableMessage toStorableMessage(CarbonMessage message) {
        StorableMessage storableMessage = new StorableMessage();

        List<Header> headers = message.getHeaders().getAll();
        HashMap<String, String> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        storableMessage.setHeaders(headerMap);
        if (headerMap.containsKey(CONTENT_TYPE)) {
            if (headerMap.get(CONTENT_TYPE).equals(APPLICATION_JSON)) {
                BJSON result = null;
                try {

                    if (message.isAlreadyRead()) {
                        BValue payload = message.getBuiltPayload();
                        if (payload instanceof BJSON) {
                            // if the payload is already JSON, return it as it is.
                            result = (BJSON) message.getBuiltPayload();
                        } else {
                            // else, build the JSON from the string representation of the payload.
                            result = new BJSON(message.getBuiltPayload().stringValue());
                        }

                    } else {
                        result = new BJSON(message.value().getInputStream());
                        message.setBuiltPayload(result);
                        message.setAlreadyRead(true);
                    }
                    storableMessage.setPayload(result.stringValue());
                } catch (JsonSyntaxException e) {
                    ErrorHandler.handleMalformedJson(OPERATION, e);
                } catch (JsonParseException e) {
                    ErrorHandler.handleJsonException(OPERATION, e);
                } catch (Throwable e) {
                    ErrorHandler.handleJsonException(OPERATION, e);
                }
            } else if (headerMap.get(CONTENT_TYPE).equals(APPLICATION_XML)) {
                BXML result = null;
                try {

                    if (message.isAlreadyRead()) {
                        BValue payload = message.getBuiltPayload();
                        if (payload instanceof BXML) {
                            // if the payload is already xml, return it as it is.
                            result = (BXML) message.getBuiltPayload();
                        } else {
                            // else, build the xml from the string representation of the payload.
                            result = new BXML(message.getBuiltPayload().stringValue());
                        }
                    } else {
                        result = new BXML(message.value().getInputStream());
                        message.setBuiltPayload(result);
                        message.setAlreadyRead(true);
                    }
                    storableMessage.setPayload(result.stringValue());
                } catch (Throwable e) {
                    ErrorHandler.handleJsonException(OPERATION, e);
                }
            } else if (headerMap.get(CONTENT_TYPE).equals(TEXT_PLAIN)) {
                BString result;
                try {
                    if (message.isAlreadyRead()) {
                        result = new BString(message.getBuiltPayload().stringValue());
                    } else {
                        String payload = MessageUtils
                                .getStringFromInputStream(message.value().getInputStream());
                        result = new BString(payload);
                        message.setBuiltPayload(result);
                        message.setAlreadyRead(true);
                    }
                    storableMessage.setPayload(result.stringValue());
                } catch (Throwable e) {
                    throw new BallerinaException(
                            "Error while retrieving string payload from message: " +
                            e.getMessage());
                }
            }
        }

        return storableMessage;
    }

    public static BMessage toBallerinaMessage(StorableMessage storableMessage) {
        BMessage bMessage = new BMessage();
        HashMap<String, String> headerMap = storableMessage.getHeaders();
        Iterator iterator = headerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            bMessage.getHeaders()
                    .add(new Header((String) entry.getKey(), (String) entry.getValue()));
        }
        if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_JSON)) {
            BJSON payload = new BJSON(storableMessage.getPayload());
            bMessage.setBuiltPayload(payload);
        } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_XML)) {
            BXML payload = new BXML(storableMessage.getPayload());
            bMessage.setBuiltPayload(payload);
        } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.TEXT_PLAIN)) {
            BString payload = new BString(storableMessage.getPayload());
            bMessage.setBuiltPayload(payload);
        }
        return bMessage;
    }*/

    public static void setTransportHeaders(Message message, Map<String, Object> headerMap) {
        try {
            if (headerMap != null) {
                Iterator iterator = headerMap.keySet().iterator();

                while (true) {
                    String name;
                    do {
                        if (!iterator.hasNext()) {
                            return;
                        }

                        Object headerName = iterator.next();
                        name = (String) headerName;
                    } while (name.startsWith("JMSX") && !name.equals("JMSXGroupID") &&
                             !name.equals("JMSXGroupSeq"));

                    if ("JMS_COORELATION_ID".equals(name)) {
                        message.setJMSCorrelationID((String) headerMap.get("JMS_COORELATION_ID"));
                    } else {
                        Object value;
                        if ("JMS_DELIVERY_MODE".equals(name)) {
                            value = headerMap.get("JMS_DELIVERY_MODE");
                            if (value instanceof Integer) {
                                message.setJMSDeliveryMode(((Integer) value).intValue());
                            } else if (value instanceof String) {
                                try {
                                    message.setJMSDeliveryMode(Integer.parseInt((String) value));
                                } catch (NumberFormatException var8) {

                                }
                            } else {

                            }
                        } else if ("JMS_EXPIRATION".equals(name)) {
                            message.setJMSExpiration(
                                    Long.parseLong((String) headerMap.get("JMS_EXPIRATION")));
                        } else if ("JMS_MESSAGE_ID".equals(name)) {
                            message.setJMSMessageID((String) headerMap.get("JMS_MESSAGE_ID"));
                        } else if ("JMS_PRIORITY".equals(name)) {
                            message.setJMSPriority(
                                    Integer.parseInt((String) headerMap.get("JMS_PRIORITY")));
                        } else if ("JMS_TIMESTAMP".equals(name)) {
                            message.setJMSTimestamp(
                                    Long.parseLong((String) headerMap.get("JMS_TIMESTAMP")));
                        } else if ("JMS_MESSAGE_TYPE".equals(name)) {
                            message.setJMSType((String) headerMap.get("JMS_MESSAGE_TYPE"));
                        } else {
                            value = headerMap.get(name);
                            if (value instanceof String) {
                                message.setStringProperty(name, (String) value);
                            } else if (value instanceof Boolean) {
                                message.setBooleanProperty(name, ((Boolean) value).booleanValue());
                            } else if (value instanceof Integer) {
                                message.setIntProperty(name, ((Integer) value).intValue());
                            } else if (value instanceof Long) {
                                message.setLongProperty(name, ((Long) value).longValue());
                            } else if (value instanceof Double) {
                                message.setDoubleProperty(name, ((Double) value).doubleValue());
                            } else if (value instanceof Float) {
                                message.setFloatProperty(name, ((Float) value).floatValue());
                            }
                        }
                    }
                }
            }
        } catch (JMSException exception) {

        }
    }

    /**
     * Convert input stream to String.
     *
     * @param in Message payload as an input stream
     * @return Message payload as string
     */
    public static String getStringFromInputStream(InputStream in) {
        StringBuilder sb = new StringBuilder(4096);
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
        } catch (IOException ioe) {
            try {
                throw new Exception(ioe.getMessage(), ioe);
            } catch (Exception exception) {
                //Do Nothing
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // Do nothing.
            }
            try {
                reader.close();
            } catch (IOException e) {
                // Do nothing.
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // Do nothing.
            }
        }
        return sb.toString();
    }
}
