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
package com.qlangtech.tis.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LauncherResourceUtils {

    private static final Pattern CONFIG_RES_PATTERN = Pattern.compile("/(search4.+?)/(app-context-partition_(\\d))?");

    private static final Log logger = LogFactory.getLog(LauncherResourceUtils.class);

    // 目前版本n最大为8，因为RM那边默认设置成8分区管道。
    private static final int PARTITION_MAX_NUM = 8;

    public static AppLauncherResource getAppResource(Set<String> includes, String resourcePattern) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
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
        Resource[] extendRes = resourcePatternResolver.getResources("classpath*:global-conifg/*-context.xml");
        for (Resource r : extendRes) {
            launcherResource.add(r);
            logger.info("add extend res:" + r);
        }
        return launcherResource;
    }

    public static class AppLauncherResource {

        private final Map<String, IndexPro> indexPros;

        private final List<Resource> res;

        AppLauncherResource(// , Resource[] res
        Map<String, IndexPro> indexPros) {
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
