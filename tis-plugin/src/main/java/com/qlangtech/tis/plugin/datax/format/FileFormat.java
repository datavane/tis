/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.datax.format;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.plugin.unstructuredstorage.reader.UnstructuredReader;
import com.alibaba.datax.plugin.unstructuredstorage.writer.UnstructuredWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataType;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * "enum": [
 * {
 * "val": "csv",
 * "label": "CSV"
 * },
 * {
 * "val": "text",
 * "label": "TEXT"
 * }
 * ]
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-10-14 17:02
 **/
public abstract class FileFormat implements Describable<FileFormat> {

    /**
     * 根据FlileFormat 设置对已经创建的colValCreator
     *
     * @return
     */
    public abstract Function<String, Column> buildColValCreator(CMeta cmeta);

    public abstract UnstructuredWriter createWriter(OutputStream writer);

    public abstract UnstructuredReader createReader(InputStream input);

    /**
     * @param input
     * @return
     * @throws IOException
     */
    public abstract FileHeader readHeader(InputStream input) throws IOException;

    /**
     * "suffix": {
     * "help": "描述：最后输出文件的后缀，当前支持 \".text\"以及\".csv\""
     * },
     *
     * @return
     */
    public final String getSuffix() {
        return "." + getFormat();
    }

    public abstract boolean containHeader();

    public abstract char getFieldDelimiter();

    public final String getFormat() {
        return StringUtils.lowerCase(this.getDescriptor().getDisplayName());
    }


    public static class FileHeader {
        public final int colCount;
        final Optional<List<String>> headerCols;
        final List<DataType> types;

        public FileHeader(int colCount, List<DataType> types) {
            this(colCount, null, types);
        }

        public FileHeader(int colCount, List<String> headerCols, List<DataType> types) {
            this.colCount = colCount;
            this.headerCols = Optional.ofNullable(headerCols);
            this.types = Collections.unmodifiableList(types);
        }

        public List<DataType> getTypes() {
            return this.types;
        }

        public boolean containHeader() {
            return this.headerCols.isPresent();
        }

        public List<String> getHeaderCols() {
            return this.headerCols.get();
        }

    }
}
