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
package com.qlangtech.tis.solrextend;

import com.qlangtech.tis.TisZkClient;
import junit.framework.TestCase;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestDeleteZK extends TestCase {

    public void test() throws Exception {
        TisZkClient zk = new TisZkClient("10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud", 5000);
        String root = "/collections/search4_thin_menu";
        deletePath(zk, root);
        root = "/collections/search4_thin_warehouse";
        deletePath(zk, root);
    }

    private void deletePath(TisZkClient zk, String path) throws Exception {
        List<String> child = zk.getChildren(path, null, true);
        if (child.size() > 0) {
            for (String p : child) {
                deletePath(zk, path + "/" + p);
            }
        }
        System.out.println("delete:" + path);
        zk.delete(path, -1, false);
    }
}
