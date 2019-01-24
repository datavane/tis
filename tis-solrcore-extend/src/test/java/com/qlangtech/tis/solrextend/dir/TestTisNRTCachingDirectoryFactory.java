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
package com.qlangtech.tis.solrextend.dir;

import com.qlangtech.tis.solrextend.dir.impl.DefaultTisCoreContext;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.DirectoryFactory.DirContext;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestTisNRTCachingDirectoryFactory extends TestCase {

    public void testDir() throws Exception {
        TisNRTCachingDirectoryFactory factory = new TisNRTCachingDirectoryFactory();
        NamedList<Object> namedList = new NamedList<Object>();
        DefaultTisCoreContext coreContext = new DefaultTisCoreContext();
        coreContext.setCoreName("search4dfireOrderInfo_0");
        // namedList.add(TisSolrConfig.TIS_CORE_DESC, coreContext);
        namedList.add("hdfshost", "hdfs://10.1.6.211:9000");
        factory.init(namedList);
        // String indexpath = "D:\\home\\solr\\docpath";
        String indexpath = "D:\\home\\solr\\index\\-1";
        // (path,
        Directory directory = factory.get(indexpath, DirContext.DEFAULT, null);
        // lockFactory,
        // dirContext)
        // IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
        // .get(index)));
        IndexReader reader = DirectoryReader.open(directory);
        System.out.println(reader.numDocs());
        Document document = reader.document(1);
        for (IndexableField f : document.getFields()) {
            System.out.println(f.name() + ":" + f.stringValue());
        }
        // IndexSearcher searcher = new IndexSearcher(reader);
        // Analyzer analyzer = new StandardAnalyzer();
        // 
        // for (String l : directory.listAll()) {
        // System.out.println(l);
        // }
        reader.close();
        directory.close();
    }
}
