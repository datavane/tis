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
package com.qlangtech.tis.indexbuilder.source.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.Decompressor;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.indexbuilder.exception.RowException;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.source.SourceReader;
import com.qlangtech.tis.indexbuilder.utils.Context;
import com.qlangtech.tis.indexbuilder.utils.SimpleStringTokenizer;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HDFSReader implements SourceReader {

	public static final Logger logger = LoggerFactory.getLogger(HDFSReader.class);

	protected static final char DELIMITER_001 = '\001';

	protected Context context;

	// private final Log log = LogFactory.getLog(HDFSReader.class);
	protected long start;

	protected long pos;

	protected long end;

	protected BufferedInputStream in;

	protected ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);

	private String[] titleText;

	FSDataInputStream fileIn;

	protected IndexConf indexConf;

	protected FileSystem fs;

	protected FileSplit split;

	protected String delimiter = "\t";

	private String uniqueKey;

	private TextStuffer bridge = new TextStuffer();

	public void init() throws Exception {

		TaskContext taskContext = ((TaskContext) context.get("taskcontext"));
		String paramDelimiter = taskContext.getUserParam(IndexConf.KEY_COL_DELIMITER);
		if ("char001".equalsIgnoreCase(paramDelimiter)) {
			this.delimiter = String.valueOf(DELIMITER_001);
		}
		logger.warn(IndexConf.KEY_COL_DELIMITER + ":" + paramDelimiter);
		openStream();
		long start = this.split.getStart();
		long end = start + this.split.getLength();

		boolean skipFirstLine = false;
		if (start != 0L) {
			skipFirstLine = true;
			start -= 1L;
			this.fileIn.seek(start);
		}
		// }
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
	// protected String[] getTitles() {
	// return (String[]) this.context.get("titletext");
	// }
	private void openStream() throws Exception {
		this.fileIn = this.fs.open(this.split.getPath());
		if (this.indexConf.get("indexing.codec") != null) {
			Class codecClass = Class
					.forName(this.indexConf.get("indexing.codec", "org.apache.hadoop.io.compress.GzipCodec"));
			CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, this.fs.getConf());
			Decompressor decompressor = decompressor = CodecPool.getDecompressor(codec);
			CompressionInputStream cin = codec.createInputStream(this.fileIn, decompressor);
			this.in = new BufferedInputStream(this.fileIn);
		} else {
			this.in = new BufferedInputStream(this.fileIn);
		}
	}

	public HDFSReader(Context context, FileSplit split) throws Exception {
		this.context = context;
		this.indexConf = ((IndexConf) context.get("indexconf"));
		this.split = split;
		this.fs = ((FileSystem) context.get("filesystem"));
		init();
	}

	private boolean readInto(Text text, char delimiter) throws IOException {
		this.buffer.reset();
		long bytesRead = readData(this.in, this.buffer, delimiter);
		if (bytesRead == 0L) {
			return false;
		}
		this.pos += bytesRead;
		this.bridge.target = text;
		this.buffer.writeTo(this.bridge);
		return true;
	}

	private long readline = 0;

	public Map<String, String> next() throws Exception {
		if (this.pos >= this.end) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		Text lineText = new Text();
		String line = null;
		boolean hasRead = readInto(lineText, '\n');
		if (hasRead) {
			readline++;
			line = lineText.toString();
			String[] columns = SimpleStringTokenizer.split(line, this.delimiter);
			if (columns.length != this.titleText.length) {
				String error = "[error] cloumns length[" + columns.length + "] and titleText length["
						+ this.titleText.length + "] is mismatch......line [" + line + "]" + ",\n path:"
						+ this.split.getPath() + "\n line:" + readline;
				throw new RowException(error, map.toString());
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

	// public String[] getTitleText() {
	// return this.titleText;
	// }
	public void setTitleText(String[] titleText) {
		this.titleText = titleText;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public void close() throws IOException {
		this.in.close();
		this.buffer.close();
		this.bridge.close();
	}

	private static class TextStuffer extends OutputStream {

		public Text target;

		public void write(int b) {
			throw new UnsupportedOperationException("write(byte) not supported");
		}

		public void write(byte[] data, int offset, int len) throws IOException {
			this.target.set(data, offset, len);
		}
	}
}
