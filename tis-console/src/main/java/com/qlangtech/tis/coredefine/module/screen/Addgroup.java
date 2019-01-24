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
package com.qlangtech.tis.coredefine.module.screen;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.spring.aop.Func;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Addgroup extends CoreDefineScreen {

    private static final long serialVersionUID = 1L;

    private static final Set<AddType> ADD_TYPE = new HashSet<AddType>();

    static {
        ADD_TYPE.add(AddType.GROUP);
        ADD_TYPE.add(AddType.REPLICA);
    }

    @Override
    @Func(PermissionConstant.APP_REPLICA_MANAGE)
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        AddType addtype = AddType.parse(this.getString("addtype"));
        if (!ADD_TYPE.contains(addtype)) {
            throw new IllegalArgumentException("param addtype:" + this.getString("addtype") + " is invalid");
        }
        context.put("addtype", addtype);
        setAssignServerInfo(context);
    }

    protected void setAssignServerInfo(Context context) throws Exception {
    }

    public enum AddType {

        GROUP("添加组"), REPLICA("添加组内副本");

        private final String literia;

        private AddType(String literia) {
            this.literia = literia;
        }

        public String getLiteria() {
            return literia;
        }

        public static AddType parse(String value) {
            if (StringUtils.equalsIgnoreCase(value, "group")) {
                return GROUP;
            } else if (StringUtils.equalsIgnoreCase(value, "replica")) {
                return REPLICA;
            }
            throw new IllegalArgumentException("value:" + value + " is not valid");
        }
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}
