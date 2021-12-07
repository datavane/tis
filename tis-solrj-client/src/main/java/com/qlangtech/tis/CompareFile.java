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
package com.qlangtech.tis;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CompareFile {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        File file1 = new File(args[0]);
        File file2 = new File(args[1]);
        LineIterator it1 = FileUtils.lineIterator(file1);
        LineIterator it2 = FileUtils.lineIterator(file2);
        Map<String, String> /* version */
        map1 = getMap(it1);
        Map<String, String> /* version */
        map2 = getMap(it2);
        PrintWriter writer = new PrintWriter(FileUtils.openOutputStream(new File("summary.txt")));
        compare(file1, file2, map1, map2, writer);
        writer.println("=================================");
        compare(file2, file1, map2, map1, writer);
        writer.flush();
        writer.close();
    }

    protected static void compare(File file1, File file2, Map<String, String> map1, Map<String, String> map2, PrintWriter writer) {
        final Map<String, AtomicInteger> hourCount = new HashMap<String, AtomicInteger>();
        writer.println(file1.getName() + " compare to " + file2.getName());
        String value = null;
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            value = map2.get(entry.getKey());
            if (value == null) {
                writer.println(entry.getValue() + "," + entry.getKey());
                addCount(hourCount, entry);
            } else if (!StringUtils.equals(entry.getValue(), value)) {
                writer.println(entry.getValue() + "," + value + "," + entry.getKey());
                addCount(hourCount, entry);
            }
        }
        writer.println("-------------------------------------------");
        for (Map.Entry<String, AtomicInteger> entry : hourCount.entrySet()) {
            writer.println(entry.getKey() + ":" + entry.getValue().get());
        }
    }

    protected static void addCount(final Map<String, AtomicInteger> hourCount, Map.Entry<String, String> entry) {
        String hour = StringUtils.substring(entry.getValue(), 0, 10);
        AtomicInteger count = hourCount.get(hour);
        if (count == null) {
            count = new AtomicInteger();
            hourCount.put(hour, count);
        }
        count.incrementAndGet();
    }

    protected static Map<String, /* totalpayid */
    String> getMap(LineIterator it1) {
        Map<String, String> /* version */
        result = new HashMap<String, String>();
        String[] tuple = null;
        while (it1.hasNext()) {
            tuple = StringUtils.split(it1.nextLine(), ",");
            result.put(tuple[0], /* totalpayid */
            tuple[1]);
        }
        return result;
    }
}
