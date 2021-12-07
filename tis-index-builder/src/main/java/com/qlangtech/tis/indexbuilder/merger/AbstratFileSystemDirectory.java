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
