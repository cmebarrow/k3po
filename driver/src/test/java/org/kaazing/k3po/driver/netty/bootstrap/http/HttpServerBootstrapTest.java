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

package org.kaazing.k3po.driver.netty.bootstrap.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.netty.channel.Channels.close;
import static org.jboss.netty.channel.Channels.future;
import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.channel.Channels.write;
import static org.junit.Assert.assertEquals;
import static org.kaazing.k3po.driver.netty.channel.ChannelAddressFactory.newChannelAddressFactory;
import static org.kaazing.net.URLFactory.createURL;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.kaazing.k3po.driver.netty.bootstrap.ServerBootstrapRule;
import org.kaazing.k3po.driver.netty.bootstrap.http.HttpChannelConfig;
import org.kaazing.k3po.driver.netty.channel.ChannelAddress;
import org.kaazing.k3po.driver.netty.channel.ChannelAddressFactory;
import org.kaazing.k3po.driver.netty.channel.ShutdownInputEvent;
import org.kaazing.k3po.driver.netty.channel.SimpleChannelHandler;
import org.mockito.InOrder;

@RunWith(Theories.class)
public class HttpServerBootstrapTest {

    private static enum ContentStrategy { CLOSE, CHUNKED, BUFFERED, EXPLICIT }

    @Rule
    public final ServerBootstrapRule server = new ServerBootstrapRule("http");

    @Rule
    public final TestRule timeout = new DisableOnDebug(new Timeout(5, SECONDS));

    @DataPoints
    public static final Set<ContentStrategy> CONTENT_STRATEGIES = EnumSet.allOf(ContentStrategy.class);

    @Test
    public void shouldBindMoreThanOnceThenUnbindMoreThanOnce() throws Exception {

        server.setPipeline(pipeline(new SimpleChannelHandler()));

        ChannelAddressFactory channelAddressFactory = newChannelAddressFactory();
        ChannelAddress channelAddress1 = channelAddressFactory.newChannelAddress(URI.create("http://localhost:8000/path1"));
        ChannelAddress channelAddress2 = channelAddressFactory.newChannelAddress(URI.create("http://localhost:8000/path2"));
        Channel binding1 = server.bind(channelAddress1).syncUninterruptibly().getChannel();
        Channel binding2 = server.bind(channelAddress2).syncUninterruptibly().getChannel();

        binding1.unbind().syncUninterruptibly();
        binding2.unbind().syncUninterruptibly();
    }

    @Test(expected = Exception.class)
    public void shouldFailToBindMoreThanOnceWithEquivalentAddresses() throws Exception {


        server.setPipeline(pipeline(new SimpleChannelHandler()));

        ChannelAddressFactory channelAddressFactory = newChannelAddressFactory();
        ChannelAddress channelAddress1 = channelAddressFactory.newChannelAddress(URI.create("http://localhost:8000/path"));
        ChannelAddress channelAddress2 = channelAddressFactory.newChannelAddress(URI.create("http://localhost:8000/path"));
        Channel binding1 = server.bind(channelAddress1).syncUninterruptibly().getChannel();
        Channel binding2 = server.bind(channelAddress2).syncUninterruptibly().getChannel();

        binding1.unbind().syncUninterruptibly();
        binding2.unbind().syncUninterruptibly();
    }

    @Theory
    public void shouldAcceptEchoThenClose(final ContentStrategy strategy) throws Exception {
        ChannelHandler echoHandler = new SimpleChannelHandler() {
            @Override
            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                Channel channel = ctx.getChannel();
                ChannelBuffer message = (ChannelBuffer) e.getMessage();

                HttpChannelConfig config = (HttpChannelConfig) channel.getConfig();
                HttpHeaders writeHeaders = config.getWriteHeaders();
                switch (strategy) {
                case BUFFERED:
                    config.setMaximumBufferedContentLength(8192);
                    break;
                case CLOSE:
                    writeHeaders.set(Names.CONNECTION, Values.CLOSE);
                    break;
                case CHUNKED:
                    writeHeaders.set(Names.TRANSFER_ENCODING, Values.CHUNKED);
                    break;
                case EXPLICIT:
                    writeHeaders.set(Names.CONTENT_LENGTH, 12);
                    break;
                }

                write(ctx, future(channel), message);
                close(ctx, future(channel));
            }
        };

