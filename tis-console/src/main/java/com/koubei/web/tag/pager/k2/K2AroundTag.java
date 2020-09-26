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
package com.koubei.web.tag.pager.k2;

import java.text.MessageFormat;
import org.apache.commons.lang.StringUtils;
import com.koubei.web.tag.pager.AroundTag;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
