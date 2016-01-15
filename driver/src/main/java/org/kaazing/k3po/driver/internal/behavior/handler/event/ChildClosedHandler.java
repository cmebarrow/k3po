/**
 * Copyright 2007-2015, Kaazing Corporation. All rights reserved.
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
import static org.kaazing.k3po.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.CHILD_CLOSED;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChildChannelStateEvent;

public class ChildClosedHandler extends AbstractServerEventHandler {

    public ChildClosedHandler() {
        super(of(CHILD_CLOSED));
    }

    @Override
    public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {

        ChannelFuture handlerFuture = getHandlerFuture();
        assert handlerFuture != null;
        handlerFuture.setSuccess();
    }

    @Override
    protected StringBuilder describe(StringBuilder sb) {
        return sb.append("child closed");
    }
}
