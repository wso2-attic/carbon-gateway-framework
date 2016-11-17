package org.wso2.ballerina.core.flow.mediators.builtin.manipulators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.config.ParameterHolder;
import org.wso2.ballerina.core.flow.AbstractMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
 * Sleep Mediator
 */
public class SleepMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(SleepMediator.class);

    private Long time;


    public SleepMediator() {
    }

    public SleepMediator(long time) {
        this.time = time;
    }

    @Override
    public String getName() {
        return "sleep";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        log.info("Sleeping for " + time + " miliseconds.");
        Thread.sleep(time);

        return next(carbonMessage, carbonCallback);
    }

    public void setParameters(ParameterHolder parameterHolder) {
        time = Long.valueOf(parameterHolder.getParameter("time").getValue());
    }
}
