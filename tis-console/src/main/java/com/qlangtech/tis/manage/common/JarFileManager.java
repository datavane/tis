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
package com.qlangtech.tis.manage.common;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
abstract class JarFileManager {
    // private final AppDomainInfo domainInfo;
    // final File saveFile;
    // 
    // private JarFileManager(Application application, Long currentTimestamp) {
    // super();
    // // this.domainInfo = domainInfo;
    // this.saveFile = new File(ConfigFileReader.getAppDomainDir(Config
    // .getLocalRepository(), application.getDptId(), application
    // .getAppId()), String.valueOf(currentTimestamp) + ".jar");
    // }
    // 
    // public File getSaveFile() {
    // return this.saveFile;
    // }
    // 
    // /**
    // * @param validateCode
    // * 校验码 ，防止文件被读之前已经被人篡改过了
    // * @return
    // * @throws FileNotFoundException
    // */
    // public InputStream readFile(String validateCode)
    // throws FileNotFoundException {
    // if (!saveFile.exists()) {
    // throw new IllegalStateException("file:"
    // + saveFile.getAbsolutePath() + " is not exist can not read");
    // }
    // 
    // try {
    // if (!StringUtils.equalsIgnoreCase(md5file(this.saveFile),
    // validateCode)) {
    // throw new IllegalStateException("saveFile:"
    // + this.saveFile.getAbsolutePath() + " 已经被篡改过了");
    // }
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // 
    // return new FileInputStream(this.saveFile);
    // }
    // 
    // public long getFileSize() {
    // if (!saveFile.exists()) {
    // throw new IllegalStateException("file:"
    // + saveFile.getAbsolutePath() + " is not exist can not read");
    // }
    // 
    // return saveFile.length();
    // }
    // 
    // /**
    // *保存文件
    // *
    // * @param reader
    // * @return validate code
    // */
    // public String save(InputStream reader) throws IOException {
    // 
    // if (!saveFile.exists() && !saveFile.createNewFile()) {
    // throw new IllegalStateException("file:"
    // + saveFile.getAbsolutePath() + " can not be null");
    // }
    // // 将流保存到预定目录
    // return saveFile(reader, saveFile);
    // }
    // 
    // /**
    // * @param saveFile
    // * @return
    // * @throws FileNotFoundException
    // * @throws IOException
    // */
    // private static String md5file(final File saveFile)
    // throws FileNotFoundException, IOException {
    // // 保存的文件的签名
    // return md5file(new FileInputStream(saveFile));
    // }
    // 
    // private static String md5file(InputStream reader) throws IOException {
    // 
    // try {
    // // 保存的文件的签名
    // return ConfigFileReader.md5file(IOUtils.toByteArray(reader));
    // } finally {
    // try {
    // reader.close();
    // } catch (Throwable e) {
    // }
    // }
    // }
    // 
    // public static String saveFile(final InputStream reader, final File
    // saveFile)
    // throws FileNotFoundException, IOException {
    // OutputStream writer = null;
    // try {
    // 
    // writer = new FileOutputStream(saveFile);
    // IOUtils.copy(reader, writer);
    // } finally {
    // try {
    // writer.close();
    // } catch (Throwable e) {
    // }
    // 
    // try {
    // reader.close();
    // } catch (Throwable e) {
    // }
    // }
    // 
    // return md5file(saveFile);
    // }
}
