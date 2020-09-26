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
package com.koubei.web.tag.pager;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;

/**
 * 因为要做到能够在页面（jsp）和action（webwork或者Struts2）<br>
 * 之间传递分页控件对象 分页控件也和webwork 和struts的实现无关,<br>
 * 所以做这个pageDTO类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2010-2-25
 */
public class PagerDTO {

    private final Map<String, Pager> pageControl = new HashMap<String, Pager>();

    private static final String REQUEST_KEY = PagerDTO.class.getName();

    private final ServletRequest request;

    public static PagerDTO get(ServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        PagerDTO dto = (PagerDTO) request.getAttribute(REQUEST_KEY);
        if (dto == null) {
            synchronized (PagerDTO.class) {
                if (request.getAttribute(REQUEST_KEY) == null) {
                    dto = new PagerDTO(request);
                    request.setAttribute(REQUEST_KEY, dto);
                }
            }
        }
        return dto;
    }

    protected PagerDTO(ServletRequest request) {
        this.request = request;
    }

    void add(String name, Pager action) {
        // BeanInfo beanInfo = null;
        // try {
        // beanInfo = Introspector.getBeanInfo(action.getClass(),
        // Introspector.IGNORE_ALL_BEANINFO);
        // } catch (IntrospectionException e) {
        // throw new RuntimeException(e);
        // }
        // 
        // PropertyDescriptor[] pDescript = beanInfo.getPropertyDescriptors();
        // Method m = null;
        // 
        // for (PropertyDescriptor d : pDescript) {
        // 
        // m = d.getReadMethod();
        // 
        // try {
        // 
        // if (m.getReturnType() == Pager.class) {
        // pageControl
        // .put(d.getName(), (Pager) m.invoke(action, null));
        // }
        // 
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }
        // }
        pageControl.put(name, action);
    }

    /**
     * 取得分页控件
     *
     * @param name
     * @return
     */
    public Pager getByName(String name) {
        return pageControl.get(name);
    }

    // public Pager getPager() {
    // return new Pager(null);
    // }
    public static void main(String[] arg) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(User.class, Introspector.IGNORE_ALL_BEANINFO);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        PropertyDescriptor[] pDescript = beanInfo.getPropertyDescriptors();
        Method m = null;
        for (PropertyDescriptor d : pDescript) {
            m = d.getReadMethod();
            try {
                // if (m.getReturnType() == Pager.class) {
                System.out.println(m.invoke(new User(), null));
            // }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class User {

        public String getName() {
            return "aa";
        }
    }
}
