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
package com.qlangtech.tis.manage.common;

import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Module {

    private final String namespace;

    public Module(String namespache) {
        this.namespace = namespache;
    }

    public StringBuffer setTarget(String value) {
        final char[] charAry = value.toCharArray();
        StringBuffer result = new StringBuffer(namespace);
        if (!StringUtils.startsWith(value, "/")) {
            result.append("/");
        }
        for (int i = 0; i < charAry.length; i++) {
            if (Character.isUpperCase(charAry[i])) {
                result.append("_").append(Character.toLowerCase(charAry[i]));
            } else {
                result.append(charAry[i]);
            }
        }
        if (StringUtils.indexOf(value, ".") < 0) {
            result.append(".htm");
        }
        return result;
    }
}