        final ChannelGroup childChannels = new DefaultChannelGroup();
        SimpleChannelHandler parent = new SimpleChannelHandler() {

            @Override
            public void childChannelOpen(ChannelHandlerContext ctx,
                    ChildChannelStateEvent e) throws Exception {
                childChannels.add(e.getChildChannel());
                super.childChannelOpen(ctx, e);
            }

        };
        SimpleChannelHandler parentSpy = spy(parent);

        SimpleChannelHandler child = new SimpleChannelHandler();
        SimpleChannelHandler childSpy = spy(child);

        server.setParentHandler(parentSpy);
        server.setPipeline(pipeline(childSpy, echoHandler));

        ChannelAddressFactory channelAddressFactory = newChannelAddressFactory();
        ChannelAddress channelAddress = channelAddressFactory.newChannelAddress(URI.create("http://localhost:8000/path"));
        Channel binding = server.bind(channelAddress).syncUninterruptibly().getChannel();

        URL location = createURL("http://localhost:8000/path");
        URLConnection connection = location.openConnection();
        connection.setDoOutput(true);
        OutputStream output = connection.getOutputStream();
        output.write("Hello, world".getBytes(UTF_8));
        output.close();

        DataInputStream input = new DataInputStream(connection.getInputStream());
        byte[] buf = new byte[12];
        input.readFully(buf);
        input.close();

        // wait for child channels to close
        for (Channel childChannel : childChannels) {
            ChannelFuture childCloseFuture = childChannel.getCloseFuture();
            childCloseFuture.syncUninterruptibly();
        }

        // wait for server channel to close
        binding.close().syncUninterruptibly();

        assertEquals("Hello, world", new String(buf, UTF_8));

        verify(parentSpy, times(6)).handleUpstream(any(ChannelHandlerContext.class), any(ChannelEvent.class));
        verify(parentSpy, times(2)).handleDownstream(any(ChannelHandlerContext.class), any(ChannelEvent.class));

        InOrder parentBind = inOrder(parentSpy);
        parentBind.verify(parentSpy).channelOpen(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        parentBind.verify(parentSpy).bindRequested(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        parentBind.verify(parentSpy).channelBound(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));

        InOrder parentChild = inOrder(parentSpy);
        parentChild.verify(parentSpy).childChannelOpen(any(ChannelHandlerContext.class), any(ChildChannelStateEvent.class));
        parentChild.verify(parentSpy).childChannelClosed(any(ChannelHandlerContext.class), any(ChildChannelStateEvent.class));

        InOrder parentClose = inOrder(parentSpy);
        parentClose.verify(parentSpy).closeRequested(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        parentClose.verify(parentSpy).channelUnbound(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        parentClose.verify(parentSpy).channelClosed(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));

        verify(childSpy, times(9)).handleUpstream(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        verify(childSpy, times(2)).handleDownstream(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));

        InOrder childConnect = inOrder(childSpy);
        childConnect.verify(childSpy).channelOpen(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        childConnect.verify(childSpy).channelBound(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        childConnect.verify(childSpy).channelConnected(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));

        InOrder childRead = inOrder(childSpy);
        childRead.verify(childSpy).messageReceived(any(ChannelHandlerContext.class), any(MessageEvent.class));
        childRead.verify(childSpy).inputShutdown(any(ChannelHandlerContext.class), any(ShutdownInputEvent.class));

        InOrder childWrite = inOrder(childSpy);
        childWrite.verify(childSpy).writeRequested(any(ChannelHandlerContext.class), any(MessageEvent.class));
        childWrite.verify(childSpy).writeComplete(any(ChannelHandlerContext.class), any(WriteCompletionEvent.class));

        InOrder childClose = inOrder(childSpy);
        childClose.verify(childSpy).closeRequested(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        childClose.verify(childSpy).channelDisconnected(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        childClose.verify(childSpy).channelUnbound(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));
        childClose.verify(childSpy).channelClosed(any(ChannelHandlerContext.class), any(ChannelStateEvent.class));

        verifyNoMoreInteractions(parentSpy, childSpy);
    }
}