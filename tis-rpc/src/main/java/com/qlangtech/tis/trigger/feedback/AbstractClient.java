/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.feedback;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractClient<// extends TaskOverseer
		T> {

	private static final Logger log = LoggerFactory.getLogger(AbstractClient.class);

	private final int port;

	private final ChannelGroup channelGroup;

	/**
	 * @param duration
	 * @param timeunit
	 * @param maxErrorCount
	 */
	public AbstractClient(int port, long duration, TimeUnit timeunit, int maxErrorCount) {
		this.port = port;
		// (1)
		this.bootstrap = new Bootstrap();
		this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		initialBootstrap();
		// this.oversee.start();
	}

	protected void connect2Remote() throws Exception {
		String[] remoteLogSourceAddress = getTargetAddress();
		Object address = remoteLogSourceAddress;
		log.info("remoteLogSourceAddress:{}", address);
		reConnect(Arrays.asList(remoteLogSourceAddress));
	}

	protected void reConnect(List<String> linkHost) throws Exception {
		StringBuffer hosts = new StringBuffer();
		for (String host : linkHost) {
			hosts.append(host).append(",");
		}
		log.info("connect to:" + hosts);
		// 重连
		this.channelGroup.close().sync();
		for (String host : linkHost) {
			bootstrap.connect(host, port).sync();
		}
	}

	/**
	 * 客户端向服务端发送消息
	 *
	 * @param info
	 */
	public void sendInfo(Serializable info) {
		int trycount = 1;
		while (channelGroup.isEmpty()) {
			if (trycount++ > 10) {
				throw new IllegalStateException("trycount:" + trycount + " channelgroup size 0");
			}
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
			}
			log.info("waitting channel creating:" + (trycount));
		}
		channelGroup.writeAndFlush(info);
	}

	// @Override
	// protected void startOverseer() {
	//
	// }
	protected ChannelGroup getChannelGroup() {
		return this.channelGroup;
	}

	protected void initialBootstrap() {
		try {
			// (2)
			bootstrap.group(workerGroup);
			// (3)
			bootstrap.channel(NioSocketChannel.class);
			// (4)
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
					super.handlerRemoved(ctx);
				}

				public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
					log.error(cause.getMessage(), cause);
					super.exceptionCaught(ctx, cause);
				}

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast("ping", new IdleHandler(20, -1, -1, TimeUnit.SECONDS));
					ch.pipeline().addLast("decoder", new ObjectDecoder(new ClassResolver() {

						public Class<?> resolve(String className) throws ClassNotFoundException {
							return this.getClass().getClassLoader().loadClass(className);
						}
					}));
					ch.pipeline().addLast("encoder", new ObjectEncoder());
					ch.pipeline().addLast("stateLogHandler", new StateLogHandlerAdapter());
				}
			});
		} finally {
			// workerGroup.shutdownGracefully();
		}
	}

	private class IdleHandler extends IdleStateHandler {

		public IdleHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
			super(readerIdleTime, writerIdleTime, allIdleTime, unit);
		}

		@Override
		public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
			log.warn("IdleHandler was removed");
			// relaunch();
			super.handlerRemoved(ctx);
		}
	}

	// ///////////////////////////////////////////////////////////
	// private static final int LISTENER_PORT = 8848;
	final EventLoopGroup workerGroup = new NioEventLoopGroup();

	private final Bootstrap bootstrap;

	/**
	 * @return
	 */
	protected String[] getTargetAddress() {
		String[] remoteLogSourceAddress = StringUtils.split(TSearcherConfigFetcher.get().getLogSourceAddress(), ",");
		if (remoteLogSourceAddress.length < 1) {
			throw new IllegalStateException("remoteLogSourceAddress length can not small than 1");
		}
		return remoteLogSourceAddress;
	}

	private class StateLogHandlerAdapter extends ChannelHandlerAdapter {

		@Override
		@SuppressWarnings("all")
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof Ping) {
				log.info("receive an ping");
				return;
			}
			T log = (T) msg;
			processMessage(log);
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			channelGroup.add(ctx.channel());
			super.channelActive(ctx);
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (evt instanceof IdleStateEvent) {
				IdleStateEvent event = (IdleStateEvent) evt;
				log.info("IdleState:" + event.state());
				if (event.state() == (IdleState.READER_IDLE)) {
					log.warn("IdleState.READER_IDLE:" + IdleState.READER_IDLE + " close the connection");
					// ctx.close();
					// relaunch();
				}
			}
			super.userEventTriggered(ctx, evt);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			log.error(cause.getMessage(), cause);
			processError(cause);
			super.exceptionCaught(ctx, cause);
			// ctx.close();
			// relaunch();
		}

		public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			log.error("channel:" + ctx.name() + " disconnect");
			disconnectChannel(ctx);
		}
	}

	protected abstract void disconnectChannel(ChannelHandlerContext ctx);

	protected abstract void processError(Throwable e);

	/**
	 * @param state
	 */
	protected abstract void processMessage(T state);

}
