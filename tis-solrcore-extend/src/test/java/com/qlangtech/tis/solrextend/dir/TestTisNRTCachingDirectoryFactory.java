/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestTisNRTCachingDirectoryFactory extends TestCase {

    public void testDir() throws Exception {
        TisNRTCachingDirectoryFactory factory = new TisNRTCachingDirectoryFactory();
        NamedList<Object> namedList = new NamedList<Object>();
        DefaultTisCoreContext coreContext = new DefaultTisCoreContext();
        coreContext.setCoreName("search4OrderInfo_0");
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
