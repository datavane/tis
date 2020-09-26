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
package store.hdfs;

import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import org.apache.lucene.store.OutputStreamIndexOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class TisFsFileWriter extends OutputStreamIndexOutput {

    public static final String HDFS_SYNC_BLOCK = "solr.hdfs.sync.block";

    public static final int BUFFER_SIZE = 16384;

    public TisFsFileWriter(ITISFileSystem fileSystem, IPath path, String name) throws IOException {
        super("fileSystem=" + fileSystem.getName() + " path=" + path, name, getOutputStream(fileSystem, path), 16384);
    }

    private static final OutputStream getOutputStream(ITISFileSystem fileSystem, IPath path) throws IOException {
        // }
        return fileSystem.create(path, true);
    }
}
