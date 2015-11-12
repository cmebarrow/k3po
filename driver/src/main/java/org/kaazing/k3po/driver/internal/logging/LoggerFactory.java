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
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * This class augments the features of InternalLoggerFactory by wrapping any loggers with debug enabled
 * supplied by InternalLoggerFactory in a decorator which will keep log messages in memory and only
 * actually log them if finish is called with a future that indicates test failure. This allows us
 * to print out log messages only in the case of test failures, which is useful to avoid swamping the
 * build output with unneeded debug messages.
 */
public final class LoggerFactory {

    private static Queue<Runnable> messages = new LinkedBlockingQueue<Runnable>();

    public static InternalLogger getInstance(Class<?> class1) {
        InternalLogger result = InternalLoggerFactory.getInstance(class1);
        return result.isDebugEnabled() ? new BufferedLogger(result, messages) : result;
    }

    public static InternalLogger getInstance(String name) {
        InternalLogger result = InternalLoggerFactory.getInstance(name);
        return result.isDebugEnabled() ? new BufferedLogger(result, messages) : result;
    }

    public static void finish(final ChannelFuture future) {
        if (future.getCause() != null) {
            // LATER, once we move to checkstyle 2.17 which supports jdk 1.8 syntax:
            // messages.forEach(m -> m.run());
            for (Runnable m : messages) {
                m.run();
            }
        }
        messages.clear();
    }

    private LoggerFactory() {
        // prevent instantiation
    };
}
