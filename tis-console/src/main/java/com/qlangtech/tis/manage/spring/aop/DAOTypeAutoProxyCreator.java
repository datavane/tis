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
package com.qlangtech.tis.manage.spring.aop;

import java.util.regex.Pattern;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DAOTypeAutoProxyCreator extends AbstractAutoProxyCreator {

    private static final long serialVersionUID = 1L;

    private static final Pattern methodPattern = Pattern.compile("^([^((get)(count)(select))]).*");

    public static void main(String[] arg) {
        // System.out.println(PatternMatchUtils.simpleMatch("updateUser",
        // ));
        System.out.println(methodPattern.matcher("updateUser").matches());
        System.out.println(methodPattern.matcher("getUser").matches());
        System.out.println(methodPattern.matcher("countUser").matches());
        System.out.println(methodPattern.matcher("selectUser").matches());
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException {
        if (beanClass == null || beanClass.getPackage() == null) {
            return DO_NOT_PROXY;
        }
        try {
            if (beanClass.equals(com.qlangtech.tis.manage.biz.dal.dao.impl.OperationLogDAOImpl.class) || IDepartmentDAO.class.isAssignableFrom(beanClass)) {
                return DO_NOT_PROXY;
            }
        } catch (Exception e) {
            throw new BeansException(e.getMessage(), e) {

                private static final long serialVersionUID = 1L;
            };
        }
        return ((com.qlangtech.tis.manage.common.OperationLogger.class.isAssignableFrom(beanClass))) ? PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY;
    }
}
