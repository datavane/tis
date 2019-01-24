/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

/*
 * 因为要做到能够在页面（jsp）和action（webwork或者Struts2）<br>
 * 之间传递分页控件对象 分页控件也和webwork 和struts的实现无关,<br>
 * 所以做这个pageDTO类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
