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
package com.qlangtech.tis.realtime;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import com.qlangtech.tis.plugin.ds.DBConfig;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.realtime.yarn.TransferIncrContainer;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import com.qlangtech.tis.sql.parser.DBNode;
import com.qlangtech.tis.util.XStream2;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description: 增量工程启动类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TisIncrLauncher {

    public static boolean notDownload = Boolean.getBoolean("notdownloadjar");

    private final String collection;

    private final long timestamp;

    private List<DBNode> dbNodes;

    private static final Logger logger = LoggerFactory.getLogger(TisIncrLauncher.class);

    //private final boolean launchStatusReport;

//    public TisIncrLauncher(String collection, long timestamp) {
//        this(collection, timestamp, true);
//    }

    public TisIncrLauncher(String collection, long timestamp) {
        this.collection = collection;
        this.timestamp = timestamp;
    }

    public static void main(String[] args) throws Exception {
        // UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("Tis"));
        List<String> argsList = Arrays.asList(args);
        logger.info("args: " + argsList.toString());
        if (argsList.size() < 2) {
            throw new IllegalArgumentException("argslist is illegal:" + argsList.toString());
        }
        final String collection = argsList.get(0);
        final long timestamp = Long.parseLong(argsList.get(1));
        final TisIncrLauncher incrLauncher = new TisIncrLauncher(collection, timestamp);
        incrLauncher.downloadDependencyJarsAndPlugins();
        AbstractTisCloudSolrClient.initHashcodeRouter();
        // 启动增量任务
        incrLauncher.launchIncrChannel();
    }

    /**
     * 下载必要的资源(jar)
     */
    public void downloadDependencyJarsAndPlugins() throws Exception {
        if (notDownload) {
            return;
        }
        try {
            TIS.permitInitialize = false;
            final RunEnvironment runtime = RunEnvironment.getSysRuntime();
            /**
             * ==================================================================
             * 下载增量组件相关的配置及plugin包
             * ==================================================================
             */
            final String collectionRelativePath = TIS.KEY_TIS_PLUGIN_CONFIG + "/" + collection;
            // List<String> subFiles = CenterResource.getSubFiles(collectionRelativePath, false, true);
            // Lists.newArrayList();
            List<File> subs = CenterResource.synchronizeSubFiles(collectionRelativePath);
            // for (String f : subFiles) {
            // subs.add(CenterResource.copyFromRemote2Local(CenterResource.getPath(collectionRelativePath, f), true));
            // }
            // 下载plugins到指定位置
            // 需要下载那些plugin呢?
            Set<XStream2.PluginMeta> pluginMetas = TIS.loadIncrComponentUsedPlugin(collection, subs, true);
            for (XStream2.PluginMeta pmeta : pluginMetas) {
                // url = getPathURL(TIS.KEY_TIS_PLUGIN_ROOT + "/" + pmeta.getPluginPackageName());
                // pmeta.copyFromRemote(url, pmeta.getPluginPackageFile());
                pmeta.copyFromRemote();
            }
            logger.info("synchroniz remote plugin to local:" + pluginMetas.stream().map((r) -> r.getPluginPackageName()).collect(Collectors.joining(",")));
            /**
             * ==================================================================
             * 下载Stream Jar包
             * ==================================================================
             */
            // url = getPathURL(StreamContextConstant.DIR_STREAMS_SCRIPT + "/" + collection + "/" + timestamp + "/" + StreamContextConstant.getIncrStreamJarName(collection));
            // CenterResource.copyFromRemote2Local(url, StreamContextConstant.getIncrStreamJarFile(collection, timestamp));
            CenterResource.copyFromRemote2Local(StreamContextConstant.DIR_STREAMS_SCRIPT + "/" + collection + "/" + timestamp + "/" + StreamContextConstant.getIncrStreamJarName(collection), true);
            /**
             * ==================================================================
             * 下载DAO依赖元数据
             * ==================================================================
             */
            // url = getPathURL(StreamContextConstant.getDbDependencyConfigFilePath(collection, timestamp));
            // CenterResource.copyFromRemote2Local(url, dbDependencyConfigMetaFile);
            CenterResource.copyFromRemote2Local(StreamContextConstant.getDbDependencyConfigFilePath(collection, timestamp), true);
            File dbDependencyConfigMetaFile = StreamContextConstant.getDbDependencyConfigMetaFile(collection, timestamp);
            try (InputStream stream = FileUtils.openInputStream(dbDependencyConfigMetaFile)) {
                this.dbNodes = DBNode.load(stream);
            }
            /**
             * ==================================================================
             * 将jar包保存到本地
             * ==================================================================
             */
            for (DBNode n : dbNodes) {
                CenterResource.copyFromRemote2Local(
                        StreamContextConstant.getDAORootPath(n.getDbName(), n.getTimestampVer()) + "/" + DBConfig.getDAOJarName(n.getDbName()), true);
            }
        } finally {
            TIS.permitInitialize = true;
        }
    }

    // private void copyFromRemote(final URL url, final File local) {
    // 
    // HttpUtils.get(url, new ConfigFileContext.StreamProcess<Void>() {
    // 
    // @Override
    // public Void p(int status, InputStream stream, Map<String, List<String>> headerFields) {
    // Optional<String> first = null;
    // List<String> lastupdate = headerFields.get(ConfigFileContext.KEY_HEAD_LAST_UPDATE);
    // if (lastupdate == null || !(first = lastupdate.stream().findFirst()).isPresent()) {
    // throw new IllegalStateException("url:" + url + " can not find " + ConfigFileContext.KEY_HEAD_LAST_UPDATE + " in headers");
    // }
    // long lastUpdate = Long.parseLong(first.get());
    // if (local.exists()) {
    // if (lastUpdate <= local.lastModified()) {
    // return null;
    // }
    // }
    // try {
    // FileUtils.copyInputStreamToFile(stream, local);
    // local.setLastModified(lastUpdate);
    // } catch (IOException e) {
    // throw new RuntimeException("local file:" + local.getAbsolutePath(), e);
    // }
    // return null;
    // }
    // });
    // }
    public BeanFactory launchIncrChannel() throws Exception {
        if (this.dbNodes == null) {
            try (InputStream input = FileUtils.openInputStream(StreamContextConstant.getDbDependencyConfigMetaFile(collection, timestamp))) {
                this.dbNodes = DBNode.load(input);
            }
        }
        File incrScriptJar = StreamContextConstant.getIncrStreamJarFile(collection, timestamp);
        List<URL> jarUrls = this.dbNodes.stream().map((r) -> {
            File daoJarFile = StreamContextConstant.getDAOJarFile(r);
            return file2URL(daoJarFile);
        }).collect(Collectors.toList());
        jarUrls.add(file2URL(incrScriptJar));
        StringBuffer urlBuffer = new StringBuffer("\ndependency local jars:");
        for (URL u : jarUrls) {
            urlBuffer.append("\n" + u.toString());
        }
        urlBuffer.append("\n end jars");
        logger.info(urlBuffer.toString());
        // Map<String, DBConfig> dbConfigsMap = this.dbNodes.stream().collect(
        // Collectors.toMap((db) -> db.getDbName(), (db) -> GitUtils.$().getDbLinkMetaData(db.getDbName(), DbScope.FACADE)));//.collect(Collectors.toMap()).collect(Collectors.toList());
        URLClassLoader classLoader = new // 
                TISIncrClassLoader(jarUrls.toArray(new URL[jarUrls.size()]), TisIncrLauncher.class.getClassLoader());
        TransferIncrContainer incrContainer = new TransferIncrContainer(this.collection, timestamp, classLoader);
        incrContainer.start();
        return incrContainer.getSpringBeanFactory();
    }

    public static class TISIncrClassLoader extends URLClassLoader {

        public TISIncrClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        // AliasGroovyClassLoader会尝试用几个加了前缀的类名来加载，如果加载不到则抛异常是不可取的
        // @Override
        // protected Class<?> findClass(String name) throws ClassNotFoundException {
        // try {
        // return super.findClass(name);
        // } catch (ClassNotFoundException e) {
        // URL[] urls = this.getURLs();
        // StringBuffer b = new StringBuffer();
        // for (URL u : urls) {
        // b.append("\n" + u.toString());
        // }
        // throw new RuntimeException("missing find:" + name + "\n>>" + b.toString(), e);
        // }
        // 
        // }
    }

    // public static URL getPathURL(String filePath) {
    // try {
    // final RunEnvironment runtime = RunEnvironment.getSysRuntime();
    // return new URL(runtime.getInnerRepositoryURL() + "/config/stream_script_repo.action?path=" + URLEncoder.encode(filePath, TisUTF8.getName()));
    // } catch (Exception e) {
    // throw new RuntimeException("filepath:" + filePath, e);
    // }
    // }
    // public static List<DBNode> getDbDependencies() {
    // List<DBNode> dbs = Lists.newArrayList();
    // dbs.add(new DBNode("cardcenter", 1).setTimestampVer(20190816150331l));
    // dbs.add(new DBDependency("member", 20190816145323l));
    // dbs.add(new DBDependency("order", 20170909125017l));
    // dbs.add(new DBDependency("shop", 20190816171631l));
    // return dbs;
    // }
    private static URL file2URL(File f) {
        if (!f.exists()) {
            throw new IllegalArgumentException("file is not exist:" + f.getAbsolutePath());
        }
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
