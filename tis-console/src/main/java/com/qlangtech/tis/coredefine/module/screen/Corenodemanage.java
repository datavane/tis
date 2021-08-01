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
package com.qlangtech.tis.coredefine.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-7-11
 */
public class Corenodemanage extends BasicScreen {

    // private static final Logger log = LoggerFactory.getLogger(Corenodemanage.class);
    private static final long serialVersionUID = 1L;

    @Override
    @Func(PermissionConstant.APP_CORE_MANAGE_VIEW)
    public void execute(Context context) throws Exception {
    // this.enableChangeDomain(context);
    // if (!isIndexExist()) {
    // this.forward("coredefine_step1");
    // return;
    // }
    }

    public static class InstanceDirDesc {

        private String desc;

        private boolean valid;

        private long allcount;

        public long getAllcount() {
            return allcount;
        }

        public void setAllcount(long allcount) {
            this.allcount = allcount;
        }

        private final Map<String, List<ReplicState>> /* sharedName */
        collectionStat = new HashMap<>();

        public Map<String, List<ReplicState>> getCollectionStat() {
            return this.collectionStat;
        }

        public boolean isAllReplicValid() {
            for (List<ReplicState> replics : collectionStat.values()) {
                for (ReplicState r : replics) {
                    if (!r.isValid()) {
                        return false;
                    }
                }
            }
            return true;
        }

        public StringBuffer invalidDesc() {
            StringBuffer desc = new StringBuffer();
            for (List<ReplicState> replics : collectionStat.values()) {
                for (ReplicState r : replics) {
                    if (!r.isValid()) {
                        desc.append(r.getCoreName()).append("(").append(r.getNodeName()).append(")").append(r.getInvalidDesc());
                    }
                }
            }
            return desc;
        }

        /**
         * 添加一个副本的状态
         *
         * @param shardName
         * @param replicState
         */
        public void addReplicState(String shardName, ReplicState replicState) {
            List<ReplicState> replicList = this.collectionStat.get(shardName);
            if (replicList == null) {
                replicList = new ArrayList<>();
                this.collectionStat.put(shardName, replicList);
            }
            replicList.add(replicState);
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }

    public static class ReplicState {

        // ip地址
        private String nodeName;

        // "shard1_replica1"
        private String coreName;

        private String indexDir;

        // 节点是否正常
        private boolean valid;

        private String invalidDesc;

        private String state;

        private boolean leader;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public boolean isLeader() {
            return leader;
        }

        public void setLeader(boolean leader) {
            this.leader = leader;
        }

        public String getNodeName() {
            return nodeName;
        }

        public String getCoreName() {
            return coreName;
        }

        public void setCoreName(String coreName) {
            this.coreName = coreName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getIndexDir() {
            return indexDir;
        }

        public void setIndexDir(String indexDir) {
            this.indexDir = indexDir;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getInvalidDesc() {
            return invalidDesc;
        }

        public void setInvalidDesc(String invalidDesc) {
            this.invalidDesc = invalidDesc;
        }
    }

    /**
     * “SolrCore属性” 显示应用相关的属性
     *
     * @return
     */
    public boolean isShowServerRelateProp() {
        return false;
    }
}
