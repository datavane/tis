/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.koubei.web.tag.pager;

import com.koubei.web.tag.pager.Pager.DirectJump;
import com.koubei.web.tag.pager.Pager.PageStatistics;
import com.koubei.web.tag.pager.k2.K2AroundTag;
import com.koubei.web.tag.pager.k2.K2StyleFactory;

/**
 * 风格抽象工厂
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
        // }
        if ("k1".equalsIgnoreCase(schema)) {
            return new K2StyleFactory(pager, new K2AroundTag(IS_RIGHT_ALIGN_TRUE, HAS_NOT_DIRECT_JUMP), true);
        }
        // }
        throw new IllegalArgumentException("invalid schema value has not match[" + schema + "]");
    }
}
