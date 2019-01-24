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

import com.koubei.web.tag.pager.Pager.PageNumShowRule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractPageNumShowRule implements PageNumShowRule {

    public static final String YK_IGNORE_TAG = "<span class=\"yk-pagination-lot-of-page\">...</span>";

    public static final String K2_IGNORE_TAG = "<span class=\"more\">...</span>";

    @Override
    public final int getRangeWidth() {
        return this.getPreOffset().getSetp() + this.getTailOffset().getSetp();
    }

    @Override
    public boolean isShowLastPage() {
        return false;
    }

    @Override
    public boolean isShowFirstPage() {
        return true;
    }

    public static class PageNumShowRule20101203 extends AbstractPageNumShowRule {

        private final String ignorTag;

        public PageNumShowRule20101203(String ignorTag) {
            this.ignorTag = ignorTag;
        }

        @Override
        public Offset getPreOffset() {
            return new Offset(3, ignorTag);
        }

        @Override
        public Offset getTailOffset() {
            return new Offset(3, ignorTag);
        }

        @Override
        public int startRangeLength() {
            return 7;
        }
    }
}
