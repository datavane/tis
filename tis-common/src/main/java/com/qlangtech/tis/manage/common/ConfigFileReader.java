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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.pubhook.common.ConfigConstant;
import com.qlangtech.tis.solrdao.IFieldTypeFactory;
import com.qlangtech.tis.solrdao.ISchemaPluginContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.impl.ParseResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 读取solrcore相关的配置文件，该类是读取本地文件<br>
 * applicationContext.xml schema.xml 等
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-22
 */
public class ConfigFileReader {

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
     * @param appDomainDir 保存根目錄
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

    public String saveFile(final InputStream reader, PropteryGetter getter, Long fileSufix) throws IOException {
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

    public static String md5file(final File saveFile) throws IOException {
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
        Objects.requireNonNull(bizid, "bizid can not be null");
        Objects.requireNonNull(appid, "appid can not be null");
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

    public static final BasicPropteryGetter FILE_SCHEMA = new SchemaFile();

    private static class SchemaFile extends BasicPropteryGetter {

        public String getFileName() {
            return ConfigConstant.FILE_SCHEMA;
        }

        @Override
        public ConfigFileValidateResult validate(ISchemaPluginContext schemaPlugin, UploadResource resource) {
            // Assert.assertNotNull("resource can not be null", );
            Objects.requireNonNull(resource, "resource can not be null");
            // 校验schema 文件是否合法
            IFieldTypeFactory ftFactory = null;
            final byte[] content = resource.getContent();
            final ConfigFileValidateResult result = new ConfigFileValidateResult();
            try {
                IIndexMetaData meta = SolrFieldsParser.parse(() -> content, schemaPlugin, true);
                ParseResult parseResult = meta.getSchemaParseResult();


                for (SolrFieldsParser.SolrType ftype : parseResult.getFieldTypes()) {
                    if (ftype.plugin && (ftFactory = schemaPlugin.findFieldTypeFactory(ftype.getPluginName())) == null) {
                        parseResult.errlist.add("fieldType:" + ftype.getSType().getName() + " relevant fieldType plugin has not find plugin in plugin define collection");
                    }
                }

                if (!parseResult.isValid()) {
                    result.setValid(false);
                    for (Object error : parseResult.errlist) {
                        result.appendResult(error.toString());
                    }
                }
            } catch (Exception e) {
                result.setValid(false);
                result.appendResult(getErrorContent(e));
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

    public static final PropteryGetter FILE_SOLR = new BasicPropteryGetter() {

        public String getFileName() {
            return ConfigConstant.FILE_SOLR;
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

    public abstract static class BasicPropteryGetter implements PropteryGetter {

        public Long getFileSufix(SnapshotDomain snapshot) {
            return (long) snapshot.getSnapshot().getSnId();
        }

        @Override
        public ConfigFileValidateResult validate(ISchemaPluginContext schemaFieldTypeContext, UploadResource domain) {
            // 文件合法
            return validate(schemaFieldTypeContext, domain.getContent());
        }

        @Override
        public ConfigFileValidateResult validate(ISchemaPluginContext schemaFieldTypeContext, byte[] resource) {
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

    public static final PropteryGetter[] getAry = new PropteryGetter[]{
            FILE_SCHEMA, FILE_SOLR};

    public static List<PropteryGetter> getConfigList() {
        return Arrays.asList(getAry);
    }
}
