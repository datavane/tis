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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestTerms {

    public void testIteraveTerm() throws Exception {
        SolrCore core = TestEmbeddedSolrServer.server.getCoreContainer().getCore("menu");
        IndexReader rootreader = core.getSearcher().get().getIndexReader();
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
            terms = reader.terms("sale_out");
            termEnum = terms.iterator();
            while ((term = termEnum.next()) != null) {
            // writer.println(NumericUtils.getPrefixCodedLongShift(term));
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
