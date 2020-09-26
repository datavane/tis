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
package com.qlangtech.tis.manage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-16
 */
public class GroupChangeSnapshotForm {

    private String groupSnapshot;

    public String getGroupSnapshot() {
        return groupSnapshot;
    }

    public void setGroupSnapshot(String groupSnapshot) {
        this.groupSnapshot = groupSnapshot;
    }

    private static final Pattern pattern = Pattern.compile("(\\d+)-(\\d+)");

    public Integer getSnapshotId() {
        return getInt(1);
    }

    private Integer getInt(int group) {
        Matcher matcher = pattern.matcher(this.getGroupSnapshot());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(group));
        }
        return null;
    }

    public Integer getGroupId() {
        return getInt(2);
    }

    public static void main(String[] arg) {
        GroupChangeSnapshotForm form = new GroupChangeSnapshotForm();
        form.setGroupSnapshot("1-2");
        System.out.println(form.getSnapshotId());
        System.out.println(form.getGroupId());
    }
}
