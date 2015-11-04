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

package org.kaazing.k3po.driver.internal.behavior.handler.command;

import static java.lang.String.format;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.kaazing.k3po.driver.internal.behavior.handler.ExecutionHandler;
import org.kaazing.k3po.driver.internal.behavior.handler.prepare.PreparationEvent;

public abstract class AbstractCommandHandler extends ExecutionHandler implements ChannelDownstreamHandler {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractCommandHandler.class);
    private boolean synchronous;

    AbstractCommandHandler() {
        this(false);
    }

    AbstractCommandHandler(boolean synchronous) {
        this.synchronous = synchronous;
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {
        synchronized (ctx) {
            ctx.sendDownstream(evt);
        }
    }

    @Override
    public void prepareRequested(final ChannelHandlerContext ctx, PreparationEvent evt) {

        super.prepareRequested(ctx, evt);

        ChannelFuture pipelineFuture = getPipelineFuture();
        pipelineFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    synchronized (ctx) {
                        invokeCommand(future, ctx);
                    }
                }
            }

        });
    }

    protected abstract void invokeCommand0(ChannelHandlerContext ctx) throws Exception;

    private void invokeCommand(ChannelFuture future, final ChannelHandlerContext ctx) throws Exception {
        if (synchronous) {
            invokeCommand0(ctx);
            return;
        }
        ChannelPipeline pipeline = future.getChannel().getPipeline();
        pipeline.getSink().execute(pipeline, new Runnable() {

            @Override
            public void run() {
                try {
                    invokeCommand0(ctx);
                } catch (Exception e) {
                    LOGGER.error(format("Caught exception %s while executing command %s", e, AbstractCommandHandler.this), e);
                }

            }

        });
    }

}
