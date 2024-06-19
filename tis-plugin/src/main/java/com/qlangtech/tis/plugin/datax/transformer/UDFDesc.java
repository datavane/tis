/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.datax.transformer;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.Option;

import java.util.List;

/**
 * 每个Transformer udf 对应的转化器
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-12 14:06
 **/
public class UDFDesc {
    private List<Option> pairs = Lists.newArrayList();

    public UDFDesc(String key, String content) {
        this.addPair(key, content);
    }

    public UDFDesc(String key,List<UDFDesc> content){
        this.addPair(key, content);
    }

    public void addPair(String key, String  content) {
        this.pairs.add(new Option(key, content));
    }

    public void addPair(String key, List<UDFDesc>  content) {
        this.pairs.add(new Option(key, content));
    }

    public List<Option> getPairs() {
        return this.pairs;
    }
}
