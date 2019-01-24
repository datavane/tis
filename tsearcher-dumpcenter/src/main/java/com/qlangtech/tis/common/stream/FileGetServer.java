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
package com.qlangtech.tis.common.stream;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import com.qlangtech.tis.common.protocol.Address;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FileGetServer {

    private static Logger logger = Logger.getLogger(FileGetServer.class);

    private Map<String, FileProvider> type2provider = new ConcurrentHashMap<String, FileProvider>();

    /**
     * @uml.property  name="port"
     */
    private int port;

    /**
     * @uml.property  name="host"
     */
    private String host;

    /**
     * @uml.property  name="acceptThread"
     * @uml.associationEnd
     */
    protected AcceptThread acceptThread = null;

    public FileGetServer(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public synchronized void end() {
        if (acceptThread != null) {
            try {
                acceptThread.close();
                acceptThread = null;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Address getAdd() {
        return new Address(host, port);
    }

    public FileProvider register(String type, FileProvider selector) {
        return type2provider.put(type, selector);
    }

    public synchronized void start() throws IOException {
        if (acceptThread != null)
            throw new IllegalStateException("Already started");
        ServerSocketChannel channel = ServerSocketChannel.open();
        try {
            channel.socket().bind(new InetSocketAddress(host, port));
        } catch (IOException e) {
            logger.warn("Socket.bind()");
            int i = 0;
            for (i = 0; i < 100; i++) {
                try {
                    channel.socket().bind(new InetSocketAddress(host, ++port));
                    logger.warn("绑定端口成功 ==>" + port);
                    break;
                } catch (IOException e1) {
                    logger.warn("绑定端口失败  ==>" + port);
                }
            }
        }
        acceptThread = new AcceptThread(channel);
        acceptThread.setName("FileGetServer-Accept-Thread");
        acceptThread.start();
    }

    public boolean isAlive() {
        return acceptThread != null && acceptThread.isAlive();
    }

    protected class AcceptThread extends Thread {

        ServerSocketChannel channel = null;

        public AcceptThread(ServerSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            while (true) {
                SocketChannel socketChannel = null;
                try {
                    socketChannel = channel.accept();
                    new ServerStreamThread(socketChannel).start();
                } catch (AsynchronousCloseException e) {
                    logger.warn("FileServer shutting down server thread.");
                    break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        protected void close() throws IOException {
            Streamer.close(channel);
        }
    }

    protected class ServerStreamThread extends Thread {

        SocketChannel channel = null;

        public ServerStreamThread(SocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            FileInputStream fileInputStream = null;
            File file = null;
            try {
                {
                    DataInputStream input = new DataInputStream(channel.socket().getInputStream());
                    byte[] fileReqArray = Streamer.readStream(input);
                    FileGetRequest fileGetReq = (FileGetRequest) Serializer.bytesToObject(fileReqArray);
                    FileProvider provider = type2provider.get(fileGetReq.getType());
                    int code = FileGetResponse.SUCCESS;
                    if (provider == null) {
                        code = FileGetResponse.FILE_TYPE_NOT_EXIST;
                    } else {
                        file = provider.getTargetFile(fileGetReq.getName());
                        if (file == null || !file.exists()) {
                            code = FileGetResponse.FILE_NOT_EXIST;
                        }
                    }
                    FileGetResponse fileGetRes = new FileGetResponse(fileGetReq.getType(), fileGetReq.getName());
                    fileGetRes.setCode(code);
                    if (code == FileGetResponse.SUCCESS) {
                        fileGetRes.setLength(file.length());
                    }
                    ByteBuffer buffer = Streamer.constructStream(Serializer.objectToBytes(fileGetRes));
                    channel.write(buffer);
                    assert buffer.remaining() == 0;
                    if (code != FileGetResponse.SUCCESS)
                        return;
                }
                {
                    fileInputStream = new FileInputStream(file);
                    byte[] data = new byte[Streamer.BUFFER_SIZE];
                    int readnum = 0;
                    while ((readnum = fileInputStream.read(data)) != -1) {
                        ByteBuffer writeBuffer = ByteBuffer.wrap(data, 0, readnum);
                        channel.write(writeBuffer);
                    }
                }
            } catch (Exception e) {
                logger.error(e, e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
                }
            }
        }
    }

    /**
     * @return
     * @uml.property  name="port"
     */
    public int getPort() {
        return port;
    }

    /**
     * @return
     * @uml.property  name="host"
     */
    public String getHost() {
        return host;
    }
}
