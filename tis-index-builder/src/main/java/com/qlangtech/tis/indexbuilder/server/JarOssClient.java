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
package com.qlangtech.tis.indexbuilder.server;

/*
 * Created by kongshi.yks on 15-4-9.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JarOssClient {
    // private OSSClient client;
    // private final String bucketname = "newtisbucket";
    // private final String accessKeyId = "gqUn8BtJMgGV3bMs";
    // private final String accessKeySecret = "W2o14XfHmCCJeGu2ys2PoA3vN2ehOo";
    // private final String endPoint = "http://oss-cn-hangzhou-zmf.aliyuncs.com";
    // public static final String OSS_ROOT_KEY = "oss_jar_";
    // 
    // public OSSClient getClient() {
    // return this.client;
    // }
    // 
    // public void setClient(OSSClient client) {
    // this.client = client;
    // }
    // 
    // public String getBucketname() {
    // return bucketname;
    // }
    // 
    // public String getAccessKeyId() {
    // return accessKeyId;
    // }
    // 
    // public String getAccessKeySecret() {
    // return accessKeySecret;
    // }
    // 
    // public JarOssClient() {
    // client = new OSSClient(endPoint, accessKeyId, accessKeySecret);
    // }
    // 
    // public Bucket createBucket(String bucketname) {
    // try {
    // return this.client.createBucket(bucketname);
    // } catch (OSSException var3) {
    // throw new RuntimeException("createBucket error!!!  bucketname:" + bucketname + "   error:" + var3);
    // } catch (ClientException var4) {
    // throw new RuntimeException("createBucket error!!!  bucketname:" + bucketname + "   error:" + var4);
    // }
    // }
    // 
    // public PutObjectResult upload(String key, String filePath) {
    // try {
    // File e = new File(filePath);
    // FileInputStream content = new FileInputStream(e);
    // ObjectMetadata meta = new ObjectMetadata();
    // meta.setContentLength(e.length());
    // return this.client.putObject(bucketname, key, content, meta);
    // } catch (Exception var7) {
    // throw new RuntimeException("putObject error!!!  key:" + key + "  filePath:" + filePath + "   error:" + var7);
    // }
    // }
    // 
    // public PutObjectResult changeJarStatus(String key, String status) {
    // ObjectMetadata objectMeta = new ObjectMetadata();
    // //        byte[] buffer = new byte[0];
    // ByteArrayInputStream in = new ByteArrayInputStream(status.getBytes());
    // objectMeta.setContentLength(status.getBytes().length);
    // PutObjectResult result = null;
    // try {
    // result = client.putObject(bucketname, key, in, objectMeta);
    // } finally {
    // try {
    // in.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // return result;
    // }
    // 
    // public boolean getJarStatus(String key) {
    // OSSObject object = null;
    // try {
    // object = client.getObject(bucketname, key);
    // } catch (OSSException e) {
    // return false;
    // } catch (ClientException e) {
    // return false;
    // }
    // InputStream objectContent = object.getObjectContent();
    // byte[] buf = new byte[(int) object.getObjectMetadata().getContentLength()];
    // try {
    // while (objectContent.read(buf) != -1) ;
    // } catch (IOException e) {
    // return false;
    // }
    // String status = new String(buf);
    // if ("false".equals(status)) {
    // return false;
    // }
    // return true;
    // }
    // 
    // public boolean hasJar(String key) {
    // OSSObject object = null;
    // try {
    // object = client.getObject(bucketname, key);
    // } catch (OSSException e) {
    // return false;
    // } catch (ClientException e) {
    // return false;
    // }
    // return true;
    // }
    // 
    // public void download(String ossKey, String destFilePath) {
    // try {
    // OSSObject e = this.client.getObject(bucketname, ossKey);
    // InputStream objectContent = e.getObjectContent();
    // FileOutputStream outputStream = new FileOutputStream(destFilePath);
    // byte[] bytes = new byte[4096];
    // 
    // int byteCount1;
    // while ((byteCount1 = objectContent.read(bytes)) != -1) {
    // outputStream.write(bytes, 0, byteCount1);
    // }
    // 
    // objectContent.close();
    // outputStream.close();
    // } catch (Exception var9) {
    // throw new RuntimeException("download error!!!  soureFilePath:" +
    // ossKey + "  destFilePath:" + destFilePath + "   error:" + var9);
    // }
    // }
    // 
    // public InputStream downloadInputStream(String ossKey) {
    // OSSObject e = this.client.getObject(bucketname, ossKey);
    // return  e.getObjectContent();
    // }
    // 
    // private void zipFile(File inFile, ZipOutputStream zos, String dir) throws IOException {
    // File[] entryName;
    // int len;
    // if (inFile.isDirectory()) {
    // entryName = inFile.listFiles();
    // File[] entry = entryName;
    // int is = entryName.length;
    // 
    // for (len = 0; len < is; ++len) {
    // File file = entry[len];
    // this.zipFile(file, zos, dir + "\\" + inFile.getName());
    // }
    // } else {
    // entryName = null;
    // System.out.println(inFile.getName());
    // String var9;
    // if (!"".equals(dir)) {
    // var9 = dir + "\\" + inFile.getName();
    // } else {
    // var9 = inFile.getName();
    // }
    // 
    // ZipEntry var10 = new ZipEntry(var9);
    // zos.putNextEntry(var10);
    // FileInputStream var11 = new FileInputStream(inFile);
    // boolean var12 = false;
    // 
    // while ((len = var11.read()) != -1) {
    // zos.write(len);
    // }
    // 
    // var11.close();
    // }
    // 
    // }
}
