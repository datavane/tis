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
package com.qlangtech.tis.trigger.netty;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.trigger.feedback.Ping;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.RegisterMonotorTarget;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerLogServer {

	// LogFactory.getLog(TriggerLogServer.class);;
	private static final Logger logger = LoggerFactory.getLogger(TriggerLogServer.class);

	private final int port;

	public TriggerLogServer(int port) {
		this.port = port;
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void run() throws Exception {
		// (1)
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			// (2)
			ServerBootstrap b = new ServerBootstrap();
			// (3)
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new // (4)
			ChannelInitializer<SocketChannel>() {

				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast("ping", new IdleStateHandler(-1, 10, -1, TimeUnit.SECONDS));
					ch.pipeline().addLast(new ObjectEncoder(), new ChannelHandlerAdapter() {

						// @Override
						// public void channelActive(ChannelHandlerContext ctx)
						// throws Exception {
						// super.channelActive(ctx);
						// recipients.add(ctx.channel());
						//
						// }
						@Override
						public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
							if (evt instanceof IdleStateEvent) {
								IdleStateEvent event = (IdleStateEvent) evt;
								if (event.state() == (IdleState.WRITER_IDLE)) {
									logger.info("write an ping to client:" + ctx.channel().remoteAddress());
									ctx.writeAndFlush(new Ping());
								}
							}
							// super.userEventTriggered(ctx, evt);
						}

						@Override
						public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
							// ctx.channel();
							logger.error(cause.getMessage(), cause);
							// super.exceptionCaught(ctx, cause);
						}
					});
					ch.pipeline().addLast("decoder", new ObjectDecoder(new ClassResolver() {

						public Class<?> resolve(String className) throws ClassNotFoundException {
							return this.getClass().getClassLoader().loadClass(className);
						}
					}));
					ch.pipeline().addLast("stateLogHandler", new LogMonitorAdapter());
				}
			}).option(ChannelOption.SO_BACKLOG, // (5)
					128).childOption(ChannelOption.SO_KEEPALIVE, // (6)
							true);
			// Bind and start to accept incoming connections.
			logger.info("start bind point:" + port);
			// (7)
			ChannelFuture f = b.bind(port).sync();
			// Wait until the server socket is closed.
			// f.channel().closeFuture().sync();
			// logger.info("the server socket is closed");
		} finally {
			// workerGroup.shutdownGracefully();
			// bossGroup.shutdownGracefully();
		}
	}

	/**
	 * 接收客户端 注册监听或者取消注册的信号
	 */
	private class LogMonitorAdapter extends ChannelHandlerAdapter {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			// super.exceptionCaught(ctx, cause);
			logger.info("channel:" + ctx.name() + " close ,remove audience count", cause);
			// 客户端主动关闭
			for (LogFileMonitor fileMonitor : LogFileMonitor.getAllMonitor()) {
				fileMonitor.removeAudience(ctx);
			}
		}

		@Override
		@SuppressWarnings("all")
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof Ping) {
				logger.info("receive an ping");
				return;
			}
			final RegisterMonotorTarget register = (RegisterMonotorTarget) msg;

			LogFileMonitor fileMonitor = LogFileMonitor.getLogFileMonitor(register, TriggerLogServer.this);
			if (register.isRegister()) {
				logger.info(
						"receive register monitor:" + register.getCollection() + ",logtype:" + register.getLogType());
				fileMonitor.startMonitor(ctx);
			} else {
				logger.info(
						"receive unregister monitor:" + register.getCollection() + ",logtype:" + register.getLogType());
				fileMonitor.removeAudience(ctx);
			}
		}
	}

	public static void main(String[] args) throws Exception {

	}
}
