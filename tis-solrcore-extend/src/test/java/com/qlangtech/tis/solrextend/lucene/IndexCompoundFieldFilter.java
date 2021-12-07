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
package com.qlangtech.tis.solrextend.lucene;

import org.apache.lucene.index.*;
import org.apache.lucene.index.TermsEnum.SeekStatus;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexCompoundFieldFilter {

    public static void main(String[] args) throws Exception {
        String index = "D:\\home\\solr\\dynamicinfo";
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        IndexReaderContext readerContext = searcher.getTopReaderContext();
        FixedBitSet bitSet = new FixedBitSet(readerContext.reader().maxDoc());
        final Pattern DYNAMIC_INFO = Pattern.compile("^(\\d+?)_(\\d+?)_(\\d+?)_(.+?)_(\\d+?)_.*");
        Map<Integer, StaticReduce> /* doc id */
        buyerStatis = new HashMap<>();
        int docBase;
        LeafReader reader = null;
        Bits liveDocs = null;
        Terms terms = null;
        TermsEnum termEnum = null;
        PostingsEnum posting = null;
        for (LeafReaderContext leaf : readerContext.leaves()) {
            docBase = leaf.docBase;
            reader = leaf.reader();
            liveDocs = reader.getLiveDocs();
            terms = reader.terms("dynamic_info");
            termEnum = terms.iterator();
            String prefixStart = "94298837_20150720";
            String prefixEnd = "94298837_20150720";
            String termStr = null;
            int docid = -1;
            if ((termEnum.seekCeil(new BytesRef(prefixStart))) != SeekStatus.END) {
                do {
                    Matcher matcher = DYNAMIC_INFO.matcher(termStr = termEnum.term().utf8ToString());
                    if (!matcher.matches()) {
                        continue;
                    }
                    posting = termEnum.postings(posting);
                    docid = posting.nextDoc();
                    if (!(docid != PostingsEnum.NO_MORE_DOCS && (liveDocs == null || (liveDocs != null && liveDocs.get(docid))))) {
                        continue;
                    }
                    if ((matcher.group(1) + "_" + matcher.group(2)).compareTo(prefixEnd) > 0) {
                        break;
                    }
                    // StaticReduce statis =
                    addStatis(buyerStatis, docBase, docid, matcher);
                } while (termEnum.next() != null);
                for (StaticReduce statis : buyerStatis.values()) {
                    if (statis.payCount > Integer.MAX_VALUE || statis.paymentSum > 1) {
                        System.out.println("count:" + statis.payCount + ",sum:" + statis.paymentSum);
                        bitSet.set(statis.luceneDocId);
                    }
                }
                BitDocIdSet docIdSet = new BitDocIdSet(bitSet);
                DocIdSetIterator it = docIdSet.iterator();
            // BitQuery query = new BitQuery(it);
            //
            // TopDocs topdocs = searcher.search(query, 999999);
            //
            // for (ScoreDoc doc : topdocs.scoreDocs) {
            //
            // Document document = indexReader.document(doc.doc);
            //
            // String[] values = document.getValues("dynamic_info");
            // for (String v : values) {
            // System.out.println(v);
            // }
            //
            // System.out.println();
            // }
            // docid = it.nextDoc();
            // while (docid != DocIdSetIterator.NO_MORE_DOCS) {
            // System.out.println(docid);
            // docid = it.nextDoc();
            // }
            // while () {
            // if (term.utf8ToString().startsWith("94298837_20150719")) {
            //
            // }
            //
            // }
            // posting = termEnum.postings(posting);
            // int doc = posting.nextDoc();
            //
            // while (doc != PostingsEnum.NO_MORE_DOCS) {
            //
            // document = reader.document(doc + docBase);
            //
            // for (IndexableField field : document
            // .getFields("dynamic_info")) {
            //
            // System.out.println(field.stringValue());
            // }
            //
            // posting.nextDoc();
            // }
            // bitSet.set(doc + docBase);
            }
        }
        indexReader.close();
    }

    /**
     * @param buyerStatis
     * @param docBase
     * @param docid
     * @param matcher
     * @return
     */
    private static StaticReduce addStatis(Map<Integer, StaticReduce> buyerStatis, int docBase, int docid, Matcher matcher) {
        StaticReduce statis = buyerStatis.get(docBase + docid);
        if (statis == null) {
            statis = new StaticReduce(docBase + docid, Long.parseLong(matcher.group(3)));
            buyerStatis.put(docBase + docid, statis);
        }
        if (statis.buyerId != Long.parseLong(matcher.group(3))) {
            return statis;
        }
        try {
            statis.addPayCount(Integer.parseInt(matcher.group(5)));
        } catch (Exception e) {
        }
        try {
            statis.addPayment(Float.parseFloat(matcher.group(4)));
        } catch (Exception e) {
        }
        return statis;
    }

    private static class StaticReduce {

        final long buyerId;

        final int luceneDocId;

        int payCount;

        float paymentSum;

        /**
         * @param luceneDocId
         */
        public StaticReduce(int luceneDocId, long buyerId) {
            super();
            this.luceneDocId = luceneDocId;
            this.buyerId = buyerId;
        }

        public void addPayCount(int addCount) {
            this.payCount += addCount;
        }

        public void addPayment(float payment) {
            this.paymentSum += payment;
        }
    }
}
