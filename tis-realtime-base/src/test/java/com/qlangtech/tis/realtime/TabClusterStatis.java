/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
