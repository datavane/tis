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
package com.qlangtech.tis.manage.common;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-16
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
