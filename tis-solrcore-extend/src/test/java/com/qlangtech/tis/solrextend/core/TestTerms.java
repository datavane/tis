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
