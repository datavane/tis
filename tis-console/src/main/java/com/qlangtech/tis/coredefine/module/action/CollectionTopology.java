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
package com.qlangtech.tis.coredefine.module.action;

import com.google.common.collect.Lists;
import org.apache.solr.common.cloud.Replica;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-06 15:16
 */
public class CollectionTopology {

    private List<Shared> shareds = Lists.newArrayList();

    public List<Shared> getShareds() {
        return this.shareds;
    }

    public void addShard(Shared shard) {
        this.shareds.add(shard);
    }

    public static class Shared {

        private final String name;

        public Shared(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private List<Replica> replics = Lists.newArrayList();

        public List<Replica> getReplics() {
            return this.replics;
        }

        public void addReplic(Replica replic) {
            this.replics.add(replic);
        }
    }
}
