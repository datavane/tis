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

import com.koubei.web.tag.pager.Pager.PageNumShowRule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
