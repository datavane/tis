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
package com.qlangtech.tis.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月6日 下午4:42:57
 */
public class LauncherResourceUtils {

    private static final Pattern CONFIG_RES_PATTERN = Pattern.compile("/(search4.+?)/(app-context-partition_(\\d))?");

    private static final Log logger = LogFactory.getLog(LauncherResourceUtils.class);

    // 目前版本n最大为8，因为RM那边默认设置成8分区管道。
    private static final int PARTITION_MAX_NUM = 8;
    // 测试用，因为测试环境中有多个collection，对应多个daoContext配置，在测试过程中是否需要加入到springContext中
    public static IResourceFilter resourceFilter = (r) -> true;

    public static //
    AppLauncherResource getAppResource(// 
                                       Set<String> includes, String resourcePattern, URLClassLoader classLoader) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] res = resourcePatternResolver.getResources(resourcePattern);
        Map<String, IndexPro> indexes = new HashMap<>();
        AppLauncherResource launcherResource = new AppLauncherResource(indexes);
        Matcher matcher;
        URI uri;
        int includeSize = includes.size();
        for (Resource r : res) {
            uri = r.getURI();
            logger.info("resource uri:" + uri);
            matcher = CONFIG_RES_PATTERN.matcher(String.valueOf(uri));
            if (matcher.find()) {
                if (includeSize > 0 && !includes.contains(matcher.group(1))) {
                    continue;
                }
                launcherResource.add(r);
                String indexName = matcher.group(1);
                int partitionNum = matcher.group(3) == null ? 1 : Integer.parseInt(matcher.group(3));
                assert (partitionNum <= PARTITION_MAX_NUM);
                indexes.put(indexName, new IndexPro(indexName, partitionNum));
            }
        }
        Resource[] extendRes = resourcePatternResolver.getResources("classpath*:global-conf/*-context.xml");
        for (Resource r : extendRes) {
            if (!resourceFilter.match(r)) {
                continue;
            }
            launcherResource.add(r);
            logger.info("add extend res:" + r);
        }
        Resource[] daoConfigRes = resourcePatternResolver.getResources("classpath*:conf/*-dao-context.xml");
        for (Resource r : daoConfigRes) {
            if (!resourceFilter.match(r)) {
                continue;
            }
            launcherResource.add(r);
            logger.info("add dao config res:" + r);
        }
        return launcherResource;
    }

    /**
     * 测试用，因为测试环境中有多个collection，对应多个daoContext配置，在测试过程中是否需要加入到springContext中
     */
    public interface IResourceFilter {
        boolean match(Resource res);
    }

    public static class AppLauncherResource {

        private final Map<String, IndexPro> indexPros;

        private final List<Resource> res;

        AppLauncherResource(Map<String, IndexPro> indexPros) {
            super();
            this.indexPros = indexPros;
            this.res = new // Arrays.asList(res)
                    ArrayList<>();
        }

        public List<String> getIndexNames() {
            return new ArrayList<>(this.indexPros.keySet());
        }

        public Map<String, IndexPro> getIndexPro() {
            return this.indexPros;
        }

        public void add(Resource r) {
            this.res.add(r);
        }

        public List<Resource> getResource() {
            return res;
        }

        public List<List<String>> getIndexPartitionShuffle() {
            List<List<String>> lists = new LinkedList<>();
            int column = 0;
            for (IndexPro indexPro : indexPros.values()) {
                if (indexPro.getPartitionNum() > column) {
                    column = indexPro.getPartitionNum();
                }
            }
            for (int i = 0; i < column; i++) {
                lists.add(new LinkedList<>());
            }
            int currentColumn = 0;
            for (IndexPro indexPro : indexPros.values()) {
                System.out.println(indexPro.getIndexName() + " " + indexPro.getPartitionNum());
                for (int i = 0; i < indexPro.getPartitionNum(); i++) {
                    List<String> list = lists.get(currentColumn);
                    currentColumn = (currentColumn + 1) % column;
                    list.add(indexPro.getIndexName());
                }
            }
            return lists;
        }
    }

    public static class IndexPro {

        private final String indexName;

        private int partitionNum;

        public IndexPro(String indexName, int partitionNum) {
            this.indexName = indexName;
            this.partitionNum = partitionNum;
        }

        public String getIndexName() {
            return indexName;
        }

        public int getPartitionNum() {
            return partitionNum;
        }

        public void setPartitionNum(int partitionNum) {
            this.partitionNum = partitionNum;
        }
    }
}
