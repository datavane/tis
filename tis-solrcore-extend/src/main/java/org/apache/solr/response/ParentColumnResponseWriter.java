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
package org.apache.solr.response;

//import com.qlangtech.tis.solrextend.transformer.s4supplyCommodity.ParentDocTransformerFactory;
//import org.apache.lucene.index.LeafReader;
//import org.apache.lucene.index.LeafReaderContext;
//import org.apache.lucene.index.SortedDocValues;
//import org.apache.lucene.search.DocIdSetIterator;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.Sort;
//import org.apache.lucene.search.SortField;
//import org.apache.lucene.util.BitSetIterator;
//import org.apache.lucene.util.FixedBitSet;
//import org.apache.solr.common.SolrDocument;
//import org.apache.solr.common.params.ModifiableSolrParams;
//import org.apache.solr.common.params.SolrParams;
//import org.apache.solr.request.SolrQueryRequest;
//import org.apache.solr.request.SolrRequestInfo;
//import org.apache.solr.response.transform.DocTransformer;
//import org.apache.solr.response.transform.DocTransformers;
//import org.apache.solr.response.transform.TransformerFactory;
//import org.apache.solr.schema.FieldType;
//import org.apache.solr.schema.IndexSchema;
//import org.apache.solr.schema.SchemaField;
//import org.apache.solr.schema.StrField;
//import org.apache.solr.schema.TrieDoubleField;
//import org.apache.solr.schema.TrieFloatField;
//import org.apache.solr.schema.TrieIntField;
//import org.apache.solr.schema.TrieLongField;
//import org.apache.solr.search.DocList;
//import org.apache.solr.search.QueryParsing;
//import org.apache.solr.search.ReturnFields;
//import org.apache.solr.search.SolrIndexSearcher;
//import org.apache.solr.search.SolrReturnFields;
//import org.apache.solr.search.SortSpec;
//import org.apache.solr.search.StrParser;
//import org.apache.solr.search.SyntaxError;
//import java.io.IOException;
//import java.io.StringWriter;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/* *
 * FIXME:原始代码都已经移到了org.apache.solr.handler.export.ExportWriter 大部分逻辑需要修改
 * 先把这个类注释掉了
 * 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
