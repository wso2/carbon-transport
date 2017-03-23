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

package org.wso2.carbon.transport.http.netty.internal.websocket;

import org.wso2.carbon.messaging.AbstractCarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * The callback for the for WebSocket Connection. The done method is not used here.
 * This callback is basically used to transfer some callback properties to application layer.
 */
public class WebSocketCallBack extends AbstractCarbonCallback {
    @Override
    public void done(CarbonMessage cMsg) {
        throw new UnsupportedOperationException("This method is not supported for WebSocket Callback");
    }
}
