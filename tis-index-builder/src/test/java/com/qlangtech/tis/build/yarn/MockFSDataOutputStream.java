/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.fs.TISFSDataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-27 19:00
 */
public class MockFSDataOutputStream extends TISFSDataOutputStream {

    public MockFSDataOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    // super.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public long getPos() throws IOException {
        return 0;
    }
}
