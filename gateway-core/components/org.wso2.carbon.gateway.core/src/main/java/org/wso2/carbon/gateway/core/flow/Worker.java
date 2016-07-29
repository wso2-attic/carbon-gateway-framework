/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.core.flow;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A Class representing a collection of Mediators which execute mediators in a new thread of execution.
 */
public class Worker {

    // TODO make executor configurable
    public static final ExecutorService WORKER_EXECUTOR_SERVICE = Executors.newFixedThreadPool(200);

    private String name;

    /* Mediator collection */
    MediatorCollection mediators;

    /* Error handling mediator collection */
    MediatorCollection errorHandlerMediators;

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    public Worker(String name) {
        this.name = name;
        this.mediators = new MediatorCollection();
    }

    public Worker(String name, MediatorCollection mediators) {
        this.mediators = mediators;
        this.name = name;
    }

    public Observable submit(UUID id, CarbonMessage cMsg, CarbonCallback cCallback) {

        RxContext rxContext = new RxContext(id.toString(), cMsg, cCallback);
        BehaviorSubject<RxContext> behaviorSubject = BehaviorSubject.create(rxContext);

        Future f = WORKER_EXECUTOR_SERVICE.submit(() -> {
            try {
                log.info("Thread using carbon message with UUID " + id);
                mediators.getFirstMediator().receive(cMsg, cCallback);
                log.debug("mediator receive returned");
                behaviorSubject.onNext(rxContext);
            } catch (Exception e) {
                log.error("Error while mediating", e);
                //behaviorSubject.onNext(rxContext);
            }
        });

        f.isDone();
        return behaviorSubject;
    }

    public void addMediator(Mediator mediator) {
        mediators.addMediator(mediator);
    }

    public String getName() {
        return name;
    }

    public MediatorCollection getMediators() {
        return mediators;
    }
}
