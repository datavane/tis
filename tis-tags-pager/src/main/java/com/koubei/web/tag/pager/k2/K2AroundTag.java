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

import java.text.MessageFormat;
import org.apache.commons.lang.StringUtils;
import com.koubei.web.tag.pager.AroundTag;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class K2AroundTag extends AroundTag {

    private boolean isRightAlign;

    private boolean containForm;

    // k2-pagination-right <div class=\"option\">
    private static final MessageFormat START_TAGE_FORMAT = new MessageFormat("<div class=\"k2-pagination{0}\">{1}");

    public K2AroundTag(boolean isRightAlign, boolean containForm) {
        super();
        this.isRightAlign = isRightAlign;
        this.containForm = containForm;
    }

    public boolean isContainForm() {
        return containForm;
    }

    /**
     * 是否右对齐
     *
     * @param isRightAlign
     */
    public K2AroundTag(boolean isRightAlign) {
        this(isRightAlign, false);
    }

    @Override
    public String getEnd() {
        return (isRightAlign && !containForm) ? "</div></div>" : "</div>";
    }

    @Override
    public String getStart() {
        Object[] args = isRightAlign ? new Object[] { " k2-pagination-right", !this.containForm ? "<div class=\"option\">" : StringUtils.EMPTY } : new Object[] { StringUtils.EMPTY, StringUtils.EMPTY };
        // "<div class=\"k2-pagination k2-pagination-right\"><div class=\"option\">";
        return START_TAGE_FORMAT.format(args);
    }

    public static void main(String[] arg) {
        K2AroundTag tag = new K2AroundTag(false, true);
        System.out.println(tag.getStart());
        System.out.println("=======");
        System.out.println(tag.getEnd());
    }
}
