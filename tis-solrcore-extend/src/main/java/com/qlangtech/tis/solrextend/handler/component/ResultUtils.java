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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.util.SimpleOrderedMap;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ResultUtils {

    public static SimpleOrderedMap<String> writeMap(Map<?, /* key */
    ?> dto) {
        SimpleOrderedMap<String> result = new SimpleOrderedMap<String>();
        for (Map.Entry<?, ?> entry : dto.entrySet()) {
            result.add(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }

    public static List<String> writeList(Collection<?> dto) {
        List<String> result = new ArrayList<String>();
        for (Object d : dto) {
            result.add(d.toString());
        }
        return result;
    }
}
