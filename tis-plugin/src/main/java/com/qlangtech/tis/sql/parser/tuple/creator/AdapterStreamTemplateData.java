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

package com.qlangtech.tis.sql.parser.tuple.creator;

import com.qlangtech.tis.datax.TableAlias;
import com.qlangtech.tis.sql.parser.tuple.creator.IStreamIncrGenerateStrategy.IStreamTemplateData;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-06-15 13:08
 **/
public abstract class AdapterStreamTemplateData implements IStreamTemplateData {
    private final IStreamTemplateData data;

    public AdapterStreamTemplateData(IStreamTemplateData data) {
        this.data = data;
    }

    @Override
    public String getCollection() {
        return data.getCollection();
    }

    @Override
    public String getJavaName() {
        return data.getJavaName();
    }

    @Override
    public List<TableAlias> getDumpTables() {
        return data.getDumpTables();
    }
}
