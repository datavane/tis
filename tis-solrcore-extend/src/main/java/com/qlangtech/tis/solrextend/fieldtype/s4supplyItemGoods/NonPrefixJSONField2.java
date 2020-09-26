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
package com.qlangtech.tis.solrextend.fieldtype.s4supplyItemGoods;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.solrextend.fieldtype.JSONField2;

/**
 * 业务方不希望在json映射的字段中有前缀
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NonPrefixJSONField2 extends JSONField2 {

    @Override
    protected boolean isPropPrefixNotEmpty() {
        return true;
    }

    @Override
    protected String getPropPrefix() {
        return StringUtils.EMPTY;
    }
}
