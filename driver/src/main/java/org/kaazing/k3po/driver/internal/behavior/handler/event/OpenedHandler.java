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

package org.kaazing.k3po.driver.internal.behavior.handler.event;

import static java.util.EnumSet.of;

import java.util.ArrayDeque;
import java.util.Queue;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

public class OpenedHandler extends AbstractEventHandler {
    private final Queue<ChannelEvent> pending = new ArrayDeque<ChannelEvent>();
    private boolean eventInProgress;

    public OpenedHandler() {
        super(of(ChannelEventKind.OPEN));
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        ChannelFuture handlerFuture = getHandlerFuture();
        assert handlerFuture != null;
        handlerFuture.setSuccess();
    }

    @Override
    public String toString() {
        return "opened";
    }

    @Override
    protected void handleUpstream1(ChannelHandlerContext ctx, ChannelEvent e) throws Exception  {
        synchronized (ctx) {
            // Always allow the call stack to unwind from one upstream event before passing on another
            // to avoid issues with future listeners not being completely fired when one event (like close)
            // causes other events to occur
            if (eventInProgress) {
                pending.add(e);
                return;
            }
            eventInProgress = true;
            super.handleUpstream1(ctx, e);
            ChannelEvent event;
            while ((event = pending.poll()) != null) {
                super.handleUpstream1(ctx, event);
            }
            eventInProgress = false;
        }
    }
}
