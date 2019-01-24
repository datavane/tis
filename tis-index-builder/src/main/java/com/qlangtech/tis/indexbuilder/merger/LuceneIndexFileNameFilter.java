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
package com.qlangtech.tis.indexbuilder.merger;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
class LuceneIndexFileNameFilter implements PathFilter {

    private static final LuceneIndexFileNameFilter singleton = new LuceneIndexFileNameFilter();

    // private final IndexFileNameFilter luceneFilter;
    public static LuceneIndexFileNameFilter getFilter() {
        return singleton;
    }

    private LuceneIndexFileNameFilter() {
    // this.luceneFilter = IndexFileNameFilter.getFilter();
    }

    public boolean accept(Path path) {
        // return this.luceneFilter.accept(null, path.getName());
        return true;
    }
}
