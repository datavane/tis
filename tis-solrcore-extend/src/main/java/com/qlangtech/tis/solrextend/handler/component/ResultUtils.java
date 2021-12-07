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
