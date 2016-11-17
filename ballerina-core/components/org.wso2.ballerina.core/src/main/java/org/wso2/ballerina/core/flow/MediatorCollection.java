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

package org.wso2.ballerina.core.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * Place Holder for chain of mediators
 */
public class MediatorCollection {

    /* List of Mediators */
    private List<Mediator> mediators;

    public MediatorCollection() {
        mediators = new ArrayList<>();
    }

    /**
     * Get the first mediator in the collection
     *
     * @return first mediator of the collection
     */
    public Mediator getFirstMediator() {
        return mediators.get(0);
    }

    /**
     * Get all the mediators
     *
     * @return all the mediators
     */
    public List<Mediator> getMediators() {
        return mediators;
    }

    /**
     * Add a mediator to the collection
     *
     * @param mediator mediator to be added
     */
    public void addMediator(Mediator mediator) {
        int lastIndex = mediators.size() - 1;
        if (lastIndex >= 0) {
            mediators.get(lastIndex).setNext(mediator);
        }
        mediators.add(mediator);
    }
}
