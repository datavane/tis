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
package com.qlangtech.tis.runtime.module.screen;

import java.util.ArrayList;
import java.util.Collection;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.BizFuncAuthority;
import com.qlangtech.tis.manage.module.screen.BuildNavData;
import com.qlangtech.tis.manage.spring.aop.Func;

/*
 * 功能列表，可以将应用授权给部门
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class FuncList extends BasicManageScreen {

    private static final long serialVersionUID = 1L;

    @Override
    @Func(PermissionConstant.AUTHORITY_FUNC_LIST)
    public void execute(Context context) throws Exception {
    // this.disableDomainView(context);
    // BizFuncAuthorityCriteria criteria = new BizFuncAuthorityCriteria();
    // 
    // List<BizFuncAuthority> authorityFunc = this.getBizFuncAuthorityDAO()
    // .selectWithGroupByFuncidAppid(criteria);
    // 
    // List<Function> funclist = new ArrayList<Function>(
    // BuildNavData.items_config.length);
    // 
    // Function func = null;
    // for (BuildNavData.Item item : BuildNavData.FUNC_MAP.values()) {
    // 
    // func = new Function(item);
    // 
    // for (BizFuncAuthority a : authorityFunc) {
    // if (StringUtils.equals(a.getFuncId(), item.getPermissionCode())) {
    // func.addAuthorityDepartmentment(a);
    // }
    // }
    // 
    // funclist.add(func);
    // }
    // 
    // context.put("funclist", funclist);
    }

    public static class FuncGroup {

        private final Integer key;

        private final String name;

        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        public FuncGroup(Integer key, String name) {
            super();
            this.key = key;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Integer getKey() {
            return key;
        }
    }

    // public static class Function {
    // 
    // final BuildNavData.Item funcItem;
    // 
    // final Collection<BizFuncAuthority> authorityDepartmentment = new ArrayList<BizFuncAuthority>();
    // 
    // private Function(Item item) {
    // super();
    // this.funcItem = item;
    // }
    // 
    // public Collection<BizFuncAuthority> getAuthorityDepartmentment() {
    // return authorityDepartmentment;
    // }
    // 
    // public void addAuthorityDepartmentment(BizFuncAuthority authority) {
    // this.authorityDepartmentment.add(authority);
    // }
    // 
    // public BuildNavData.Item getItem() {
    // return funcItem;
    // }
    // }
    public static void main(String[] arg) {
    
    }
}
