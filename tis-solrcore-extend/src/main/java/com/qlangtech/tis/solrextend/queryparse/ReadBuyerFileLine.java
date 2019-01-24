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

/* *
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
