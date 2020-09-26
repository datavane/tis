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
package com.qlangtech.tis.indexbuilder.merger;

import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import java.io.IOException;

/**
 * @description Abstract filesystem factory. This factory creates oss filesystem
 *              and hdf filesystem according to the different
 *              indexing.fileSystemType
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstratFileSystemDirectory extends Directory {

    public static Directory createFileSystemDirectory(Directory dir, ITISFileSystem fs, IPath directory, boolean create, IndexConf indexconf) throws IOException {
        // }else if("hdfs".equalsIgnoreCase(indexconf.getFileSystemType())){
        return new FileSystemDirectory(fs, directory, true);
    // }else{
    // throw new IllegalArgumentException(indexconf.getFileSystemType());
    // }
    }

    public void copyTo(Directory to, String src, String dest, IndexInput in) throws IOException {
        ;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public String[] listAll() throws IOException {
        return null;
    }
}
