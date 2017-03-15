/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.connector.framework.server.polling;

import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.Map;

/**
 * Abstract class which should be extended when writing polling type of server connectors such as file, jms, etc.
 */
public abstract class PollingServerConnector extends ServerConnector {
    protected long interval = 1000L;  //default polling interval
    private PollingTaskRunner pollingTaskRunner;

    public PollingServerConnector(String id) {
        super(id);
    }

    /**
     * The start polling method which should be called when starting the polling with given interval.
     * @param parameters parameters passed from starting this polling connector.
     */
    @Override
    @Deprecated
    public void start(Map<String, String> parameters) throws ServerConnectorException {
        String pollingInterval = parameters.get(Constants.POLLING_INTERVAL);
        if (pollingInterval != null) {
            try {
                interval = Long.parseLong(pollingInterval);
            } catch (NumberFormatException e) {
                throw new ServerConnectorException("Could not parse parameter: " + Constants.POLLING_INTERVAL
                        + " to numeric type: Long", e);
            }
        }
        pollingTaskRunner = new PollingTaskRunner(this);
        pollingTaskRunner.start();
    }

    /**
     * The start polling method which should be called when starting the polling with given interval.
     * @throws ServerConnectorException if a error happen while starting the connector.
     */
    @Override
    public void start() throws ServerConnectorException {
        String pollingInterval = getProperties().get(POLLING_INTERVAL);
        if (pollingInterval != null) {
            this.interval = Long.parseLong(pollingInterval);
        }
        pollingTaskRunner = new PollingTaskRunner(this);
        pollingTaskRunner.start();
    }

    @Override
    public void stop() throws ServerConnectorException {
        if (pollingTaskRunner != null) {
            pollingTaskRunner.terminate();
        }
    }

    @Override
    protected void beginMaintenance() {
        if (pollingTaskRunner != null) {
            pollingTaskRunner.terminate();
        }
    }

    @Override
    protected void endMaintenance() {
        if (pollingTaskRunner != null) {
            pollingTaskRunner.start();
        }
    }

    /**
     * Generic polling method which will be invoked with each polling invocation.
     */
    public abstract void poll();


    public long getInterval() {
        return interval;
    }
}
