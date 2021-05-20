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

package com.qlangtech.tis.datax.impl;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.InputStream;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-20 10:25
 **/
public class Test {

    public static void main(String[] args) throws Exception {
        JSONObject jsonObject = new JSONObject();
        String readFrom = null;
        try (InputStream input = Test.class.getResourceAsStream("/test.txt")) {
            readFrom = IOUtils.toString(input, TisUTF8.get());
            System.out.println(readFrom);
            jsonObject.put("filecontent", (readFrom));
        }
        jsonObject.put("tabchar", "\t");

        jsonObject.put("test", "\\N");
        System.out.println(JsonUtil.toString(jsonObject));
        System.out.println("jsonObject.getString(\"test\"):" + jsonObject.getString("test"));
        System.out.println("jsonObject.getString(\"filecontent\"):" + jsonObject.getString("filecontent"));
        System.out.println("jsonObject.getString(\"tabchar\"):" + jsonObject.getString("tabchar"));


        System.out.println("ddd:" + StringEscapeUtils.escapeJava("\n"));
    }

}
