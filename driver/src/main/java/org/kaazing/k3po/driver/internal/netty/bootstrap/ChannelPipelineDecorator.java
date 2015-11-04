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

import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;

public class ChannelPipelineDecorator implements ChannelPipeline {
    private final ChannelPipeline delegate;

    public ChannelPipelineDecorator(ChannelPipeline delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addFirst(String name, ChannelHandler handler) {
        delegate.addFirst(name, handler);
    }

    @Override
    public void addLast(String name, ChannelHandler handler) {
        delegate.addLast(name, handler);
    }

    @Override
    public void addBefore(String baseName, String name, ChannelHandler handler) {
        delegate.addBefore(baseName, name, handler);
    }

    @Override
    public void addAfter(String baseName, String name, ChannelHandler handler) {
        delegate.addAfter(baseName, name, handler);
    }

    @Override
    public void remove(ChannelHandler handler) {
        delegate.remove(handler);
    }

    @Override
    public ChannelHandler remove(String name) {
        return delegate.remove(name);
    }

    @Override
    public <T extends ChannelHandler> T remove(Class<T> handlerType) {
        return delegate.remove(handlerType);
    }

    @Override
    public ChannelHandler removeFirst() {
        return delegate.removeFirst();
    }

    @Override
    public ChannelHandler removeLast() {
        return delegate.removeLast();
    }

    @Override
    public void replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler) {
        delegate.replace(oldHandler, newName, newHandler);
    }

    @Override
    public ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler) {
        return delegate.replace(oldName, newName, newHandler);
    }

    @Override
    public <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler) {
        return delegate.replace(oldHandlerType, newName, newHandler);
    }

    @Override
    public ChannelHandler getFirst() {
        return delegate.getFirst();
    }

    @Override
    public ChannelHandler getLast() {
        return delegate.getLast();
    }

    @Override
    public ChannelHandler get(String name) {
        return delegate.get(name);
    }

    @Override
    public <T extends ChannelHandler> T get(Class<T> handlerType) {
        return delegate.get(handlerType);
    }

    @Override
    public ChannelHandlerContext getContext(ChannelHandler handler) {
        return delegate.getContext(handler);
    }

    @Override
    public ChannelHandlerContext getContext(String name) {
        return delegate.getContext(name);
    }

    @Override
    public ChannelHandlerContext getContext(Class<? extends ChannelHandler> handlerType) {
        return delegate.getContext(handlerType);
    }

    @Override
    public void sendUpstream(ChannelEvent e) {
        delegate.sendUpstream(e);
    }

    @Override
    public void sendDownstream(ChannelEvent e) {
        delegate.sendDownstream(e);
    }

    @Override
    public ChannelFuture execute(Runnable task) {
        return delegate.execute(task);
    }

    @Override
    public Channel getChannel() {
        return delegate.getChannel();
    }

    @Override
    public ChannelSink getSink() {
        return delegate.getSink();
    }

    @Override
    public void attach(Channel channel, ChannelSink sink) {
        delegate.attach(channel, sink);
    }

    @Override
    public boolean isAttached() {
        return delegate.isAttached();
    }

    @Override
    public List<String> getNames() {
        return delegate.getNames();
    }

    @Override
    public Map<String, ChannelHandler> toMap() {
        return delegate.toMap();
    }

}
