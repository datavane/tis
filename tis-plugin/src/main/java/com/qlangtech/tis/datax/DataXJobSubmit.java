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

package com.qlangtech.tis.datax;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteJobTrigger;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.tis.hadoop.rpc.RpcServiceReference;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 17:03
 **/
public abstract class DataXJobSubmit {

    public static Callable<DataXJobSubmit> mockGetter;

    public static Optional<DataXJobSubmit> getDataXJobSubmit(DataXJobSubmit.InstanceType expectDataXJobSumit) {
        try {
            if (mockGetter != null) {
                return Optional.ofNullable(mockGetter.call());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ExtensionList<DataXJobSubmit> jobSumits = TIS.get().getExtensionList(DataXJobSubmit.class);
        Optional<DataXJobSubmit> jobSubmit = jobSumits.stream()
                .filter((jsubmit) -> (expectDataXJobSumit) == jsubmit.getType()).findFirst();
        return jobSubmit;
    }

    public enum InstanceType {
        DISTRIBUTE, LOCAL
    }


    public abstract InstanceType getType();

    /**
     * 创建dataX任务
     *
     * @param taskContext
     * @param dataXfileName
     * @return
     */
    public abstract IRemoteJobTrigger createDataXJob(IJoinTaskContext taskContext
            , RpcServiceReference statusRpc, IDataxProcessor dataxProcessor, String dataXfileName);


}
