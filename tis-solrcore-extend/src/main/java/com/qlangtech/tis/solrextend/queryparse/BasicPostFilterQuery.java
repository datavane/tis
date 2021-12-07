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

import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicPostFilterQuery extends ExtendedQueryBase implements PostFilter {

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public boolean getCache() {
        return false;
    }

    @Override
    public int getCost() {
        return Math.max(super.getCost(), 100);
    }
}
