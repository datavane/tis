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
package com.qlangtech.tis.manage.spring.aop;

import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-9-11
 */
public class TerminatorNameMatchMethodPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (StringUtils.startsWith(method.getName(), "count")) {
            return false;
        }
        if (StringUtils.startsWith(method.getName(), "select")) {
            return false;
        }
        if (StringUtils.startsWith(method.getName(), "load")) {
            return false;
        }
        if (StringUtils.startsWith(method.getName(), "get")) {
            return false;
        }
        if (StringUtils.startsWith(method.getName(), "is")) {
            return false;
        }
        return true;
    }
}
