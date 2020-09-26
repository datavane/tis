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
