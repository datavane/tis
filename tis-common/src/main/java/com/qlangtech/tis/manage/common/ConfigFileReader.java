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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.pubhook.common.ConfigConstant;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import junit.framework.Assert;

/*
 * 读取solrcore相关的配置文件，该类是读取本地文件<br>
 * applicationContext.xml schema.xml 等
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ConfigFileReader {

    public static final String ENCODE = "utf8";

    private final SnapshotDomain snapshot;

    private final URL appDomainDir;

    public static void main(String[] arg) throws Exception {
    }

    protected URL getAppDomainDir() {
        return appDomainDir;
    }

    public SnapshotDomain getSnapshot() {
        return snapshot;
    }

    public ConfigFileReader(SnapshotDomain snapshot, File appDomainDir) {
        this(snapshot, convert2URL(appDomainDir));
    }

    /**
     * @param snapshot
     * @param appDomainDir
     *            保存根目錄
     */
    public ConfigFileReader(SnapshotDomain snapshot, URL appDomainDir) {
        super();
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot can not be null");
        }
        if (appDomainDir == null) {
            throw new IllegalArgumentException("appDomainDir can not be null");
        }
        this.snapshot = snapshot;
        this.appDomainDir = appDomainDir;
    }

    /**
     * 从本地文件系统中读取
     *
     * @param pGetter
     * @return
     * @throws Exception
     */
    public InputStream read(PropteryGetter pGetter) throws Exception {
        byte[] content = getContent(pGetter);
        if (!StringUtils.equalsIgnoreCase(md5file(content), pGetter.getMd5CodeValue(snapshot))) {
            throw new IllegalArgumentException("path：" + getPath(pGetter) + "has been modify");
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * 取得文件的绝对路径
     *
     * @param pGetter
     * @return
     */
    public String getPath(PropteryGetter pGetter) {
        return this.getFile(pGetter).getAbsolutePath();
    }

    public byte[] getContent(PropteryGetter getter) {
        InputStream reader = null;
        try {
            reader = new FileInputStream(this.getFile(getter));
            return IOUtils.toByteArray(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (Throwable e) {
            }
        }
    }

    public final File getFile(PropteryGetter pGetter) {
        return getNewFile(pGetter, pGetter.getFileSufix(snapshot));
    }

    public File getNewFile(PropteryGetter pGetter, Long fileSufix) {
        try {
            return getNewFile(this.getAppDomainDir().toURI(), pGetter, fileSufix);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected File getNewFile(URI appDomainDir, PropteryGetter pGetter, Long fileSufix) {
        if (pGetter == null) {
            throw new IllegalArgumentException("pGetter can not be null");
        }
        // try {
        File dir = new File(appDomainDir);
        return new File(dir, pGetter.getFileName() + (fileSufix == null ? StringUtils.EMPTY : String.valueOf(fileSufix)));
    // } catch (URISyntaxException e) {
    // throw new RuntimeException(e);
    // }
    }

    public String saveFile(final InputStream reader, PropteryGetter getter, Long fileSufix) throws FileNotFoundException, IOException {
        OutputStream writer = null;
        File saveFile = this.getNewFile(getter, fileSufix);
        try {
            writer = new FileOutputStream(saveFile);
            IOUtils.copy(reader, writer);
        } finally {
            try {
                writer.close();
            } catch (Throwable e) {
            }
            try {
                reader.close();
            } catch (Throwable e) {
            }
        }
        return md5file(saveFile);
    }

    public static String md5file(final File saveFile) throws FileNotFoundException, IOException {
        InputStream savedStream = null;
        try {
            savedStream = new FileInputStream(saveFile);
            // 保存的文件的签名
            return md5file(IOUtils.toByteArray(savedStream));
        } finally {
            try {
                savedStream.close();
            } catch (Throwable e) {
            }
        }
    }

    public static String md5file(byte[] content) {
        // try {
        return (DigestUtils.md5Hex(content));
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    }

    public static File getAppDomainDir(File localRepository, Integer bizid, Integer appid) {
        Assert.assertNotNull("bizid can not be null", bizid);
        Assert.assertNotNull("appid can not be null", appid);
        File saveDir = new File(localRepository, String.valueOf(bizid) + File.separatorChar + appid);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            throw new IllegalStateException("dir:" + saveDir.getAbsolutePath() + " can not be create");
        }
        return saveDir;
    }

    private static URL convert2URL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final PropteryGetter FILE_APPLICATION = new BasicPropteryGetter() {

        public String getFileName() {
            return ConfigConstant.FILE_APPLICATION;
        }

        public Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon) {
            colon.setResApplicationId((long) newUploadResourceId);
            return colon;
        }

        public UploadResource getUploadResource(SnapshotDomain snapshotDomain) {
            return snapshotDomain.getApplication();
        }
    };

    public static final BasicPropteryGetter FILE_SCHEMA = new SchemaFile();

    // public final static DocumentBuilderFactory schemaDocumentBuilderFactory =
    // DocumentBuilderFactory
    // .newInstance(
    // "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
    // ConfigFileReader.class.getClassLoader());
    // static {
    // schemaDocumentBuilderFactory.setValidating(true);
    // }
    private static final SolrFieldsParser fieldsParser = new SolrFieldsParser();

    private static class SchemaFile extends BasicPropteryGetter {

        public String getFileName() {
            return ConfigConstant.FILE_SCHEMA;
        }

        @Override
        public ConfigFileValidateResult validate(UploadResource resource) {
            Assert.assertNotNull("resource can not be null", resource);
            // 校验schema 文件是否合法
            byte[] content = resource.getContent();
            ByteArrayInputStream inputStream = null;
            final ConfigFileValidateResult result = new ConfigFileValidateResult();
            try {
                inputStream = new ByteArrayInputStream(content);
                // DocumentBuilder builder = schemaDocumentBuilderFactory
                // .newDocumentBuilder();
                // builder.setEntityResolver(new EntityResolver() {
                // @Override
                // public InputSource resolveEntity(String publicId,
                // String systemId) throws SAXException, IOException {
                // final URL url = new URL(
                // "http://tisrepository/dtd/solrschema.dtd");
                // return new InputSource(new ByteArrayInputStream(
                // ConfigFileContext.processContent(url,
                // new StreamProcess<byte[]>() {
                // @Override
                // public byte[] p(int status,
                // InputStream stream,
                // String md5) {
                // try {
                // return IOUtils
                // .toByteArray(stream);
                // } catch (IOException e) {
                // throw new RuntimeException(
                // e);
                // }
                // }
                // })));
                // }
                // });
                // final DefaultHandler mh = new DefaultHandler() {
                // public void error(SAXParseException e) throws SAXException {
                // result.setValid(false);
                // result.appendResult("linenumber:" + e.getLineNumber()
                // + " " + e.getMessage() + "<br/>");
                // }
                // 
                // public void fatalError(SAXParseException e)
                // throws SAXException {
                // this.error(e);
                // }
                // };
                // builder.setErrorHandler(mh);
                // FIXME:先吧校验过滤掉
                ParseResult parseResult = fieldsParser.parseSchema((inputStream), true);
                if (!parseResult.isValid()) {
                    result.setValid(false);
                    for (String error : parseResult.errlist) {
                        result.appendResult(error);
                    }
                }
            } catch (Exception e) {
                result.setValid(false);
                result.appendResult(getErrorContent(e));
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            return result;
        }

        @Override
        public Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon) {
            colon.setResSchemaId(newUploadResourceId);
            return colon;
        }

        @Override
        public UploadResource getUploadResource(SnapshotDomain snapshotDomain) {
            return snapshotDomain.getSolrSchema();
        }
    }

    public static String getErrorContent(Throwable e) {
        StringWriter reader = new StringWriter();
        PrintWriter errprint = null;
        try {
            errprint = new PrintWriter(reader);
            e.printStackTrace(errprint);
            return StringUtils.trimToEmpty(reader.toString()).replaceAll("(\r|\n|\t)+", "<br/>");
        } finally {
            IOUtils.closeQuietly(errprint);
        }
    }

    public static final PropteryGetter FILE_SOLOR = new BasicPropteryGetter() {

        public String getFileName() {
            return ConfigConstant.FILE_SOLOR;
        }

        @Override
        public Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon) {
            colon.setResSolrId(newUploadResourceId);
            return colon;
        }

        @Override
        public UploadResource getUploadResource(SnapshotDomain snapshotDomain) {
            return snapshotDomain.getSolrConfig();
        }
    };

    public static final PropteryGetter FILE_DATA_SOURCE = new BasicPropteryGetter() {

        public String getFileName() {
            return ConfigConstant.FILE_DATA_SOURCE;
        }

        @Override
        public Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon) {
            colon.setResDsId(newUploadResourceId);
            return colon;
        }

        @Override
        public UploadResource getUploadResource(SnapshotDomain snapshotDomain) {
            return snapshotDomain.getDatasource();
        }
    };

//    public static final PropteryGetter FILE_JAR = new BasicPropteryGetter() {
//
//        public String getFileName() {
//            return ConfigConstant.FILE_JAR;
//        }
//
//        @Override
//        public Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon) {
//            colon.setResJarId(newUploadResourceId);
//            return colon;
//        }
//
//        @Override
//        public UploadResource getUploadResource(SnapshotDomain snapshotDomain) {
//            return snapshotDomain.getJarFile();
//        }
//    };

//    public static final PropteryGetter FILE_CORE_PROPERTIES = new BasicPropteryGetter() {
//
//        public String getFileName() {
//            return ConfigConstant.FILE_CORE_PROPERTIES;
//        }
//
//        @Override
//        public Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon) {
//            colon.setResCorePropId(newUploadResourceId);
//            return colon;
//        }
//
//        @Override
//        public UploadResource getUploadResource(SnapshotDomain snapshotDomain) {
//            return snapshotDomain.getCoreProp();
//        }
//    };

    public abstract static class BasicPropteryGetter implements PropteryGetter {

        public Long getFileSufix(SnapshotDomain snapshot) {
            return (long) snapshot.getSnapshot().getSnId();
        }

        @Override
        public ConfigFileValidateResult validate(UploadResource domain) {
            // 文件合法
            return validate(domain.getContent());
        }

        @Override
        public ConfigFileValidateResult validate(byte[] resource) {
            return new ConfigFileValidateResult();
        }

        public final String getMd5CodeValue(SnapshotDomain snapshot) {
            UploadResource resource = getUploadResource(snapshot);
            if (resource == null) {
                return StringUtils.EMPTY;
            }
            return resource.getMd5Code();
        }

        public final byte[] getContent(SnapshotDomain snapshot) {
            UploadResource resource = getUploadResource(snapshot);
            if (resource == null) {
                return null;
            }
            byte[] content = resource.getContent();
            if (content == null) {
                return null;
            }
            // 校验文件是否被篡改
            if (!StringUtils.equalsIgnoreCase(md5file(content), getMd5CodeValue(snapshot))) {
                throw new IllegalArgumentException("snapshot：" + snapshot.getSnapshot().getSnId() + " file:" + getFileName() + "has been modify");
            }
            return content;
        }

        // @Override
        // public Snapshot createNewSnapshot(Integer newId, SnapshotDomain
        // domain) {
        // 
        // return null;
        // }
        @Override
        public final Snapshot createNewSnapshot(Integer newUploadResourceId, Snapshot snapshot) {
            // Snapshot snapshot = domain.getSnapshot();
            Snapshot colon = new Snapshot();
            if (snapshot != null) {
                
                colon.setMemo(snapshot.getMemo());
                colon.setSnId(snapshot.getSnId());
                colon.setAppId(snapshot.getAppId());
                colon.setCreateTime(new Date());
                colon.setCreateUserId(snapshot.getCreateUserId());
                colon.setCreateUserName(snapshot.getCreateUserName());
                // colon.setPid(snapshot.getPid());
                colon.setPreSnId(snapshot.getSnId());
                colon.setResApplicationId(snapshot.getResApplicationId());
                colon.setResCorePropId(snapshot.getResCorePropId());
                colon.setResDsId(snapshot.getResDsId());
                colon.setResJarId(snapshot.getResJarId());
                final Long schemaId = snapshot.getResSchemaId();
                colon.setResSchemaId(schemaId);
                colon.setResSolrId(snapshot.getResSolrId());
            }
            return setSolrCoreResourceId((long) newUploadResourceId, colon);
        // return colon;
        }

        public abstract Snapshot setSolrCoreResourceId(long newUploadResourceId, Snapshot colon);
    }

    public static PropteryGetter createPropertyGetter(String resName) {
        for (PropteryGetter geter : getAry) {
            if (StringUtils.equals(geter.getFileName(), resName)) {
                return geter;
            }
        }
        throw new IllegalStateException("res name:" + resName + " is illegal");
    }

  //  public static final PropteryGetter[] getAry = new PropteryGetter[] { FILE_APPLICATION, FILE_SCHEMA, FILE_SOLOR, FILE_DATA_SOURCE, FILE_JAR, FILE_CORE_PROPERTIES };
    
    public static final PropteryGetter[] getAry = new PropteryGetter[] {  FILE_SCHEMA, FILE_SOLOR };

    public static List<PropteryGetter> getConfigList() {
        return Arrays.asList(getAry);
    }
}
