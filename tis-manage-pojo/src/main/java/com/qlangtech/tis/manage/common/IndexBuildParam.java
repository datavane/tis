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
    
    // 构建索引最大错误上限，超过这个上限之后索引构建会失败
    public static final String INDEX_MAX_DOC_FAILD_LIMIT = "indexing_maxfail_limit";

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
 
}
