/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.queryparse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ReadBuyerFileLine {

    static final char splitToken = '\05';

    public static void main(String[] args) throws Exception {
        final Map<String, AtomicInteger> sellerIdset = new TreeMap<String, AtomicInteger>();
        write(new RowProcess() {

            @Override
            public void process(Map<String, String> row) throws Exception {
                String dynamicInfo = row.get("dynamic_info");
                System.out.println(dynamicInfo);
                if (StringUtils.isEmpty(dynamicInfo)) {
                    return;
                }
                String[] array = StringUtils.split(dynamicInfo, ";");
                String[] content = null;
                String prefixDynamicInfo = null;
                AtomicInteger count = null;
                for (String info : array) {
                    content = StringUtils.split(info, "_");
                    if (content.length < 2) {
                        continue;
                    }
                    prefixDynamicInfo = content[0] + "_" + content[1];
                    count = sellerIdset.get(prefixDynamicInfo);
                    if (count == null) {
                        count = new AtomicInteger();
                        sellerIdset.put(prefixDynamicInfo, count);
                    }
                    count.incrementAndGet();
                }
            }
        });
        for (Map.Entry<String, AtomicInteger> entry : sellerIdset.entrySet()) {
            System.out.println("key:" + entry.getKey() + ",value:" + entry.getValue());
        }
        System.out.println(sellerIdset.size());
    }

    public interface RowProcess {

        public void process(Map<String, String> row) throws Exception;
    }

    /**
     * @param args
     */
    public static void write(RowProcess process) throws Exception {
        LineIterator it = FileUtils.lineIterator(new File("d:\\tmp\\21"));
        String title = null;
        List<String> titles = new ArrayList<String>();
        if (it.hasNext()) {
            title = it.nextLine();
            for (String colName : StringUtils.split(title, "\05")) {
                titles.add(colName);
            }
        }
        System.out.println("==============================");
        String line = null;
        Map<String, String> row = null;
        while (it.hasNext()) {
            row = new HashMap<String, String>();
            int colIndex = 0;
            line = it.nextLine();
            for (String colValue : cols(titles.size(), line)) {
                row.put(titles.get(colIndex++), colValue);
            }
            process.process(row);
        }
    }

    /**
     * @param args
     */
    public static void write(final IndexWriter writer) throws Exception {
        write(new RowProcess() {

            @Override
            public void process(Map<String, String> row) throws Exception {
                Document doc = new Document();
                String dynamicInfo = row.get("dynamic_info");
                if (StringUtils.isEmpty(dynamicInfo)) {
                    return;
                }
                String[] array = StringUtils.split(dynamicInfo, ";");
                for (String info : array) {
                    Field pathField = new StringField("dynamic_info", info, Field.Store.YES);
                    doc.add(pathField);
                }
                writer.addDocument(doc);
            }
        });
    }

    private static String[] cols(int titleLength, String line) {
        String[] result = new String[titleLength];
        int i = 0;
        char[] lineArray = line.toCharArray();
        int strLength = 0;
        int offset = 0;
        for (char c : line.toCharArray()) {
            if (c != splitToken) {
                strLength++;
            } else {
                result[i++] = new String(lineArray, offset, strLength);
                offset += (strLength + 1);
                strLength = 0;
                if (i >= titleLength) {
                    return result;
                }
            }
        }
        return result;
    }
}
