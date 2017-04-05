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
 *
 */

package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.messaging.AbstractCarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

import javax.websocket.Session;

/**
 * This is the callback for WebSocket which contains nothing but is used to contain any property needed for the
 * callback.
 */
public class WebSocketCallback extends AbstractCarbonCallback {

    public WebSocketCallback(Session session) {
        this.setProperty(Constants.WEBSOCKET_SESSION, session);
    }

    @Override
    public void done(CarbonMessage cMsg) {
        throw new UnsupportedOperationException("This method is not supported for WebSocket");
    }
}
