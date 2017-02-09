/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.transport.jms.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.jms.exception.JMSServerConnectorException;

/**
 * This class tries to connect to JMS provider until the maximum re-try count meets
 */
class JMSConnectionRetryHandler {
    private JMSServerConnector jmsServerConnector;
    private static final Log logger = LogFactory.getLog(JMSConnectionRetryHandler.class.getName());
    private long retryInteval;
    private int retryCount = 0;
    private int maxRetryCount;

    JMSConnectionRetryHandler(JMSServerConnector jmsServerConnector, long retryInterval, int maxRetryCount) {
        this.jmsServerConnector = jmsServerConnector;
        this.retryInteval = retryInterval;
        this.maxRetryCount = maxRetryCount;
    }

    public void run() throws JMSServerConnectorException {
        int minuteToMilli = 60000;
        try {
            Thread.sleep(retryInteval * minuteToMilli);
        } catch (InterruptedException e) {
            //Ignore the exception
        }
        while (retryCount < maxRetryCount) {
            try {
                retryCount++;
                jmsServerConnector.createMessageListener();
                logger.info("Connected to the message broker after retrying for " + retryCount + " time(s)");
                return;
            } catch (JMSServerConnectorException ex) {
                jmsServerConnector.closeAll();
                if (retryCount < maxRetryCount) {
                    logger.error("Retry connection attempt " + retryCount + " to JMS Provider failed. Retry will be "
                            + "attempted ");
                    retryInteval = retryInteval * 2;
                    try {
                        Thread.sleep(retryInteval * minuteToMilli);
                    } catch (InterruptedException e) {
                        // ignore the exception
                    }
                }
            }
        }
        throw new JMSServerConnectorException("Connection to the jms provider failed after retrying for " +
                retryCount + " times");

    }
}
