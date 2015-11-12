/*
 * Copyright 2014, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaazing.k3po.driver.internal.logging;

import java.util.Queue;

import org.jboss.netty.logging.AbstractInternalLogger;
import org.jboss.netty.logging.InternalLogger;

class BufferedLogger extends AbstractInternalLogger {

    private final InternalLogger logger;
    private final Queue<Runnable> messages;

    public BufferedLogger(InternalLogger logger, Queue<Runnable> messages) {
        this.logger = logger;
        this.messages = messages;
    }

    public void debug(String msg) {
        messages.add(() -> logger.debug(msg));
    }

    public void debug(String msg, Throwable cause) {
        messages.add(() -> logger.debug(msg, cause));
    }

    public void error(String msg) {
        messages.add(() -> logger.error(msg));
    }

    public void error(String msg, Throwable cause) {
        messages.add(() -> logger.error(msg, cause));
    }

    public void info(String msg) {
        messages.add(() -> logger.info(msg));
    }

    public void info(String msg, Throwable cause) {
        messages.add(() -> logger.info(msg, cause));
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void warn(String msg) {
        messages.add(() -> logger.warn(msg));
    }

    public void warn(String msg, Throwable cause) {
        messages.add(() -> logger.warn(msg, cause));
    }
}
