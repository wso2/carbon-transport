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

package org.wso2.carbon.transport.http.netty.multipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.carbon.transport.http.netty.message.multipart.HttpBodyPart;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listen for multipart request data.
 */
public class MultipartConnectionListener implements HttpConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(MultipartConnectionListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<HttpBodyPart> multiparts;
    private CountDownLatch latch;

    @Override
    public void onMessage(HTTPCarbonMessage httpMessage) {
        executor.execute(() -> {
            try {
                latch = new CountDownLatch(1);
                InputStream inputStream = new HttpMessageDataStreamer(httpMessage).getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                multiparts = (List<HttpBodyPart>) objectInputStream.readObject();
                latch.countDown();
            } catch (IOException e) {
                logger.error("IOException occurred during multipart message retrieval ", e);
            } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException occurred during multipart message retrieval ", e);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }

    /**
     * Get multipart bodies.
     *
     * @return List<HttpBodyPart>
     */
    public List<HttpBodyPart> getMultiparts() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("InterruptedException occurred while waiting for multiparts to receive ", e);
        }
        return multiparts;
    }
}
