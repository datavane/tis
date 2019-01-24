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
package com.qlangtech.tis.hdfs.client.data;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TextLineRecordReader {
    // protected static Log log = LogFactory.getLog(TextLineRecordReader.class);
    // 
    // private long start;
    // private long pos;
    // private long end;
    // private BufferedInputStream in;
    // private ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);
    // private String[] titleText;
    // 
    // private char tab = '\t';
    // private String TAB;
    // private char eof = '\n';
    // 
    // private static class TextStuffer extends OutputStream {
    // public Text target;
    // 
    // public void write(int b) {
    // throw new UnsupportedOperationException("write(byte) not supported");
    // }
    // 
    // public void write(byte[] data, int offset, int len) throws IOException {
    // target.set(data, offset, len);//
    // }
    // }
    // 
    // private TextStuffer bridge = new TextStuffer();
    // 
    // public TextLineRecordReader(FileSystem fs, FileSplit split, String title,
    // char tab, char eof) throws IOException {
    // 
    // this.tab = tab;
    // TAB = String.valueOf(tab);
    // this.eof = eof;
    // FSDataInputStream fileIn = fs.open(split.getPath());//
    // this.in = new BufferedInputStream(fileIn);//
    // long start = split.getStart();//
    // long end = start + split.getLength();//
    // // Text titleText = new Text();
    // // readInto(titleText, this.eof);//
    // titleText = title.split(",");
    // fileIn = fs.open(split.getPath());
    // boolean skipFirstLine = false;
    // if (start != 0) {
    // skipFirstLine = true;
    // --start;
    // fileIn.seek(start);
    // // this.in = new BufferedInputStream(fileIn);// read stream
    // }
    // this.in = new BufferedInputStream(fileIn);// read stream
    // // if (start == 0&&) {
    // // start += TextLineRecordReader.readData(this.in, null, this.eof);
    // // }
    // if (skipFirstLine) {
    // start += TextLineRecordReader.readData(this.in, null, this.eof);
    // }
    // this.start = start;
    // this.pos = start;
    // this.end = end;
    // }
    // 
    // public TextLineRecordReader(FileSystem fs, FileSplit split, char tab,
    // char eof) throws IOException {
    // 
    // this.tab = tab;
    // this.eof = eof;
    // FSDataInputStream fileIn = fs.open(split.getPath());//
    // this.in = new BufferedInputStream(fileIn);// read stream
    // long start = split.getStart();// 文件开始
    // long end = start + split.getLength();// 结束位置
    // Text titleText = new Text();
    // readInto(titleText, Constants.EOL);// 读取Title
    // this.titleText = titleText.toString().split(
    // String.valueOf(Constants.TAB));
    // in.close();
    // fileIn = fs.open(split.getPath());
    // boolean skipFirstLine = false;
    // if (start != 0) {
    // skipFirstLine = true;
    // --start;
    // fileIn.seek(start);
    // // this.in = new BufferedInputStream(fileIn);// read stream
    // }
    // this.in = new BufferedInputStream(fileIn);// read stream
    // if (start == 0) {
    // start += TextLineRecordReader
    // .readData(this.in, null, Constants.EOL);
    // }
    // if (skipFirstLine) {
    // start += TextLineRecordReader
    // .readData(this.in, null, Constants.EOL);
    // }
    // this.start = start;
    // this.pos = start;
    // this.end = end;
    // }
    // 
    // private boolean readInto(Text text, char delimiter) throws IOException {
    // buffer.reset();
    // long bytesRead = readData(in, buffer, delimiter);
    // if (bytesRead == 0) {
    // return false;
    // }
    // pos += bytesRead;//
    // bridge.target = text;
    // buffer.writeTo(bridge);
    // return true;
    // }
    // 
    // public boolean next(Map<String, String> map) throws Exception {
    // if (pos >= end) {
    // return false;
    // }
    // Text lineText = new Text();
    // 
    // boolean hasRead = readInto(lineText, eof);
    // String line = null;
    // try {
    // if (hasRead) {
    // line = lineText.toString();
    // String[] columns = line.split(TAB);
    // if (columns.length != titleText.length) {
    // return hasRead;
    // }
    // 
    // for (int i = 0; i < titleText.length; i++) {
    // 
    // map.put(titleText[i], columns[i]);
    // }
    // }
    // } catch (Exception e) {
    // 
    // log.warn(" 读取文件出现错误，该行数据为>>>>>>   " + line);
    // log.warn(" 读取文件出现错误，该行数据Title长度为>>>>>   " + titleText.length);
    // throw e;
    // 
    // }
    // 
    // return hasRead;
    // 
    // }
    // 
    // public static void main() {
    // // String line = "93437001 105408750 1 0 199222443 niujiankang09
    // // 254381121 ru_501649402 i3/T1dqBJXoxzXXb1upjX.jpg C:\Documents and
    // // Settings\Administrator\ 0 20100820 20100820 1";
    // String linesString = "";
    // }
    // 
    // private static long readData(InputStream in, OutputStream out,
    // char delimiter) throws IOException {
    // long bytes = 0;
    // while (true) {
    // 
    // int b = in.read();
    // if (b == -1) {
    // break;
    // }
    // bytes += 1;
    // 
    // byte c = (byte) b;
    // if (c == Constants.EOL || c == delimiter) {
    // break;
    // }
    // 
    // if (c == '\r') {
    // in.mark(1);
    // byte nextC = (byte) in.read();
    // if (nextC != Constants.EOL || c == delimiter) {
    // in.reset();
    // } else {
    // bytes += 1;
    // }
    // break;
    // }
    // 
    // if (out != null) {//
    // out.write(c);
    // }
    // }
    // return bytes;
    // }
    // 
    // /**
    // * @return
    // */
    // @Override
    // public TSearcherQueryContext getConfig() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    // 
    // /**
    // * @param context
    // */
    // @Override
    // public void setConfig(TSearcherDumpContext context) {
    // 
    // }
    // 
    // public float getProgress() throws IOException {
    // if (start == end) {
    // return 0.0f;
    // } else {
    // return Math.min(1.0f, (pos - start) / (float) (end - start));
    // }
    // }
    // 
    // public void close() throws IOException {
    // in.close();
    // }
}
