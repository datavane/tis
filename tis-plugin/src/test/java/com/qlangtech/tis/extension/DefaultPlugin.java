/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.extension;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-11 09:57
 **/
public class DefaultPlugin implements Describable<DefaultPlugin> {

    @FormField(identity = false, type = FormFieldType.INPUTTEXT)
    public String name;

    @FormField(type = FormFieldType.TEXTAREA)
    public String cols;

    static int turn;

    public static String getColsDefaultVal() {

        JSONArray cols = new JSONArray();
        JSONObject col = new JSONObject();
        col.put("name", "baisui" + (turn++));
        cols.add(col);
        return cols.toJSONString(); // "cols_default_val_turn_" + turn++;
    }

    @Override
    public Descriptor<DefaultPlugin> getDescriptor() {
        Descriptor<DefaultPlugin> descriptor = TIS.get().getDescriptor(this.getClass());
        return descriptor;
    }

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<DefaultPlugin> {
        public DefaultDescriptor() {
        }
    }
}
