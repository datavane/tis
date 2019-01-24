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

import com.koubei.web.tag.pager.AbstractPageNumShowRule;
import com.koubei.web.tag.pager.AroundTag;
import com.koubei.web.tag.pager.FormtDirectJump;
import com.koubei.web.tag.pager.GlobalStyleFactory;
import com.koubei.web.tag.pager.Pager;
import com.koubei.web.tag.pager.Pager.DirectJump;
import com.koubei.web.tag.pager.Pager.NavigationStyle;
import com.koubei.web.tag.pager.Pager.PageNumShowRule;
import com.koubei.web.tag.pager.Pager.PageStatistics;

/*
 *         2010-12-3
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class K2StyleFactory extends GlobalStyleFactory {

    private final K2AroundTag aroundTag;

    private boolean havePageStatistics;

    public K2StyleFactory(Pager pager, K2AroundTag aroundTag) {
        this(pager, aroundTag, false);
    }

    public K2StyleFactory(Pager pager, K2AroundTag aroundTag, boolean havePageStatistics) {
        super(pager);
        this.aroundTag = aroundTag;
        this.havePageStatistics = havePageStatistics;
    }

    @Override
    public DirectJump getDirectJump() {
        if (aroundTag.isContainForm()) {
            return new FormtDirectJump(this.getPager(), new FormtDirectJump.DirectJumpCss("jump", "jump-inp", "submit", "option"));
        }
        return NULL_DIRECT_JUMP;
    }

    @Override
    public PageStatistics getPageStatistics() {
        if (!havePageStatistics) {
            return NULL_PAGE_STSTISTICS;
        }
        return new PageStatistics() {

            @Override
            public void build(StringBuffer builder, int page, int pageCount) {
                // <span class="sum">第<b>1</b>/335页</span>
                builder.append("<span class=\"sum\">第<b>").append(page).append("</b>").append("/").append(pageCount).append("页</span>");
            }
        };
    }

    @Override
    public PageNumShowRule getPageNumShowRule() {
        return new AbstractPageNumShowRule.PageNumShowRule20101203(AbstractPageNumShowRule.K2_IGNORE_TAG);
    }

    @Override
    public NavigationStyle getNaviagationStyle() {
        return new NavigationStyle20101203() {

            @Override
            public AroundTag getAroundStyle() {
                return aroundTag;
            }

            @Override
            public String getCurrentPageTag(int page) {
                return "<strong class=\"current\">" + page + "</strong>";
            }

            @Override
            public void popNextPageLink(StringBuffer pageHtml, int page) {
                popDivHreLink(pageHtml, page, "下一页", "next");
            }

            @Override
            public void popPerPageLink(StringBuffer pageHtml, int page) {
                popDivHreLink(pageHtml, page, "上一页", "pre");
            }
        };
    }
}
