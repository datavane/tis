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
package com.qlangtech.tis.manage.common;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年8月28日
 */
public class TisUTF8 {

    public static void main(String[] args) {
        Charset big5 = Charset.forName("big5");
        System.out.println(big5);
        List<Option> all = allSupported();
        for (Option o : all) {
            System.out.println(o.getName() + ":" + o.getValue());
        }
    }

    public static List<Option> allSupported() {
        List<Option> all = Lists.newArrayList();
        Option o = null;
        for (Map.Entry<String, Charset> entry : Charset.availableCharsets().entrySet()) {
            if (entry.getKey().startsWith("x-")) {
                continue;
            }
            if (entry.getKey().startsWith("IBM")) {
                continue;
            }
            if (entry.getKey().startsWith("windows-")) {
                continue;
            }
            if (entry.getKey().startsWith("ISO-")) {
                continue;
            }
            o = new Option(entry.getKey(), StringUtils.lowerCase(entry.getKey()));
            all.add(o);
        }
        return all;
    }

    private TisUTF8() {
    }

    public static Charset get() {
        return StandardCharsets.UTF_8;
    }

    public static String getName() {
        return get().name();
    }
}
