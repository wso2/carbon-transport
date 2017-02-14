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

package org.wso2.carbon.transport.jms.callback;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Call back used for transacted sessions. To commit or rollback the sessions.
 */
public class TransactedSessionCallback implements CarbonCallback {
    /**
     * The {@link Session} instance representing JMS Session related with this call back
     */
    private Session session;

    /**
     * Creates a call back for the transacted session.
     *
     * @param session JMS Session connected with this callback
     */
    public TransactedSessionCallback(Session session) {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void done(CarbonMessage carbonMessage) {
        if (carbonMessage.getProperty(JMSConstants.JMS_MESSAGE_DELIVERY_STATUS)
                .equals(JMSConstants.JMS_MESSAGE_DELIVERY_SUCCESS)) {
            try {
                session.commit();
            } catch (JMSException e) {
                throw new RuntimeException("Error while committing the session. ", e);
            }
        } else {
            try {
                session.rollback();
            } catch (JMSException e) {
                throw new RuntimeException("Error while rolling back the session. ", e);
            }
        }
    }
}
