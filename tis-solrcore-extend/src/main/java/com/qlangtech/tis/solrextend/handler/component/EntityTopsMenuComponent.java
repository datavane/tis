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
package com.qlangtech.tis.solrextend.handler.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.params.SolrParams;

/**
 * 取店铺一个月之内卖出
 * http://120.55.195.72:8080/solr/search4totalpay_shard4_replica2/select
 * ?q=entity_id
 * %3A00024941+AND+curr_date%3A%5B20151100+TO+20151200%5D&wt=json&indent
 * =true&distrib
 * =false&topsMenu=true&topsMenu.field=all_menu&groupby.key=xxx&rows=0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class EntityTopsMenuComponent extends TripleValueMapReduceComponent {

    private static final String COMPONENT_NAME = "topsMenu";

    @Override
    protected InstanceCollector createCollector(SolrParams params, ValueSource valueSource, String groupByKey, String kindmenuIncludeValue) {
        // 要过滤 纸巾，餐具什么的
        String[] excludeMenuToken = StringUtils.split(params.get("exclude.token"), ",");
        int tops = Integer.parseInt(StringUtils.defaultIfEmpty(params.get("tops"), "10"));
        return new TopsMenuCollector(excludeMenuToken, tops, valueSource, Collections.emptyMap());
    }

    public static void main(String[] args) {
        Map<String, MenuCount> /* menuid */
        menuSelect = new TreeMap<String, /* menuid */
        MenuCount>();
        for (int i = 0; i < 20; i++) {
            menuSelect.put("a" + i, new MenuCount((int) (Math.random() * 100), "b" + i));
        }
        List<Map.Entry<String, MenuCount>> list = new ArrayList<Map.Entry<String, MenuCount>>(menuSelect.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, MenuCount>>() {

            @Override
            public int compare(Entry<String, MenuCount> o1, Entry<String, MenuCount> o2) {
                return o2.getValue().num - o1.getValue().num;
            }
        });
        for (Map.Entry<String, MenuCount> entry : list) {
            System.out.println(entry.getKey() + "," + entry.getValue().num);
        }
    }

    private static class TopsMenuCollector extends InstanceCollector {

        private Map<String, MenuCount> /* menuid */
        menuSelect = new TreeMap<String, /* menuid */
        MenuCount>();

        private final String[] excludeMenuToken;

        private final int tops;

        public TopsMenuCollector(String[] excludeMenuToken, int tops, ValueSource groupByVS, Map<?, ?> vsContext) {
            super(groupByVS, vsContext, null);
            this.excludeMenuToken = excludeMenuToken;
            this.tops = tops;
        }

        @Override
        public Map<String, ?> getStatiResult() {
            int collect = 0;
            Map<String, MenuCount> /* menuid */
            result = new LinkedHashMap<String, /* menuid */
            MenuCount>();
            List<Map.Entry<String, MenuCount>> list = new ArrayList<Map.Entry<String, MenuCount>>(menuSelect.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, MenuCount>>() {

                @Override
                public int compare(Entry<String, MenuCount> o1, Entry<String, MenuCount> o2) {
                    return o2.getValue().num - o1.getValue().num;
                }
            });
            aa: for (Map.Entry<String, MenuCount> entry : list) {
                if (excludeMenuToken != null) {
                    for (String token : excludeMenuToken) {
                        if (StringUtils.contains(entry.getValue().name, token)) {
                            continue aa;
                        }
                    }
                }
                if (collect++ < this.tops) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }

        @Override
        protected void processInstance(AllMenuRow menuRow) {
            String menuid = menuRow.getMenuId();
            MenuCount count = menuSelect.get(menuid);
            if (count == null) {
                count = new MenuCount(menuRow.getNum().intValue(), menuRow.getName());
                menuSelect.put(menuid, count);
                return;
            } else {
                count.addNum(menuRow.getNum().intValue());
            }
        }
    }

    public static class MenuCount {

        private int num;

        private final String name;

        /**
         * @param num
         * @param name
         */
        public MenuCount(int num, String name) {
            super();
            this.num = num;
            this.name = name;
        }

        public void addNum(int num) {
            this.num += num;
        }

        @Override
        public String toString() {
            return "num:" + num + ",name:" + this.name;
        }
    }

    @Override
    public String getDescription() {
        return COMPONENT_NAME;
    }

    @Override
    protected String getComponentName() {
        return COMPONENT_NAME;
    }
}
