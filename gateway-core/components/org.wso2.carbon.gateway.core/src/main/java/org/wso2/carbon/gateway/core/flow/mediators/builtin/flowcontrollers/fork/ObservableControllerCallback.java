/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.fork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.FlowControllerCallback;
import org.wso2.carbon.gateway.core.flow.RxContext;
import org.wso2.carbon.messaging.CarbonMessage;
import rx.Observable;
import rx.subjects.AsyncSubject;

import java.util.UUID;

/**
 * Callback that will notify Join, that worker flow has completed
 */
public class ObservableControllerCallback implements FlowControllerCallback {

    private static final Logger log = LoggerFactory.getLogger(ObservableControllerCallback.class);
    private AsyncSubject<RxContext> subject;
    private UUID uuid;
    private RxContext rxContext;
    private String name;

    public ObservableControllerCallback(String workerName, UUID uuid) {
        this.subject = AsyncSubject.create();
        this.name = workerName;
        this.uuid = uuid;
        this.rxContext = null;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {

        //The worker flow execution is completed, so now, notify all the subscribers
        if (log.isDebugEnabled()) {
            log.debug("[Worker : " + name + "] Worker flow completed");
        }

        rxContext = new RxContext(uuid.toString(), carbonMessage, name);
        subject.onNext(rxContext);
        subject.onCompleted();

    }

    @Override
    public boolean canProcess(CarbonMessage carbonMessage) {
        return !carbonMessage.isFaulty();
    }

    public Observable<RxContext> getObservable() {
        return subject.asObservable();
    }


    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
