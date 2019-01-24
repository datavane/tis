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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FileGetClient {

    private final int port;

    private final String host;

    public FileGetClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public int doGetFile(String type, String name, FileOutputStream out) throws IOException {
        SocketChannel channel = SocketChannel.open();
        FileGetRequest fileGetReq = new FileGetRequest(type, name);
        try {
            channel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
            channel.connect(new InetSocketAddress(host, port));
            byte[] bytes = Serializer.objectToBytes(fileGetReq);
            ByteBuffer buffer = Streamer.constructStream(bytes);
            channel.write(buffer);
            assert buffer.remaining() == 0;
            DataInputStream input = new DataInputStream(channel.socket().getInputStream());
            byte[] contentBytes = Streamer.readStream(input);
            FileGetResponse fileGetRes = (FileGetResponse) Serializer.bytesToObject(contentBytes);
            int code = fileGetRes.getCode();
            if (code != FileGetResponse.SUCCESS) {
                return code;
            }
            long total = fileGetRes.getLength();
            byte[] data = new byte[Streamer.BUFFER_SIZE];
            long readed = 0;
            int readnum = 0;
            while ((readnum = input.read(data)) != -1) {
                out.write(data, 0, readnum);
                readed += readnum;
            }
            if (readed != total)
                return FileGetResponse.FILE_EXCEPTION;
            return FileGetResponse.SUCCESS;
        } finally {
            Streamer.close(channel);
        }
    }
}
