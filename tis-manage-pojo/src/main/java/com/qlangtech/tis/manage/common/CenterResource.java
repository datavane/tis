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

import com.google.common.collect.Lists;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 中央资源
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-26 21:14
 */
public class CenterResource {

    public static final String KEY_LAST_MODIFIED_EXTENDION = ".lastmodified";

    private static final String KEY_notFetchFromCenterRepository = "notFetchFromCenterRepository";

    public static boolean notFetchFromCenterRepository() {
        return Boolean.getBoolean(KEY_notFetchFromCenterRepository);
    }

    public static void setNotFetchFromCenterRepository() {
        System.setProperty(KEY_notFetchFromCenterRepository, String.valueOf(true));
    }

    private static void copyFromRemote2Local(final URL url, final File local) {
        copyFromRemote2Local(url, local, false);
    }

    /**
     * 远程文件拷贝到本地
     *
     * @param filePath
     */
    public static File copyFromRemote2Local(String filePath, boolean isConfig) {
        URL url = getPathURL((isConfig ? Config.SUB_DIR_CFG_REPO : Config.SUB_DIR_LIBS) + "/" + filePath);
        File local = new File(isConfig ? Config.getMetaCfgDir() : Config.getLibDir(), filePath);
        copyFromRemote2Local(url, local, false);
        return local;
    }

    public static List<String> getSubDirs(final String relativePath) {
        return getSubFiles(relativePath, true, false);
    }

    /**
     * 同步配置自文件
     *
     * @param relativePath
     * @return
     */
    public static List<File> synchronizeSubFiles(String relativePath) {
        List<String> subFiles = CenterResource.getSubFiles(relativePath, false, true);
        List<File> subs = Lists.newArrayList();
        for (String f : subFiles) {
            subs.add(CenterResource.copyFromRemote2Local(CenterResource.getPath(relativePath, f), true));
        }
        return subs;
    }

