/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
//                        try {
//                            Thread.sleep(20);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
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
