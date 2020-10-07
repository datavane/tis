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
package com.qlangtech.tis.exec;

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.qlangtech.tis.sql.parser.TabPartitions;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月24日
 */
public class ExecChainContextUtils {

    public static final String PARTITION_DATA_PARAMS = "dateParams";

    private ExecChainContextUtils() {
    }

    /**
     * 取得全量構建依賴的全部表的Partition信息
     *
     * @param context
     * @return
     */
    public static TabPartitions getDependencyTablesPartitions(IJoinTaskContext context) {
        TabPartitions dateParams = context.getAttribute(PARTITION_DATA_PARAMS);
        if (dateParams == null) {
            throw new IllegalStateException("dateParams is not in context");
        }
        return dateParams;
    }

    public static void setDependencyTablesPartitions(IJoinTaskContext context, TabPartitions dateParams) {
        context.setAttribute(ExecChainContextUtils.PARTITION_DATA_PARAMS, dateParams);
    }


    public static ITabPartition getDependencyTablesMINPartition(IJoinTaskContext context) {
        TabPartitions dateParams = getDependencyTablesPartitions(context);
        Optional<ITabPartition> min = dateParams.getMinTablePartition();// dateParams.values().stream().min(Comparator.comparing((r) -> Long.parseLong(r.getPt())));
        if (!min.isPresent()) {
            return () -> context.getPartitionTimestamp();
        }
        return min.get();
    }
}
