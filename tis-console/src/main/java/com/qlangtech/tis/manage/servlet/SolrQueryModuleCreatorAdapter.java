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
package com.qlangtech.tis.manage.servlet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-14
 */
public abstract class SolrQueryModuleCreatorAdapter implements SolrQueryModuleCreator {

    @Override
    public String[] getParameterValues(String keyname) {
        return new String[0];
    }

    @Override
    public void setQuerySelectServerCandiate(Map<String, List<ServerJoinGroup>> servers) {
    }

    @Override
    public boolean schemaAware() {
        return false;
    }
    // @Override
    // public boolean queryResultAware() {
    // return true;
    // }
}
