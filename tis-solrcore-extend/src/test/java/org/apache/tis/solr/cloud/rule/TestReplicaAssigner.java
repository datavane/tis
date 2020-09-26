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
package org.apache.tis.solr.cloud.rule;

import java.util.Iterator;
import org.apache.solr.cloud.rule.ReplicaAssigner;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestReplicaAssigner extends TestCase {

    public void test1() {
        int count = 0;
        Iterator<int[]> it = ReplicaAssigner.permutations(2);
        int[] next = null;
        while (it.hasNext()) {
            next = it.next();
            for (int i : next) {
                System.out.print("," + i);
            }
            count++;
            System.out.println();
        }
        System.out.println("count:" + count);
    }
}
