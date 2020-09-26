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
package com.qlangtech.tis.runtime.pojo;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.PropteryGetter;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-1-16
 */
public class ResSyn {

    private final String name;

    private final UploadResource daily;

    private final UploadResource online;

    private final PropteryGetter getter;

    public UploadResource getDaily() {
        return daily;
    }

    public UploadResource getOnline() {
        return online;
    }

    public ResSyn(String name, UploadResource daily, UploadResource online, PropteryGetter getter) {
        super();
        Assert.assertNotNull("getter can not be null", getter);
        Assert.assertNotNull("daily resource " + getter.getFileName() + " can not be null", daily);
        this.name = name;
        this.daily = daily;
        this.online = online;
        this.getter = getter;
    }

    public PropteryGetter getGetter() {
        return getter;
    }

    public String getName() {
        return name;
    }

    public boolean isSame() {
        if (online == null) {
            return false;
        }
        return StringUtils.equals(daily.getMd5Code(), online.getMd5Code());
    }
}
