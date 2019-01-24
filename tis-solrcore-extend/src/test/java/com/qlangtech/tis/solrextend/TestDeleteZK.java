/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.solrextend;

import com.qlangtech.tis.TisZkClient;
import junit.framework.TestCase;
import java.util.List;

/* *
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
