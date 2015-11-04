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

package org.kaazing.k3po.driver.internal.netty.bootstrap;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineException;
import org.jboss.netty.channel.ChannelSink;

public class ChannelSinkDecorator implements ChannelSink {
    private final ChannelSink delegate;

    public ChannelSinkDecorator(ChannelSink delegate) {
        this.delegate = delegate;
    }

    @Override
    public void eventSunk(ChannelPipeline pipeline, ChannelEvent e) throws Exception {
        delegate.eventSunk(pipeline, e);
    }

    @Override
    public void exceptionCaught(ChannelPipeline pipeline, ChannelEvent e, ChannelPipelineException cause)
            throws Exception {
        delegate.exceptionCaught(pipeline, e, cause);
    }

    @Override
    public ChannelFuture execute(ChannelPipeline pipeline, Runnable task) {
        return delegate.execute(pipeline, task);
    }

}
