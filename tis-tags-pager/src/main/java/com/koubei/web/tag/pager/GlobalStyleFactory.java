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

import org.apache.commons.lang.StringUtils;
import com.koubei.web.tag.pager.Pager.DirectJump;
import com.koubei.web.tag.pager.Pager.NavigationStyle;
import com.koubei.web.tag.pager.Pager.PageNumShowRule;
import com.koubei.web.tag.pager.Pager.PageStatistics;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class GlobalStyleFactory extends StyleFactory {

    public GlobalStyleFactory(Pager pager) {
        super(pager);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.koubei.ring.util.StyleFactory#getDirectJump()
	 */
    @Override
    public DirectJump getDirectJump() {
        return new FormtDirectJump(this.getPager(), getDirectJumpCss());
    }

    protected FormtDirectJump.DirectJumpCss getDirectJumpCss() {
        return new FormtDirectJump.DirectJumpCss("yk-pagination-quick-jump", "yk-pagination-jump-inp", "yk-pagination-submit", null);
    }

    private final NavigationStyle naviagationStyle = new NavigationStyle20101203();

    @Override
    public NavigationStyle getNaviagationStyle() {
        return naviagationStyle;
    }

    protected class NavigationStyle20101203 implements NavigationStyle {

        @Override
        public AroundTag getAroundStyle() {
            return AroundTag.GlobalTag1;
        }

        @Override
        public String getCurrentPageTag(int page) {
            return "<strong class='yk-pagination-current'>" + page + "</strong>";
        }

        @Override
        public final void popDivHreLink(StringBuffer pageHtml, int page, String name) {
            popDivHreLink(pageHtml, page, name, StringUtils.EMPTY);
        }

        @Override
        public final void popDivHreLink(StringBuffer pageHtml, int page, String name, String cssClass) {
            pageHtml.append("<a href='").append(getPager().getUrl(page)).append("'");
            if (StringUtils.isNotEmpty(cssClass)) {
                pageHtml.append(" class='").append(cssClass).append("'");
            }
            pageHtml.append(" >");
            pageHtml.append(name).append("</a>");
        }

        @Override
        public void popNextPageLink(StringBuffer pageHtml, int page) {
            popDivHreLink(pageHtml, page, "下一页", "yk-pagination-next");
        }

        @Override
        public void popPerPageLink(StringBuffer pageHtml, int page) {
            popDivHreLink(pageHtml, page, "上一页", "yk-pagination-pre");
        }
    }

    @Override
    public PageNumShowRule getPageNumShowRule() {
        return new AbstractPageNumShowRule.PageNumShowRule20101203(AbstractPageNumShowRule.YK_IGNORE_TAG);
    }

    @Override
    public PageStatistics getPageStatistics() {
        return new PageStatistics() {

            @Override
            public void build(StringBuffer builder, int page, int pageCount) {
                builder.append("<span class='yk-pagination-sum'>共<b>").append(pageCount).append("</b>页</span>");
            }
        };
    }
}
