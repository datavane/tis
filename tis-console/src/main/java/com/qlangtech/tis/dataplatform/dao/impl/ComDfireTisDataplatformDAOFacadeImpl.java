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
package com.qlangtech.tis.dataplatform.dao.impl;

import com.qlangtech.tis.dataplatform.dao.IComDfireTisDataplatformDAOFacade;
import com.qlangtech.tis.dataplatform.dao.IDsDatasourceDAO;
import com.qlangtech.tis.dataplatform.dao.IDsTableDAO;
import com.qlangtech.tis.dataplatform.dao.IMvnDependencyDAO;
import com.qlangtech.tis.dataplatform.dao.INobelAppDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ComDfireTisDataplatformDAOFacadeImpl implements IComDfireTisDataplatformDAOFacade {

    private final IDsTableDAO dsTableDAO;

    private final IDsDatasourceDAO dsDatasourceDAO;

    private final IMvnDependencyDAO mvnDependencyDAO;

    private final INobelAppDAO nobelAppDAO;

    public ComDfireTisDataplatformDAOFacadeImpl(IDsTableDAO dsTableDAO, IDsDatasourceDAO dsDatasourceDAO, IMvnDependencyDAO mvnDependencyDAO, INobelAppDAO nobelAppDAO) {
        this.dsTableDAO = dsTableDAO;
        this.dsDatasourceDAO = dsDatasourceDAO;
        this.mvnDependencyDAO = mvnDependencyDAO;
        this.nobelAppDAO = nobelAppDAO;
    }

    @Override
    public IMvnDependencyDAO getMvnDependencyDAO() {
        return this.mvnDependencyDAO;
    }

    @Override
    public INobelAppDAO getNobelAppDAO() {
        return this.nobelAppDAO;
    }

    public IDsTableDAO getDsTableDAO() {
        return this.dsTableDAO;
    }

    public IDsDatasourceDAO getDsDatasourceDAO() {
        return this.dsDatasourceDAO;
    }
}