//public class ParentColumnResponseWriter extends SortingResponseWriter {
//
//    static final Pattern PATTERN_WRITER = Pattern.compile("\"(\\w+)\":\"(\\w+)\"");
//
//    private static String val(Object o) {
//        if (o == null) {
//            return "";
//        }
//        if (!(o instanceof org.apache.lucene.document.Field)) {
//            return String.valueOf(o);
//        }
//        org.apache.lucene.document.Field f = (org.apache.lucene.document.Field) o;
//        return f.stringValue();
//    }
//
//    private static SolrDocument createSolrDocument(SortDoc sortDoc, List<LeafReaderContext> leaves, FieldWriter[] fieldWriters, FixedBitSet[] sets, String[] fields) throws IOException {
//        SolrDocument doc = new SolrDocument();
//        int ord = sortDoc.ord;
//        FixedBitSet set = sets[ord];
//        set.clear(sortDoc.docId);
//        LeafReaderContext context = leaves.get(ord);
//        int fieldIndex = 0;
//        int i = 0;
//        for (FieldWriter fieldWriter : fieldWriters) {
//            StringWriter out = new StringWriter();
//            if (fieldWriter.write(sortDoc.docId, context.reader(), out, fieldIndex)) {
//                ++fieldIndex;
//                String res = out.toString();
//                Matcher matcher = PATTERN_WRITER.matcher(res);
//                if (matcher.find()) {
//                    doc.addField(fields[i], matcher.group(2));
//                    ++i;
//                }
//            }
//        }
//        return doc;
//    }
//
//    public static String[] getFields(String fl, DocTransformers augmenters, List<String> extraParams, SolrQueryRequest req) throws IOException {
//        String[] fields = null;
//        StrParser sp = new StrParser(fl);
//        List<String> fieldsList = new ArrayList<>();
//        for (; ; ) {
//            sp.opt(',');
//            sp.eatws();
//            if (sp.pos >= sp.end)
//                break;
//            int start = sp.pos;
//            // short circuit test for a really simple field name
//            String key = null;
//            String field = SolrReturnFields.getFieldName(sp);
//            char ch = sp.ch();
//            if (field != null) {
//                if (sp.opt(':')) {
//                    // this was a key, not a field name
//                    key = field;
//                    field = null;
//                    sp.eatws();
//                    start = sp.pos;
//                } else {
//                    if (Character.isWhitespace(ch) || ch == ',' || ch == 0) {
//                        fieldsList.add(field);
//                        continue;
//                    }
//                    // an invalid field name... reset the position pointer to
//                    // retry
//                    sp.pos = start;
//                    field = null;
//                }
//            }
//            if (field == null) {
//                try {
//                    field = sp.getGlobbedId(null);
//                } catch (SyntaxError e) {
//                    e.printStackTrace();
//                }
//                ch = sp.ch();
//                if (field != null && (Character.isWhitespace(ch) || ch == ',' || ch == 0)) {
//                    continue;
//                }
//                // an invalid glob
//                sp.pos = start;
//            }
//            String funcStr = sp.val.substring(start);
//            if (funcStr.startsWith("[")) {
//                ModifiableSolrParams augmenterParams = new ModifiableSolrParams();
//                int end = 0;
//                try {
//                    end = QueryParsing.parseLocalParams(funcStr, 0, augmenterParams, req.getParams(), "[", ']');
//                } catch (SyntaxError e) {
//                    e.printStackTrace();
//                }
//                sp.pos += end;
//                String augmenterName = augmenterParams.get("type");
//                augmenterParams.remove("type");
//                String disp = key;
//                if (disp == null) {
//                    disp = '[' + augmenterName + ']';
//                }
//                TransformerFactory factory = req.getCore().getTransformerFactory(augmenterName);
//                if (factory != null) {
//                    DocTransformer t = factory.create(disp, augmenterParams, req);
//                    extraParams.add(augmenterParams.get(ParentDocTransformerFactory.EXTRA_FIELDS, null));
//                    if (t != null) {
//                        // if(!_wantsAllFields) {
//                        // String[] extra = t.getExtraRequestFields();
//                        // if(extra!=null) {
//                        // for(String f : extra) {
//                        // fields.add(f); // also request this field from
//                        // IndexSearcher
//                        // }
//                        // }
//                        // }
//                        augmenters.addTransformer(t);
//                    }
//                } else {
//                // throw new SolrException(ErrorCode.BAD_REQUEST, "Unknown
//                // DocTransformer: "+augmenterName);
//                }
//                // fieldsList.add(disp);
//                continue;
//            }
//        }
//        fields = (String[]) fieldsList.toArray(new String[fieldsList.size()]);
//        return fields;
//    }
//
//    @Override
//    public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse res) throws IOException {
//        Exception e1 = res.getException();
//        if (e1 != null) {
//            if (!(e1 instanceof IgnoreException)) {
//                writeException(e1, writer, false);
//            }
//            return;
//        }
//        SolrRequestInfo info = SolrRequestInfo.getRequestInfo();
//        SortSpec sortSpec = info.getResponseBuilder().getSortSpec();
//        Exception exception = null;
//        if (sortSpec == null) {
//            exception = new IOException(new SyntaxError("No sort criteria was provided."));
//        }
//        SolrIndexSearcher searcher = req.getSearcher();
//        Sort sort = searcher.weightSort(sortSpec.getSort());
//        if (sort == null) {
//            exception = new IOException(new SyntaxError("No sort criteria was provided."));
//        }
//        if (sort != null && sort.needsScores()) {
//            exception = new IOException(new SyntaxError("Scoring is not currently supported with xsort."));
//        }
//        FixedBitSet[] sets = (FixedBitSet[]) req.getContext().get("export");
//        Integer th = (Integer) req.getContext().get("totalHits");
//        if (sets == null) {
//            exception = new IOException(new SyntaxError("xport RankQuery is required for xsort: rq={!xport}"));
//        }
//        int totalHits = th.intValue();
//        SolrParams params = req.getParams();
//        String fl = params.get("fl");
//        String[] fields = null;
//        DocTransformers augmenters = new DocTransformers();
//        List<String> extraParams = new ArrayList<>();
//        if (fl == null) {
//            exception = new IOException(new SyntaxError("export field list (fl) must be specified."));
//        } else {
//            fields = getFields(fl, augmenters, extraParams, req);
//        }
//        FieldWriter[] fieldWriters = null;
//        try {
//            fieldWriters = getFieldWriters(fields, req.getSearcher());
//        } catch (Exception e) {
//            exception = e;
//        }
//        if (exception != null) {
//            writeException(exception, writer, true);
//            return;
//        }
//        writer.write("{\"responseHeader\": {\"status\": 0}, \"response\":{\"numFound\":" + totalHits + ", \"docs\":[");
//        // Write the data.
//        List<LeafReaderContext> leaves = req.getSearcher().getTopReaderContext().leaves();
//        SortDoc sortDoc = getSortDoc(req.getSearcher(), sort.getSort());
//        int count = 0;
//        int queueSize = 30000;
//        SortQueue queue = new SortQueue(queueSize, sortDoc);
//        SortDoc[] outDocs = new SortDoc[queueSize];
//        final ResultContext context = new ResultContext() {
//
//            @Override
//            public SolrIndexSearcher getSearcher() {
//                return req.getSearcher();
//            }
//
//            @Override
//            public ReturnFields getReturnFields() {
//                return null;
//            }
//
//            @Override
//            public SolrQueryRequest getRequest() {
//                return null;
//            }
//
//            @Override
//            public Query getQuery() {
//                return null;
//            }
//
//            @Override
//            public DocList getDocList() {
//                return null;
//            }
//        };
//        boolean commaNeeded = false;
//        while (count < totalHits) {
//            queue.reset();
//            SortDoc top = queue.top();
//            for (int i = 0; i < leaves.size(); i++) {
//                LeafReaderContext leaf = leaves.get(i);
//                sortDoc.setNextReader(leaf);
//                int docBase = leaf.docBase;
//                DocIdSetIterator it = new BitSetIterator(sets[i], 0);
//                int docId = -1;
//                while ((docId = it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
//                    sortDoc.setValues(docId);
//                    if (top.lessThan(sortDoc)) {
//                        top.setValues(sortDoc);
//                        top = queue.updateTop();
//                    }
//                }
//            }
//            int outDocsIndex = -1;
//            for (int i = 0; i < queueSize; i++) {
//                SortDoc s = queue.pop();
//                if (s.docId > -1) {
//                    outDocs[++outDocsIndex] = s;
//                }
//            }
//            count += (outDocsIndex + 1);
//            try {
//                // initial transformer
//                DocTransformer transformer = null;
//                String extraParam = null;
//                String[] extraFields = null;
//                final boolean needTransform = augmenters.size() > 0;
//                if (needTransform) {
//                    transformer = augmenters.getTransformer(0);
//                    transformer.setContext(context);
//                    extraParam = extraParams.get(0);
//                    if (extraParam != null)
//                        extraFields = extraParam.split(",");
//                }
//                for (int i = outDocsIndex; i >= 0; --i) {
//                    SortDoc s = outDocs[i];
//                    if (commaNeeded) {
//                        writer.write(',');
//                    }
//                    writer.write('{');
//                    writeDoc(s, leaves, fieldWriters, sets, writer);
//                    // add parent's columns
//                    if (needTransform) {
//                        SolrDocument childDoc = createSolrDocument(s, leaves, fieldWriters, sets, fields);
//                        transformer.transform(childDoc, s.docId + leaves.get(s.ord).docBase, 0);
//                        if (extraFields != null && extraFields.length > 0) {
//                            for (String fName : extraFields) {
//                                String fValue = val(childDoc.getFieldValue(fName));
//                                writer.write(',');
//                                writer.write('"');
//                                writer.write(fName);
//                                writer.write('"');
//                                writer.write(':');
//                                writer.write(fValue);
//                            }
//                        }
//                    }
//                    writer.write('}');
//                    commaNeeded = true;
//                    s.reset();
//                }
//            } catch (Throwable e) {
//                Throwable ex = e;
//                e.printStackTrace();
//                while (ex != null) {
//                    String m = ex.getMessage();
//                    if (m != null && m.contains("Broken pipe")) {
//                        throw new IgnoreException();
//                    }
//                    ex = ex.getCause();
//                }
//                if (e instanceof IOException) {
//                    throw ((IOException) e);
//                } else {
//                    throw new IOException(e);
//                }
//            }
//        }
//        // System.out.println("Sort Time 2:"+Long.toString(total/1000000));
//        writer.write("]}}");
//        writer.flush();
//    }
//
//    private SortDoc getSortDoc(SolrIndexSearcher searcher, SortField[] sortFields) throws IOException {
//        SortValue[] sortValues = new SortValue[sortFields.length];
//        IndexSchema schema = searcher.getSchema();
//        for (int i = 0; i < sortFields.length; ++i) {
//            SortField sf = sortFields[i];
//            String field = sf.getField();
//            boolean reverse = sf.getReverse();
//            SchemaField schemaField = schema.getField(field);
//            FieldType ft = schemaField.getType();
//            if (!schemaField.hasDocValues()) {
//                throw new IOException(field + " must have DocValues to use this feature.");
//            }
//            if (ft instanceof TrieIntField) {
//                if (reverse) {
//                    sortValues[i] = new IntValue(field, new IntDesc());
//                } else {
//                    sortValues[i] = new IntValue(field, new IntAsc());
//                }
//            } else if (ft instanceof TrieFloatField) {
//                if (reverse) {
//                    sortValues[i] = new FloatValue(field, new FloatDesc());
//                } else {
//                    sortValues[i] = new FloatValue(field, new FloatAsc());
//                }
//            } else if (ft instanceof TrieDoubleField) {
//                if (reverse) {
//                    sortValues[i] = new DoubleValue(field, new DoubleDesc());
//                } else {
//                    sortValues[i] = new DoubleValue(field, new DoubleAsc());
//                }
//            } else if (ft instanceof TrieLongField) {
//                if (reverse) {
//                    sortValues[i] = new LongValue(field, new LongDesc());
//                } else {
//                    sortValues[i] = new LongValue(field, new LongAsc());
//                }
//            } else if (ft instanceof StrField) {
//                LeafReader reader = searcher.getLeafReader();
//                SortedDocValues vals = reader.getSortedDocValues(field);
//                if (reverse) {
//                    sortValues[i] = new StringValue(vals, field, new IntDesc());
//                } else {
//                    sortValues[i] = new StringValue(vals, field, new IntAsc());
//                }
//            } else {
//                throw new IOException("Sort fields must be one of the following types: int,float,long,double,string");
//            }
//        }
//        if (sortValues.length == 1) {
//            return new SingleValueSortDoc(sortValues[0]);
//        } else if (sortValues.length == 2) {
//            return new DoubleValueSortDoc(sortValues[0], sortValues[1]);
//        } else if (sortValues.length == 3) {
//            return new TripleValueSortDoc(sortValues[0], sortValues[1], sortValues[2]);
//        } else if (sortValues.length == 4) {
//            return new QuadValueSortDoc(sortValues[0], sortValues[1], sortValues[2], sortValues[3]);
//        } else {
//            throw new IOException("A max of 4 sorts can be specified");
//        }
//    }
//
//    class SortQueue extends PriorityQueue<SortDoc> {
//
//        private SortDoc proto;
//
//        private Object[] cache;
//
//        public SortQueue(int len, SortDoc proto) {
//            super(len);
//            this.proto = proto;
//        }
//
//        protected boolean lessThan(SortDoc t1, SortDoc t2) {
//            return t1.lessThan(t2);
//        }
//
//        private void populate() {
//            Object[] heap = getHeapArray();
//            cache = new SortDoc[heap.length];
//            for (int i = 1; i < heap.length; i++) {
//                cache[i] = heap[i] = proto.copy();
//            }
//            size = maxSize;
//        }
//
//        private void reset() {
//            Object[] heap = getHeapArray();
//            if (cache != null) {
//                System.arraycopy(cache, 1, heap, 1, heap.length - 1);
//                size = maxSize;
//            } else {
//                populate();
//            }
//        }
//    }
//}
