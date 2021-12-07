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
package com.qlangtech.tis.solrextend.fieldtype;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.schema.BoolField;

/**
 * 根据字段的内容是否为空来判断是否为‘T’或者‘F’
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IsNotNullField extends BoolField {

    @Override
    public final String toInternal(String val) {
        // char ch = (val!=null && val.length()>0) ? val.charAt(0) : 0;
        return isTrue(val) ? "T" : "F";
    // return super.toInternal(val);
    }

    //
    protected boolean isTrue(String val) {
        if (StringUtils.isNotEmpty(val)) {
            if (val.charAt(0) == 'F' && val.length() == 1) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
    // @Override
    // public String toExternal(IndexableField f) {
    // return super.toExternal(f);
    // }
}
