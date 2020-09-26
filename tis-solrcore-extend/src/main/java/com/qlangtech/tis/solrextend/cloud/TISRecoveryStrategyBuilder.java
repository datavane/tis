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
package com.qlangtech.tis.solrextend.cloud;

import org.apache.solr.cloud.RecoveryStrategy;
import org.apache.solr.cloud.RecoveryStrategy.RecoveryListener;
import org.apache.solr.common.cloud.ZkNodeProps;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月30日
 */
public class TISRecoveryStrategyBuilder extends RecoveryStrategy.Builder {

    @Override
    protected RecoveryStrategy newRecoveryStrategy(CoreContainer cc, CoreDescriptor cd, RecoveryListener recoveryListener) {
        return new TISRecoveryStrategy(cc, cd, recoveryListener);
    }

    private class TISRecoveryStrategy extends RecoveryStrategy {

        protected TISRecoveryStrategy(CoreContainer cc, CoreDescriptor cd, RecoveryListener recoveryListener) {
            super(cc, cd, recoveryListener);
        }
    }
}
