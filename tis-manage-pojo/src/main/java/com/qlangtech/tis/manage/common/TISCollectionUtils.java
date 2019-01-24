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
package com.qlangtech.tis.manage.common;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISCollectionUtils {

    private static Set<String> monitorIgnore;

    static {
        monitorIgnore = new HashSet<String>();
        monitorIgnore.add(StringUtils.lowerCase("search4Supplier"));
        monitorIgnore.add(StringUtils.lowerCase("search4WarehouseStock"));
        monitorIgnore.add(StringUtils.lowerCase("search4MenuProfitsNew"));
        monitorIgnore.add(StringUtils.lowerCase("search4TimeStatistic"));
        monitorIgnore.add(StringUtils.lowerCase("search4OperationStatistic"));
        monitorIgnore.add(StringUtils.lowerCase("search4GoodsRaw"));
        monitorIgnore.add(StringUtils.lowerCase("search4shopActivity"));
    // 以下这个索引是因为现在更新量实在太低了 还没有正式使用
    // monitorIgnore.add(StringUtils.lowerCase("search4shop"));
    // monitorIgnore.add(StringUtils.lowerCase("search4supplyGoods"));
    }

    private static boolean monitorIgnore(String collection) {
        return monitorIgnore.contains(StringUtils.lowerCase(collection));
    }

    /**
     * 是否忽略增量执行
     *
     * @return
     */
    public static boolean ignoreIncrTransfer(String collection) {
        return UISVersion.isDataCenterCollection(collection) || monitorIgnore(collection);
    }
}
