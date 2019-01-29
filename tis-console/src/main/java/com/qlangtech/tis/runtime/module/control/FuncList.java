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
package com.qlangtech.tis.runtime.module.control;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import com.alibaba.citrus.turbine.Context;
//import com.qlangtech.tis.manage.biz.dal.pojo.FuncCriteria;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;
//import com.qlangtech.tis.runtime.module.screen.FuncList.FuncGroup;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class FuncList extends BasicScreen {

    private static final long serialVersionUID = 1L;
//
//    @Override
//    public void execute(Context context) throws Exception {
//        FuncCriteria criteria = new FuncCriteria();
//        // criteria.setOrderByClause("func_group_key desc");
//        Map<FuncGroup, List<com.qlangtech.tis.manage.biz.dal.pojo.Func>> funcGroup = new HashMap<FuncGroup, List<com.qlangtech.tis.manage.biz.dal.pojo.Func>>();
//        List<com.qlangtech.tis.manage.biz.dal.pojo.Func> funcCluter = null;
//        for (com.qlangtech.tis.manage.biz.dal.pojo.Func func : this.getFuncDAO().selectByExample(criteria)) {
//            FuncGroup key = new FuncGroup(func.getFuncGroupKey(), func.getFuncGroupName());
//            if ((funcCluter = funcGroup.get(key)) == null) {
//                funcCluter = new ArrayList<com.qlangtech.tis.manage.biz.dal.pojo.Func>();
//                funcGroup.put(key, funcCluter);
//            }
//            funcCluter.add(func);
//        }
//        context.put("funclist", funcGroup);
//    }
}
