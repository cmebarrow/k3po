/*
 * Copyright (c) 2014 "Kaazing Corporation," (www.kaazing.com)
 *
 * This file is part of Robot.
 *
 * Robot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kaazing.robot.driver.behavior.handler.event;

import static java.util.EnumSet.of;
import static org.kaazing.robot.driver.behavior.handler.event.AbstractEventHandler.ChannelEventKind.FLUSHED;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.kaazing.robot.driver.netty.channel.FlushEvent;

public class FlushedHandler extends AbstractEventHandler {

    public FlushedHandler() {
        super(of(FLUSHED));
    }

    @Override
    public void flushed(ChannelHandlerContext ctx, FlushEvent e) {
        getHandlerFuture().setSuccess();
    }

    @Override
    public String toString() {
        return "flushed";
    }

}