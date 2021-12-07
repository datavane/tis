/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.solr.handler.admin;

import org.apache.lucene.store.*;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISCopy2LocalDirectory extends NIOFSDirectory {

    // 统计已经向本地写入多少字节
    private final AtomicLong allReadBytesCount;
    private final IContentReadListener readListener;
    private long currentTime = System.currentTimeMillis();

    public TISCopy2LocalDirectory(Path path, LockFactory lockFactory, AtomicLong allReadBytesCount, IContentReadListener readListener) throws IOException {
        super(path, lockFactory);
        this.allReadBytesCount = allReadBytesCount;
        this.readListener = readListener;
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        return new FSIndexOutput(name);
    }

    final class FSIndexOutput extends OutputStreamIndexOutput {

        static final int CHUNK_SIZE = 8192;

        public FSIndexOutput(String name) throws IOException {
            this(name, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }

        FSIndexOutput(String name, OpenOption... options) throws IOException {
            super("FSIndexOutput(path=\"" + directory.resolve(name) + "\")", name, new FilterOutputStream(Files.newOutputStream(directory.resolve(name), options)) {
                // This implementation ensures, that we never write more
                // than CHUNK_SIZE bytes:
                @Override
                public void write(byte[] b, int offset, int length) throws IOException {
                    while (length > 0) {
                        final int chunk = Math.min(length, CHUNK_SIZE);
                        allReadBytesCount.addAndGet(chunk);
                        out.write(b, offset, chunk);
                        length -= chunk;
                        offset += chunk;
                        long now = System.currentTimeMillis();
                        if ((currentTime + 2000) < now) {
                            readListener.hasReaded(allReadBytesCount.get());
                            currentTime = now;
                        }
                    }
                }
            }, CHUNK_SIZE);
        }
    }

    protected interface IContentReadListener {
        /**
         * 已经读到的字节数目
         *
         * @param size
         */
        public void hasReaded(long size);
    }
}
