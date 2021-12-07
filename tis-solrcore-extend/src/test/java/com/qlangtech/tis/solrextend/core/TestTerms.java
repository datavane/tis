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
package com.qlangtech.tis.solrextend.core;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.core.SolrCore;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestTerms {

    public static void readTerms(DirectoryReader rootreader) throws Exception {
        // SolrCore core =
        // TestEmbeddedSolrServer.server.getCoreContainer().getCore("menu");
        // IndexReader rootreader = core.getSearcher().get().getIndexReader();
        LeafReader reader = null;
        Bits liveDocs = null;
        Terms terms = null;
        TermsEnum termEnum = null;
        PostingsEnum posting = null;
        BytesRef term = null;
        int docid;
        final Charset utf8 = Charset.forName("utf8");
        OutputStream output = FileUtils.openOutputStream(new File(TestEmbeddedSolrServer.solrHome, "terms.txt"));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, utf8));
        for (LeafReaderContext leaf : rootreader.getContext().leaves()) {
            reader = leaf.reader();
            liveDocs = reader.getLiveDocs();
            terms = reader.terms("message_content");
            termEnum = terms.iterator();
            while ((term = termEnum.next()) != null) {
                writer.println(term.utf8ToString());
            // writer.println(term.utf8ToString());
            }
        // termEnum.next();
        // do {
        // posting = termEnum.postings(posting);
        // term = termEnum.term().utf8ToString();
        //
        // System.out.println(term);
        //
        // docid = posting.nextDoc();
        //
        // } while ((docid != PostingsEnum.NO_MORE_DOCS && (liveDocs == null
        // || (liveDocs != null && liveDocs.get(docid)))));
        }
        writer.flush();
        writer.close();
    }
}
