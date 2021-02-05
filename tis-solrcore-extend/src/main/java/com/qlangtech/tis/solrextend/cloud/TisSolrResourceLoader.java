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
package com.qlangtech.tis.solrextend.cloud;

import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.SolrClassLoader;
import org.apache.solr.core.*;
import org.apache.solr.handler.IndexFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrResourceLoader extends SolrResourceLoader {

    private final String collectionName;

    private final File solrhomeDir;

    public static final Map<String, PropteryGetter> configFileNames;

    private static final Logger log = LoggerFactory.getLogger(TisSolrResourceLoader.class);

    private TISPluginClassLoader pluginClassLoader;

    static {
        Map<String, PropteryGetter> names = new HashMap<String, PropteryGetter>();
        for (PropteryGetter getter : ConfigFileReader.getConfigList()) {
            names.put(getter.getFileName(), getter);
        }
        configFileNames = Collections.unmodifiableMap(names);
    }

    private String coreNodeName;
    private UUID coreUUId;

    public TisSolrResourceLoader(Path instanceDir, ClassLoader parent, String collectionName) {
        super(instanceDir, parent);
        this.collectionName = collectionName;
        this.solrhomeDir = SolrPaths.locateSolrHome().toFile();
    }

    @Override
    public void inform(SolrCore core) {
        super.inform(core);
        this.coreNodeName = core.getName();
        this.coreUUId = core.uniqueId;
    }

    public SolrClassLoader getSchemaLoader() {
        super.getSchemaLoader();
        if (pluginClassLoader == null) {
            this.pluginClassLoader = new TISPluginClassLoader(this.collectionName, this.getCoreContainer(), this, () -> {
                if (getCoreContainer() == null || getSolrConfig() == null || coreNodeName == null || coreUUId == null)
                    return;
                try (SolrCore c = getCoreContainer().getCore(coreNodeName, coreUUId)) {
                    if (c != null) {
                        c.fetchLatestSchema();
                    }
                }
            });
        }
        return this.pluginClassLoader;
    }

    @Override
    public String getConfigDir() {
        // throw new UnsupportedOperationException();
        return "repository:" + Config.getConfigRepositoryHost();
    }

    @Override
    public String[] listConfigDir() {
        return new String[]{};
    }

    @Override
    public InputStream openResource(String resource) throws IOException {
        // 希望在服务端不需要校验schema的正确性
        if (StringUtils.equals("dtd/solrschema.dtd", resource)) {
            return new ByteArrayInputStream(new byte[0]);
        }
        PropteryGetter getter = null;
        if ((getter = configFileNames.get(resource)) != null) {
            return loadConfigResource(getter, getConfigSnapshotId(getCollectionConfigDir(this.solrhomeDir, this.collectionName)));
        }
        InputStream is = null;
        try {
            // delegate to the class loader (looking into $INSTANCE_DIR/lib
            // jars)
            is = classLoader.getResourceAsStream(resource.replace(File.separatorChar, '/'));
        } catch (Exception e) {
            throw new IOException("Error opening " + resource, e);
        }
        if (is == null) {
            throw new SolrResourceNotFoundException("Can't find resource '" + resource + "' in classpath or '"
                    + collectionName + "', cwd=" + System.getProperty("user.dir"));
        }
        return is;
    }

    /**
     * 下载配置资源，如果本地已经有这个配置文件，那直接从本地加载就行了<br>
     * 如果本地没有文件，那就要从远端下载下来
     *
     * @param
     * @param snapshotid ，期望
     * @return
     * @throws IOException
     * @throws SolrResourceNotFoundException
     */
    private InputStream loadConfigResource(PropteryGetter getter, final long snapshotid) throws IOException, SolrResourceNotFoundException {
        // TSearcherConfigFetcher configFetcher = TSearcherConfigFetcher.get();
        synchronized (TisSolrResourceLoader.class) {
            File collectionConfigDir = getCollectionConfigDir(this.solrhomeDir, this.collectionName);
            File configFile = null;
            try {
                configFile = new File(collectionConfigDir, getter.getFileName() + snapshotid);
                if (snapshotid > 0 && configFile.exists()) {
                    return FileUtils.openInputStream(configFile);
                }
            } catch (IOException e) {
                throw new SolrException(ErrorCode.SERVER_ERROR, "can not find configfile:"
                        + getter.getFileName() + snapshotid + " in " + collectionConfigDir.getAbsolutePath(), e);
            }
            try {
                SnapshotDomain snapshotDomain = downConfigFromConsoleRepository(snapshotid, this.collectionName
                        , collectionConfigDir, configFileNames.values().toArray(new PropteryGetter[0]), true);
                // 返回需要的文件
                return new ByteArrayInputStream(getter.getContent(snapshotDomain));
            } catch (RepositoryException e1) {
                throw new SolrResourceNotFoundException("repository:" + Config.getConfigRepositoryHost() + ",collection:" + collectionName, e1);
            }
        }
    }

    /**
     * 取得远端仓库中的snapshotid
     *
     * @param collectionName
     * @return
     * @throws Exception
     */
    public static int getRemoteSnapshotId(String collectionName) throws Exception {
        SnapshotDomain snapshotDomain = downConfigFromConsoleRepository(-1, collectionName, null, new PropteryGetter[]{ConfigFileReader.FILE_SOLR}, false);
        return snapshotDomain.getSnapshot().getSnId();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getRemoteSnapshotId("search4totalpay"));
    }

    /**
     * 下载配置并且写入本地文件系统
     *
     * @param targetSnapshotid
     * @param collectionName
     * @param coreContainer
     * @throws RepositoryException
     * @throws IOException
     */
    public static void downConfigFromConsoleRepository(final long targetSnapshotid, String collectionName
            , CoreContainer coreContainer) throws RepositoryException, IOException {
        File collectionDir = TisSolrResourceLoader.getCollectionConfigDir(new File(coreContainer.getSolrHome()), collectionName);
        downConfigFromConsoleRepository(targetSnapshotid, collectionName, collectionDir, ConfigFileReader.getAry, true);
    }

    /**
     * @param targetSnapshotid    ,配置文件目标版本，期望得到的版本
     * @param
     * @param collectionConfigDir
     * @return
     * @throws RepositoryException
     * @throws IOException
     */
    public static SnapshotDomain downConfigFromConsoleRepository(final long targetSnapshotid, String collectionName
            , File collectionConfigDir, PropteryGetter[] fileGetter, boolean modifyFileSys) throws RepositoryException, IOException {
        // TSearcherConfigFetcher configFetcher = TSearcherConfigFetcher.get();
        SnapshotDomain snapshotDomain = HttpConfigFileReader.getResource(collectionName, targetSnapshotid, RunEnvironment.getSysRuntime(), fileGetter);
        // 期望下载的 和远端的配置文件版本不同则肯定有问题了，需要抛出异常
        if (targetSnapshotid > 0 && snapshotDomain.getSnapshot().getSnId() != targetSnapshotid) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "local config snapshotid:" + targetSnapshotid
                    + ",config repository snapshotid:" + snapshotDomain.getSnapshot().getSnId() + " is not match");
        }
        if (// && localSnapshotid < 1
                modifyFileSys) {
            // 修改配置文件
            saveConfigFileSnapshotId(collectionConfigDir, snapshotDomain.getSnapshot().getSnId());
        }
        // 将文件存入本地
        UploadResource content = null;
        if (modifyFileSys) {
            for (PropteryGetter g : fileGetter) {
                content = g.getUploadResource(snapshotDomain);
                if (content != null && content.getContent() != null) {
                    File newFile = new File(collectionConfigDir, g.getFileName() + snapshotDomain.getSnapshot().getSnId());
                    // 只有不存在的情况下才会去更新
                    if (!newFile.exists()) {
                        FileUtils.writeByteArrayToFile(newFile, content.getContent(), false);
                    }
                }
            }
        }
        return snapshotDomain;
    }

    /**
     * @return
     */
    public static File getCollectionConfigDir(File solrhomeDir, String collectionName) {
        // File parent = instanceDir.toFile().getParentFile();
        File collectionConfigDir = new File(solrhomeDir, "configsets" + File.separator + collectionName);
        try {
            if (!collectionConfigDir.exists()) {
                // throw new IllegalStateException("collectionConfigDir:" + collectionConfigDir.getAbsolutePath() + " is not exist");
                FileUtils.forceMkdir(collectionConfigDir);
            }
            return collectionConfigDir;
        } catch (IOException e) {
            throw new RuntimeException(collectionConfigDir.getAbsolutePath(), e);
        }
    }

    private static final String configsnapshotid = "configsnapshotid";

    public static final String CONFIG_FILE_NAME = "config.properties";

    /**
     * If the index is stale by any chance, load index from a different dir in
     * the data dir.
     */
    public static void saveConfigFileSnapshotId(File collectionConfigDir, long snapshotId) {
        log.info("New index installed. Updating index properties... config snapshotid=" + snapshotId);
        File configFile = new File(collectionConfigDir, CONFIG_FILE_NAME);
        Properties p = new Properties();
        try {
            if (configFile.exists()) {
                final InputStream is = FileUtils.openInputStream(configFile);
                try {
                    p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.error("Unable to load " + IndexFetcher.INDEX_PROPERTIES, e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
                try {
                    // dir.deleteFile(IndexFetcher.INDEX_PROPERTIES);
                    FileUtils.forceDelete(configFile);
                } catch (IOException e) {
                    // no problem
                }
            }
            p.put(configsnapshotid, String.valueOf(snapshotId));
            Writer os = null;
            try {
                os = new OutputStreamWriter(FileUtils.openOutputStream(configFile, false), StandardCharsets.UTF_8);
                p.store(os, IndexFetcher.INDEX_PROPERTIES);
            } catch (Exception e) {
                throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unable to write " + IndexFetcher.INDEX_PROPERTIES, e);
            } finally {
                IOUtils.closeQuietly(os);
            }
            // return true;
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        } finally {
        }
    }

    /**
     * 取得现在目录下的配置文件版本
     *
     * @param collectionConfigDir
     * @return
     */
    public static int getConfigSnapshotId(final File collectionConfigDir) {
        // 先要尝试到本地目录中去找文件
        if (!collectionConfigDir.exists()) {
            collectionConfigDir.mkdirs();
            return 0;
        }
        File propFile = new File(collectionConfigDir, CONFIG_FILE_NAME);
        if (!propFile.exists()) {
            return 0;
        }
        Properties p = new Properties();
        try (final InputStream is = FileUtils.openInputStream(propFile)) {
            p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            return Integer.parseInt(p.getProperty(configsnapshotid));
        } catch (Exception e) {
            log.error("Unable to load " + IndexFetcher.INDEX_PROPERTIES, e);
        }
        return 0;
    }
}
