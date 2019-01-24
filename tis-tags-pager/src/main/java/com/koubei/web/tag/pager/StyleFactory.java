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

import com.koubei.web.tag.pager.Pager.DirectJump;
import com.koubei.web.tag.pager.Pager.PageStatistics;
import com.koubei.web.tag.pager.k2.K2AllRowsCountStyleFactory;
import com.koubei.web.tag.pager.k2.K2AroundTag;
import com.koubei.web.tag.pager.k2.K2StyleFactory;

/*
 * 风格抽象工厂
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class StyleFactory {

    public abstract Pager.NavigationStyle getNaviagationStyle();

    public abstract Pager.PageStatistics getPageStatistics();

    public abstract Pager.DirectJump getDirectJump();

    public abstract Pager.PageNumShowRule getPageNumShowRule();

    public static final DirectJump NULL_DIRECT_JUMP = new DirectJump() {

        public void build(StringBuffer builder) {
            return;
        }

        public AroundTag getAroundTag() {
            return AroundTag.NULL;
        }
    };

    public static final PageStatistics NULL_PAGE_STSTISTICS = new PageStatistics() {

        @Override
        public void build(StringBuffer builder, int page, int pageCount) {
            return;
        }
    };

    // public static final PageNumShowRule COMMON_PAGE_NUM_SHOW_RULE =
    private final Pager pager;

    public StyleFactory(Pager pager) {
        this.pager = pager;
    }

    protected Pager getPager() {
        return this.pager;
    }

    private static final boolean IS_RIGHT_ALIGN_TRUE = true;

    private static final boolean IS_RIGHT_ALIGN_FALSE = !IS_RIGHT_ALIGN_TRUE;

    private static final boolean HAS_DIRECT_JUMP = true;

    private static final boolean HAS_NOT_DIRECT_JUMP = !HAS_DIRECT_JUMP;

    public static StyleFactory getInstance(final Pager pager) {
        final String schema = pager.getSchema();
        if ("g1".equalsIgnoreCase(schema)) {
            return new GlobalStyleFactory1(pager);
        }
        if ("g2".equalsIgnoreCase(schema)) {
            return new GlobalStyleFactory2(pager);
        }
        if ("g3".equalsIgnoreCase(schema)) {
            return new GlobalStyleFactory3(pager);
        }
        if ("g4".equalsIgnoreCase(schema)) {
            return new GlobalStyleFactory4(pager);
        }
        if ("k1".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(IS_RIGHT_ALIGN_TRUE, HAS_NOT_DIRECT_JUMP), true);
        }
        if ("k2".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(false, false), true);
        }
        if ("k3".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(true, true));
        }
        if ("k4".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(false, true));
        }
        if ("k5".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(true, false));
        }
        if ("k6".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(false, false));
        }
        if ("k7".equalsIgnoreCase(schema)) {
            return new K2AllRowsCountStyleFactory(pager, new K2AroundTag(IS_RIGHT_ALIGN_TRUE, HAS_NOT_DIRECT_JUMP));
        }
        throw new IllegalArgumentException("invalid schema value has not match[" + schema + "]");
    }
}
