/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.runtime.module.misc;

import com.koubei.web.tag.pager.LinkBuilder;
import com.koubei.web.tag.pager.Pager;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 与 web 请求绑定的分页器辅助方法。
 *
 * <p>{@link Pager} 本体已下沉到 tis-plugin（不含 servlet 依赖），做 Web 请求绑定所需的这几步
 * 留在 tis-console：本模块的依赖方向是 tis-console → tis-plugin，所以只有这个方向放得下。
 *
 * @author 百岁（baisui@qlangtech.com）
 */
public class PagerUtils {

    private PagerUtils() {
    }

    /**
     * 新建一个 {@link Pager} 并登记进本次请求，使 JSP 与 action 能按名字取到同一个分页器。
     *
     * @param name   分页器名字（页面里引用它用的 key）
     * @param builder 分页链接构造器
     * @param request 当前请求，分页器挂在它的 attribute 上
     * @return 新建的分页器（调用方随后 setTotalCount / setCurPage / setRowsPerPage）
     */
    public static Pager register(String name, LinkBuilder builder, HttpServletRequest request) {
        Pager pager = new Pager(builder);
        PagerDTO.get(request).add(name, pager);
        return pager;
    }
}
