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
package com.qlangtech.tis.indexbuilder.source.impl;

import com.qlangtech.tis.fs.FSDataInputStream;
import com.qlangtech.tis.fs.IFileSplit;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.exception.RowException;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.source.SourceReader;
import com.qlangtech.tis.indexbuilder.utils.Context;
import com.qlangtech.tis.indexbuilder.utils.SimpleStringTokenizer;
import com.qlangtech.tis.trigger.jst.ImportDataProcessInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HDFSReader implements SourceReader {

    public static final Logger logger = LoggerFactory.getLogger(HDFSReader.class);

    protected static final char DELIMITER_001 = '\001';

    protected static final char DELIMITER_TAB = '\t';

    protected Context context;

    // private final Log log = LoggerFactory.getLogger(HDFSReader.class);
    protected long start;

    protected long pos;

    protected long end;

    protected BufferedInputStream in;

    protected ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);

    private String[] titleText;

    FSDataInputStream fileIn;

    protected IndexConf indexConf;

    protected ITISFileSystem fs;

    protected IFileSplit split;

    protected String delimiter = "\t";

    // private String uniqueKey;
    private TextStuffer bridge = new TextStuffer();

    public void init() throws Exception {
        TaskContext taskContext = ((TaskContext) context.get("taskcontext"));
        String paramDelimiter = String.valueOf(DELIMITER_TAB);
        if (ImportDataProcessInfo.DELIMITER_001.equalsIgnoreCase(paramDelimiter)) {
            this.delimiter = String.valueOf(DELIMITER_001);
        }
        openStream();
        long start = this.split.getStart();
        long end = start + this.split.getLength();
        boolean skipFirstLine = false;
        if (start != 0L) {
            skipFirstLine = true;
            start -= 1L;
            this.fileIn.seek(start);
        }
        if (skipFirstLine) {
            start += readData(this.in, null, '\n');
        }
        this.start = start;
        this.pos = start;
        this.end = end;
    }

    /**
     * @return
     */
    private void openStream() throws Exception {
        this.fileIn = this.fs.open(this.split.getPath());
        this.in = new BufferedInputStream(this.fileIn);
    }

    public HDFSReader(Context context, IFileSplit split, ITISFileSystem fs) throws Exception {
        this.context = context;
        this.indexConf = ((IndexConf) context.get("indexconf"));
        this.split = split;
        this.fs = fs;
        init();
    }

    private boolean readInto(char delimiter) throws IOException {
        this.buffer.reset();
        long bytesRead = readData(this.in, this.buffer, delimiter);
        if (bytesRead == 0L) {
            return false;
        }
        this.pos += bytesRead;
        // this.bridge.target = text;
        this.buffer.writeTo(this.bridge);
        return true;
    }

    private long readline = 0;

    public Map<String, String> next() throws Exception {
        if (this.pos >= this.end) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        // Text lineText = new Text();
        String line = null;
        boolean hasRead = readInto('\n');
        if (hasRead) {
            readline++;
            // lineText.toString();
            line = bridge.target.toString();
            String[] columns = SimpleStringTokenizer.split(line, this.delimiter);
            if (columns.length != this.titleText.length) {
                String error = "[error] cloumns length[" + columns.length + "] and titleText length[" + this.titleText.length
                        + "] is mismatch......line [" + line + "]" + ",\n path:" + this.split.getPath() + "\n line:" + readline;
                throw new RowException(parselineContent(columns, this.titleText).append("\n").append(error).toString(), map.toString());
            }
            for (int i = 0; i < columns.length; i++) {
                if (StringUtils.trim(columns[i]).length() != 0) {
                    map.put(this.titleText[i], columns[i]);
                }
            }
        } else {
            return null;
        }
        return map;
    }

    @Override
    public String toString() {
        return "split:" + this.split.getPath().getName() + ",read:" + readline;
    }

    protected static StringBuffer parselineContent(String[] columns, String[] titleText) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < titleText.length; i++) {
            result.append("title[" + i + "]:").append(titleText[i]).append(",");
        }
        result.append("\n");
        for (int i = 0; i < columns.length; i++) {
            result.append("col[" + i + "]:").append(columns[i]).append(",");
        }
        return result;
    }

    protected static long readData(InputStream in, OutputStream out, char delimiter) throws IOException {
        long bytes = 0L;
        while (true) {
            int b = in.read();
            if (b == -1) {
                break;
            }
            bytes += 1L;
            byte c = (byte) b;
            if ((c == 10) || (c == delimiter)) {
                break;
            }
            if (c == 13) {
                in.mark(1);
                byte nextC = (byte) in.read();
                if ((nextC != 10) || (c == delimiter)) {
                    in.reset();
                    break;
                }
                bytes += 1L;
                break;
            }
            if (out != null) {
                out.write(c);
            }
        }
        return bytes;
    }

    public void setTitleText(String[] titleText) {
        this.titleText = titleText;
    }

    public void close() throws IOException {
        this.in.close();
        this.buffer.close();
        this.bridge.close();
    }

    private static class TextStuffer extends OutputStream {

        public final Text target = new Text();

        public void write(int b) {
            throw new UnsupportedOperationException("write(byte) not supported");
        }

        public void write(byte[] data, int offset, int len) throws IOException {
            this.target.set(data, offset, len);
        }
    }
}
