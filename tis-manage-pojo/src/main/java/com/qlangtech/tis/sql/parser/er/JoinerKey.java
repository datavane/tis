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
package com.qlangtech.tis.sql.parser.er;

import org.apache.commons.lang.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主外键连接
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class JoinerKey {

    private String parentKey;

    private String childKey;

    public JoinerKey(String parentKey, String childKey) {
        if (StringUtils.isBlank(parentKey) || StringUtils.isBlank(childKey)) {
            throw new IllegalArgumentException("param parentKey or childKey can not be null");
        }
        this.parentKey = parentKey;
        this.childKey = childKey;
    }

    public JoinerKey() {
    }

    public static String createListNewLiteria(List<JoinerKey> joinerKeys) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Lists.newArrayList(");
        buffer.append(joinerKeys.stream().map((key) -> {
            StringBuffer b = new StringBuffer();
            return b.append("(\"").append(key.getParentKey()).append("\",\"").append(key.getChildKey()).append("\")");
        }).collect(Collectors.joining(",")));
        buffer.append(")");
        return buffer.toString();
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public void setChildKey(String childKey) {
        this.childKey = childKey;
    }

    public String getParentKey() {
        return this.parentKey;
    }

    public String getChildKey() {
        return this.childKey;
    }

    @Override
    public String toString() {
        return "{" + "parentKey='" + parentKey + '\'' + ", childKey='" + childKey + '\'' + '}';
    }
}
