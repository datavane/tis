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
package com.qlangtech.tis.realtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TabClusterStatis {

    private static Set<String> mainTables = new HashSet<String>();

    static {
        // 主表
        mainTables.add("menu");
        mainTables.add("kind_menu");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // 记录每个表和其他表的应用关系
        Map<String, TableLink> tabcluster = new HashMap<String, TableLink>();
        LineIterator it = FileUtils.lineIterator(new File("D:\\tmp\\tab.txt"));
        // 计算表之间的引用关系
        String tab = null;
        Set<String> tables = null;
        TableLink tlink = null;
        while (it.hasNext()) {
            tab = it.nextLine();
            if (StringUtils.startsWith(tab, "\"")) {
                tables = new HashSet<String>();
                tab = StringUtils.substringAfter(tab, "\"");
            }
            boolean endWithSlash = false;
            if (endWithSlash = StringUtils.endsWith(tab, "\"")) {
                tab = StringUtils.substringBefore(tab, "\"");
            }
            tables.add(tab);
            if (endWithSlash) {
                for (String t : tables) {
                    tlink = tabcluster.get(t);
                    if (tlink == null) {
                        tlink = new TableLink(t);
                        tabcluster.put(t, tlink);
                    }
                    tlink.refs.addAll(tables);
                }
            }
        }
        // 将主表删除不参与计算
        Iterator<String> nameIt = tabcluster.keySet().iterator();
        while (nameIt.hasNext()) {
            if (mainTables.contains(nameIt.next())) {
                nameIt.remove();
            }
        }
        List<TableLink> tablelist = new ArrayList<TableLink>(tabcluster.values());
        TableLink tref1 = null;
        TableLink tref2 = null;
        Set<String> subsetTable = null;
        List<Set<String>> subsetList = new ArrayList<Set<String>>();
        for (int i = 0; i < tablelist.size(); i++) {
            tref1 = tablelist.get(i);
            if (tref1.hasSetsubWithOther) {
                continue;
            }
            subsetTable = new HashSet<String>();
            // subsetTable.add(String.valueOf(i));
            subsetTable.addAll(tref1.refs);
            subsetList.add(subsetTable);
            for (int j = (i + 1); j < tablelist.size(); j++) {
                tref2 = tablelist.get(j);
                if (tref2.hasSetsubWithOther) {
                    continue;
                }
                if (tref2.hasSubSet(subsetTable)) {
                    // subsetTable.add("j:" + String.valueOf(j));
                    subsetTable.addAll(tref2.refs);
                    tref2.hasSetsubWithOther = true;
                }
            }
            for (int jj = tablelist.size() - 1; jj >= (i + 1); jj--) {
                tref2 = tablelist.get(jj);
                if (tref2.hasSetsubWithOther) {
                    continue;
                }
                if (tref2.hasSubSet(subsetTable)) {
                    // subsetTable.add("jj:" + String.valueOf(jj));
                    subsetTable.addAll(tref2.refs);
                    tref2.hasSetsubWithOther = true;
                }
            }
        }
        List<String> sortTables = null;
        for (Set<String> c : subsetList) {
            sortTables = new ArrayList<>(c);
            Collections.sort(sortTables);
            for (String t : sortTables) {
                // if (mainTables.contains(t)) {
                // continue;
                // }
                System.out.print(t + ",");
            }
            System.out.println();
        }
    // System.out
    // .println("===============================================================");
    // int ii = 0;
    // for (TableLink l : tabcluster.values()) {
    // System.out.print((ii++) + l.name + "[" + l.refs.size() + "]===>");
    // for (String n : l.refs) {
    // System.out.print(n + ",");
    // }
    // 
    // System.out.println();
    // }
    }

    private static class TableLink {

        private final String name;

        private final Set<String> refs = new HashSet<String>();

        boolean hasSetsubWithOther = false;

        public TableLink(String name) {
            super();
            this.name = name;
        }

        boolean hasSubSet(TableLink refs) {
            return hasSubSet(refs.refs);
        }

        public boolean hasSubSet(Set<String> refs) {
            for (String tab : refs) {
                if (mainTables.contains(tab)) {
                    continue;
                }
                if (this.refs.contains(tab)) {
                    return true;
                }
            }
            return false;
        }
    }
}
