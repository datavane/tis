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
package com.qlangtech.tis.manage.common;

import org.apache.commons.dbcp.BasicDataSource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-26 11:47
 */
public class TISDataSource extends BasicDataSource {

    @Override
    protected synchronized DataSource createDataSource() throws SQLException {
        try {
            return super.createDataSource();
        } catch (Throwable e) {
            throw new RuntimeException(this.getUrl(), e);
        }
    }
}
