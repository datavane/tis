/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.ibatis.BasicCriteria;

import java.text.MessageFormat;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-11 15:12
 */
public abstract class TISBaseCriteria extends BasicCriteria {

    private static final MessageFormat DB_DERBY_PAGINATION_FORMAT = new MessageFormat(" OFFSET {0} ROWS FETCH NEXT {1} ROWS ONLY");
    private static final MessageFormat DB_MYSQL_PAGINATION_FORMAT = new MessageFormat(" limit {0},{1}");

    public final String getPaginationScript() {

        Config.TisDbConfig dbCfg = Config.getDbCfg();
        if (Config.DB_TYPE_DERBY.equals(dbCfg.dbtype)) {
            return DB_DERBY_PAGINATION_FORMAT.format(new Object[]{this.getSkip(), this.getPageSize()});
        } else if (Config.DB_TYPE_MYSQL.equals(dbCfg.dbtype)) {
            return DB_MYSQL_PAGINATION_FORMAT.format(new Object[]{this.getSkip(), this.getPageSize()});
        } else {
            throw new IllegalStateException("illegal dbtype:" + dbCfg.dbtype);
        }
    }
}
