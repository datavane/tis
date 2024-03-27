/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.datax.job;

import com.qlangtech.tis.coredefine.module.action.TargetResName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-01-17 15:11
 **/
public class FlinkSessionResName {
    private static final TargetResName K8S_FLINK_CLUSTER_NAME = new TargetResName("flink-cluster");

    public static final TargetResName group() {
        return K8S_FLINK_CLUSTER_NAME;
    }

    public boolean match(TargetResName targetName) {
        return match(targetName.getName());
    }

    public boolean match(String targetName) {
//        String[] split = StringUtils.split(targetName, "/");
//        if (split.length != 2) {
//            return false;
//        }
//        return StringUtils.equals(split[0], K8S_FLINK_CLUSTER_NAME.getName());
        return matchPair(targetName).getLeft();
    }

    private Pair<Boolean, String> matchPair(String targetName) {
        String[] split = StringUtils.split(targetName, "/");
        final int splitLength = split.length;
        if (splitLength < 1) {
            return Pair.of(false, null);
        }
        boolean containVal = (splitLength > 1);
        return Pair.of(StringUtils.equals(split[0], K8S_FLINK_CLUSTER_NAME.getName()), (containVal ? split[1] : null));
    }

    /**
     * 传入"flink-cluster/xxx" 转成 "xxx"
     *
     * @param targetName
     * @return
     */
    public TargetResName resName(TargetResName targetName) {
        Pair<Boolean, String> p = matchPair(targetName.getName());
        if (!p.getLeft() || StringUtils.isEmpty(p.getRight())) {
            throw new IllegalStateException("can not find targetRes name:" + targetName);
        }
        return new TargetResName(p.getValue());
    }
}