    /**
     * @param
     * @param dir  取子文件的目录
     * @param file 取子文件
     * @return
     */
    public static List<String> getSubFiles(String relativePath, boolean dir, boolean file) {
        // 是否取本地文件
        if (notFetchFromCenterRepository()) {
            File parent = new File(Config.getMetaCfgDir(), relativePath);
            if (!parent.exists()) {
                throw new IllegalStateException("parent:" + parent.getAbsolutePath() + " is not exist");
            }
            File c = null;
            List<String> suNames = Lists.newArrayList();
            for (String child : parent.list()) {
                c = new File(parent, child);
                if ((c.isFile() && file) || (c.isDirectory() && dir)) {
                    suNames.add(child);
                }
            }
            return suNames;
        }
        final URL url = getPathURL(Config.SUB_DIR_CFG_REPO, relativePath);
        List<String> subDirs = Lists.newArrayList();
        subDirs.addAll(HttpUtils.get(url, new ConfigFileContext.StreamProcess<List<String>>() {

            @Override
            public List<ConfigFileContext.Header> getHeaders() {
                return HEADER_GET_META;
            }

            @Override
            public List<String> p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                List<String> subChild = headerFields.get(ConfigFileContext.KEY_HEAD_FILES);
                Optional<String> first = subChild.stream().findFirst();
                if (!first.isPresent()) {
                    return Collections.emptyList();
                } else {
                    List<String> result = Lists.newArrayList();
                    String[] childs = StringUtils.split(first.get(), ",");
                    for (String c : childs) {
                        // 说明是文件夹
                        if (dir && StringUtils.endsWith(c, ":d")) {
                            result.add(StringUtils.substringBefore(c, ":"));
                        }
                        if (file && StringUtils.endsWith(c, ":f")) {
                            result.add(StringUtils.substringBefore(c, ":"));
                        }
                    }
                    return result;
                }
            }
        }));
        return subDirs;
    }

    /**
     * @param url
     * @param local
     * @param directDownload 取得目标文件的元数据信息，比如最新更新时间
     * @return 是否已经更新本地文件
     */
    public static boolean copyFromRemote2Local(final URL url, final File local, boolean directDownload) {
        if (notFetchFromCenterRepository()) {
            return false;
        }
        final File lastModifiedFile = new File(local.getParentFile(), local.getName() + KEY_LAST_MODIFIED_EXTENDION);
        if (!directDownload) {
            boolean shallWriteLocal = HttpUtils.get(url, new ConfigFileContext.StreamProcess<Boolean>() {
                @Override
                public List<ConfigFileContext.Header> getHeaders() {
                    return HEADER_GET_META;
                }

                @Override
                public Boolean p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    return shallWriteLocal(headerFields, url, local, lastModifiedFile);
                }
            });
            if (!shallWriteLocal) {
                return false;
            }
        }
        return HttpUtils.get(url, new ConfigFileContext.StreamProcess<Boolean>() {

            @Override
            public Boolean p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                if (!shallWriteLocal(headerFields, url, local, lastModifiedFile)) {
                    return false;
                }
                long lastUpdate = getLastUpdateTimeStamp(headerFields, url);
                try {
                    FileUtils.copyInputStreamToFile(stream, local);
                } catch (IOException e) {
                    throw new RuntimeException("local file:" + local.getAbsolutePath(), e);
                }
                try {
                    FileUtils.write(lastModifiedFile, String.valueOf(lastUpdate), TisUTF8.get());
                } catch (IOException e) {
                    throw new RuntimeException("can not write:" + lastUpdate + " to lastModifiedFile:" + lastModifiedFile.getAbsolutePath(), e);
                }
                return true;
            }
        });
    }

    /**
     * 根据远端返回的元数据信息判断是否需要写本地文件
     *
     * @param headerFields
     * @param url
     * @param local
     * @return
     */
    private static boolean shallWriteLocal(Map<String, List<String>> headerFields, URL url, File local, File lastModifiedFile) {
        long lastUpdate = getLastUpdateTimeStamp(headerFields, url);
        if (local.exists()) {
            long localLastModified = 0;
            try {
                localLastModified = Long.parseLong(FileUtils.readFileToString(lastModifiedFile, TisUTF8.get()));
            } catch (Throwable e) {
            }
            if (lastUpdate <= localLastModified) {
                return false;
            }
        }
        return true;
    }

    private static long getLastUpdateTimeStamp(Map<String, List<String>> headerFields, URL url) {
        Optional<String> first = null;
        List<String> lastupdate = headerFields.get(ConfigFileContext.KEY_HEAD_LAST_UPDATE);
        if (lastupdate == null || !(first = lastupdate.stream().findFirst()).isPresent()) {
            throw new IllegalStateException("url:" + url + " can not find " + ConfigFileContext.KEY_HEAD_LAST_UPDATE + " in headers");
        }
        return Long.parseLong(first.get());
    }

    public static String getPath(String parent, String filePath) {
        boolean parentEndWithSlash = StringUtils.endsWith(parent, "/");
        boolean childStartWithSlash = StringUtils.startsWith(filePath, "/");
        if (parentEndWithSlash && childStartWithSlash) {
            filePath = parent + StringUtils.substring(filePath, 1);
        } else if (!parentEndWithSlash && !childStartWithSlash) {
            filePath = parent + "/" + filePath;
        } else {
            filePath = parent + filePath;
        }
        return filePath;
    }

    public static URL getPathURL(String parent, String filePath) {
        return getPathURL(getPath(parent, filePath));
    }

    public static URL getPathURL(String filePath) {
        try {
            final RunEnvironment runtime = RunEnvironment.getSysRuntime();
            return new URL(runtime.getInnerRepositoryURL() + "/config/stream_script_repo.action?path=" + URLEncoder.encode(filePath, TisUTF8.getName()));
        } catch (Exception e) {
            throw new RuntimeException("filepath:" + filePath, e);
        }
    }
}
