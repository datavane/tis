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

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-28
 */
public class TerminatorJndiObjectFactoryBean extends JndiObjectFactoryBean {

    private DataSource datasource;

    private TerminatorJndiObjectFactoryBean() {
        super();
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
        try {
            super.afterPropertiesSet();
        } catch (Throwable e) {
        // e.printStackTrace();
        }
    }

    @Override
    public Object getObject() {
        Object o = super.getObject();
        if (o == null) {
            if (this.getDatasource() != null) {
                return this.getDatasource();
            } else {
                throw new IllegalStateException(" you have not set datasource propertly");
            }
        }
        return o;
    }
}
