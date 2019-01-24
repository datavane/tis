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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexBuildParam {

    public static final String INDEXING_SOURCE_FS_NAME = "indexing_sourcefsname";

    public static final String INDEXING_BUILD_TABLE_TITLE_ITEMS = "indexing_buildtabletitleitems";

    public static final String INDEXING_OUTPUT_PATH = "indexing_outputpath";

    public static final String INDEXING_SOURCE_TYPE = "indexing_sourcetype";

    public static final String INDEXING_SOURCE_PATH = "indexing_sourcepath";

    public static final String INDEXING_SCHEMA_PATH = "indexing_schemapath";

    public static final String INDEXING_SOLRCONFIG_PATH = "indexing_solrconfig_path";

    public static final String INDEXING_SERVICE_NAME = "indexing_servicename";

    public static final String INDEXING_CORE_NAME = "indexing_corename";

    public static final String INDEXING_USER_NAME = "indexing_username";

    public static final String INDEXING_INCR_TIME = "indexing_incrtime";

    public static final String INDEXING_MAX_NUM_SEGMENTS = "indexing_maxNumSegments";

    public static final String INDEXING_GROUP_NUM = "indexing_groupnum";

    public static final String INDEXING_DELIMITER = "indexing_delimiter";

    public static final String INDEXING_SOLR_VERSION = "job_solrversion";

    public static final String INDEXING_RECORD_LIMIT = "indexing_recordlimit";

    // 记录数count
    public static final String INDEXING_ROW_COUNT = "indexing_row_count";

    private static final List<String> allfields;

    static {
        try {
            List<String> names = new ArrayList<>();
            Field[] fields = IndexBuildParam.class.getDeclaredFields();
            for (Field f : fields) {
                if (!((Modifier.STATIC & f.getModifiers()) > 0 && StringUtils.startsWith(f.getName(), "INDEXING_"))) {
                    continue;
                }
                names.add(String.valueOf(f.get(null)));
            }
            allfields = Collections.unmodifiableList(names);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllFieldName() {
        return allfields;
    }

    public static void main(String[] args) {
        List<String> fields = getAllFieldName();
        for (String f : fields) {
            System.out.println(f);
        }
    }
    // 
    // String schemaPath = "/user/" + username + "/" + coreName +
    // "/schema/schema.xml";
    // jobConf.set(, schemaPath);
    // 
    // jobConf.set("", state.getIndexName());
    // jobConf.set(, coreName);
    // jobConf.set(, String.valueOf(1));
    // jobConf.set(, username);
    // jobConf.set(, state.getTimepoint());
    // jobConf.set(, groupNum);
    // 
    // if (StringUtils.isNotBlank(state.getHdfsdelimiter())) {
    // jobConf.set(, state.getHdfsdelimiter());
    // }
    // 
    // // /user/admin/search4realjhsactivity/all/0/20150908233000/
    // 
    // jobConf.set("job.name", coreName + "-indexBuildJob");
    // // jobConf.setAtLeastMemoryMb(300);
    // // jobConf.setAtLeastSpaceMb(1024);
    // // jobConf.set("task.map.class",
    // //
    // "com.taobao.terminator.indexbuilder.map.HdfsIndexGetConfig,com.taobao.terminator.indexbuilder.map.HdfsIndexBuilder,com.taobao.terminator.indexbuilder.map.HdfsIndexDeliver");
    // 
    // // jobConf.set("task.jar.transfer", "false,false,false");
    // 
    // // String appcontext = "/user/" + username + "/" + coreName +
    // // "/app/applicationContext.xml";
    // // jobConf.set(, appcontext);
    // 
    // // final String sourcePath = "/user/" + userName + "/" + serviceName
    // // + "/all/" + groupNum + "/" + timePoint + "/";
    // // if (UISVersion.useSolr60(state.getIndexName())) {
    // // logger.info("collection:" + state.getIndexName() + " use solr6.0");
    // // jobConf.set("job.jarfile",
    // // "indexbuilder6.0,indexbuilder6.0,indexbuilder6.0");
    // // } else {
    // // logger.info("collection:" + state.getIndexName() + " use solr5.3");
    // // jobConf.set("job.jarfile",
    // // "indexbuilder5.3,indexbuilder5.3,indexbuilder5.3");
    // // }
    // 
    // jobConf.set("job.solrversion", UISVersion.useSolr60(state.getIndexName())
    // ? UISVersion.SOLR_VERSION_6 : UISVersion.SOLR_VERSION_5);
    // 
    // // if (state.getMaxDumpCount() != null) {
    // // jobConf.set("indexing.recordlimit",
    // // String.valueOf(state.getMaxDumpCount()));
    // // log.addLog(state, "has set record limit:" +
    // // String.valueOf(state.getMaxDumpCount()));
    // // }
    // 
    // jobConf.set("indexing.sourcetype", "HDFS");
}
