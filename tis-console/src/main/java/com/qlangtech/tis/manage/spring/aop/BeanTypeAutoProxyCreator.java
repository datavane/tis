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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * 权限控制拦截器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-15
 */
public class BeanTypeAutoProxyCreator extends AbstractAutoProxyCreator {

    private static final long serialVersionUID = 1L;

    private static final Pattern pkg_pattern = Pattern.compile("^com\\.qlangtech\\.tis\\.(runtime|trigger|coredefine)\\..*");

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException {
        if (beanClass == null || beanClass.getPackage() == null) {
            return DO_NOT_PROXY;
        }
        Matcher m = pkg_pattern.matcher(beanClass.getPackage().getName());
        return ((m.matches()) && BasicModule.class.isAssignableFrom(beanClass)) ? PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY;
    }

    public static void main(String[] arg) {
        Matcher m = pkg_pattern.matcher("com.taobao.terminator.runtimee.spring.aop");
        if (m.matches()) {
            System.out.println("match");
        }
    }
}
