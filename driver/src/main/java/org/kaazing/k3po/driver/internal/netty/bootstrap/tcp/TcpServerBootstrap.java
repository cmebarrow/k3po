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

package org.kaazing.k3po.driver.internal.netty.bootstrap.tcp;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelSink;
import org.jboss.netty.channel.socket.ChannelRunnableWrapper;
import org.jboss.netty.channel.socket.nio.NioWorker;
import org.kaazing.k3po.driver.internal.netty.bootstrap.ChannelPipelineDecorator;
import org.kaazing.k3po.driver.internal.netty.bootstrap.ChannelSinkDecorator;
import org.kaazing.k3po.driver.internal.netty.bootstrap.ServerBootstrap;

public class TcpServerBootstrap extends ServerBootstrap {

    public TcpServerBootstrap(ChannelFactory channelFactory) {
        super(channelFactory);
    }

    @Override
    public ChannelPipelineFactory getPipelineFactory() {
        final ChannelPipelineFactory wrapped = super.getPipelineFactory();
        return new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = wrapped.getPipeline();
                ChannelPipeline result = new ChannelPipelineDecorator(pipeline) {
                    @Override
                    public void attach(Channel channel, ChannelSink sink) {
                        super.attach(channel, new ChannelSinkDecorator(sink) {

                            //TODO: override eventSunk to fix k3po#128 (fulfill the future if it's a DownstreamFlushEvent )

                            @Override
                            public ChannelFuture execute(ChannelPipeline pipeline, Runnable task) {
                                NioWorker worker = TcpBootstrapFactorySpi.CURRENT_WORKER.get();
                                if (worker != null) {
                                    ChannelRunnableWrapper wrapper = new ChannelRunnableWrapper(pipeline.getChannel(), task);
                                    worker.executeInIoThread(wrapper, true);
                                    return wrapper;
                                }
                                return super.execute(pipeline, task);
                            }

                        });
                    }
                };
                return result;
            }

        };
    }

}
