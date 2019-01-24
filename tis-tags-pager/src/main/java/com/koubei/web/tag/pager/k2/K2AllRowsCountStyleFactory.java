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
package com.koubei.web.tag.pager.k2;

import com.koubei.web.tag.pager.Pager;
import com.koubei.web.tag.pager.Pager.PageStatistics;

/*
 *         2010-12-3
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class K2AllRowsCountStyleFactory extends K2StyleFactory {

    public K2AllRowsCountStyleFactory(Pager pager, K2AroundTag aroundTag) {
        super(pager, aroundTag, true);
    }

    @Override
    public PageStatistics getPageStatistics() {
        return new PageStatistics() {

            @Override
            public void build(StringBuffer builder, int page, int pageCount) {
                int total = K2AllRowsCountStyleFactory.this.getPager().getTotalCount();
                builder.append("<span class=\"sum\">(共").append(total).append("条) 第<b>").append(page).append("</b>").append("/").append(pageCount).append("页</span>");
            }
        };
    }
}
