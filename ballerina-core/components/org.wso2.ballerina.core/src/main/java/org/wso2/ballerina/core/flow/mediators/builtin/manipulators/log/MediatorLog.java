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

package org.wso2.ballerina.core.flow.mediators.builtin.manipulators.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.wso2.ballerina.core.Constants;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * A Representation of Logger.
 */
public class MediatorLog {
    private static final Logger traceLog = LoggerFactory.getLogger(Constants.TRACE_LOGGER);
    Marker fatal = MarkerFactory.getMarker("FATAL");

    private final Logger defaultLog;
    private final boolean traceOn;
  //  private final CarbonMessage message;

    // The definition of this constructor might change...
    public MediatorLog(Logger defaultLog, boolean traceOn, CarbonMessage message) {
        this.defaultLog = defaultLog;
        this.traceOn = traceOn;
      //  this.message = message;
    }

    public boolean isTraceOrDebugEnabled() {
        return isTraceEnabled() || isDebugEnabled();
    }

    public boolean isDebugEnabled() {
        return defaultLog.isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return traceOn;
    }

    /**
     * Log a message to the default log at level DEBUG and and to the trace log
     * at level INFO if trace is enabled for the mediator.
     */
    public void traceOrDebug(String msg) {
        if (traceOn) {
            traceLog.info(msg);
        }
        defaultLog.debug(msg);
    }

    /**
     * Log a message at level WARN to the default log, if level DEBUG is enabled,
     * and to the trace log, if trace is enabled for the mediator.
     */
    public void traceOrDebugWarn(String msg) {
        if (traceOn) {
            traceLog.warn(msg);
        }
        if (defaultLog.isDebugEnabled()) {
            defaultLog.warn(msg);
        }
    }

    public boolean isTraceTraceEnabled() {
        return traceOn && traceLog.isTraceEnabled();
    }

    /**
     * Log a message to the trace log at level TRACE if trace is enabled for the mediator.
     */
    public void traceTrace(String msg) {
        if (traceOn) {
            traceLog.trace(msg);
        }
    }

    /**
     * Log a message at level INFO to all available/enabled logs.
     */
    public void auditLog(String msg) {
        defaultLog.info(msg);
        if (traceOn) {
            traceLog.info(msg);
        }
    }

    /**
     * Log a message at level DEBUG to all available/enabled logs.
     */
    public void auditDebug(String msg) {
        defaultLog.debug(msg);
        if (traceOn) {
            traceLog.debug(msg);
        }
    }

    /**
     * Log a message at level TRACE to all available/enabled logs.
     */
    public void auditTrace(String msg) {
        defaultLog.trace(msg);
        if (traceOn) {
            traceLog.trace(msg);
        }
    }

    /**
     * Log a message at level WARN to all available/enabled logs.
     */
    public void auditWarn(String msg) {
        defaultLog.warn(msg);
        if (traceOn) {
            traceLog.warn(msg);
        }
    }

    /**
     * Log a message at level ERROR to all available/enabled logs.
     */
    public void auditError(String msg) {
        defaultLog.error(msg);
        if (traceOn) {
            traceLog.error(msg);
        }
    }

    /**
     * Log a message at level FATAL to all available/enabled logs.
     */
    public void auditFatal(String msg) {
        defaultLog.error(fatal, msg);
        if (traceOn) {
            traceLog.error(fatal, msg);
        }
    }

    /**
     * Log a message at level ERROR to the default log and to the trace, if trace is enabled.
     */
    public void error(String msg) {
        defaultLog.error(msg);
        if (traceOn) {
            traceLog.error(msg);
        }
    }
}
