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
package com.qlangtech.tis.solrextend.fieldtype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.uninverting.UninvertingReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BitwiseField extends FieldType {

    private static final Logger LOG = LoggerFactory.getLogger(BitwiseField.class);

    // private static final WhitespaceAnalyzer spaceAnalyzer = new
    // WhitespaceAnalyzer();
    public static final List<Long> masks;

    static {
        long longval = 1;
        masks = new ArrayList<Long>();
        while (longval > 0) {
            masks.add(longval);
            longval = longval << 1;
        }
    }

    // private static final ThreadLocal<List<Long>> arbitaryListThreadLocal =
    // new
    // ThreadLocal<List<Long>>() {
    // @Override
    // protected List<Long> initialValue() {
    // List<Long> arbitaryList = new ArrayList<Long>();
    // return arbitaryList;
    // }
    // };
    // = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40,
    // 0x80, 0x100, 0x200, 0x400, 0x800, 0x1000, 0x2000, 0x4000, 0x8000,
    // 0x10000, 0x20000, 0x40000, 0x80000, 0x100000, 0x200000, 0x400000,
    // 0x800000, 0x1000000, 0x2000000, 0x4000000, 0x8000000, 0x10000000,
    // 0x20000000, 0x40000000, 0x80000000 };
    private final int radix = 10;

    public BitwiseField() {
        super();
    // properties = (properties | INDEXED | TOKENIZED | BINARY | STORED);
    }

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    // String swidth = args.get("width");
    // int width = 32;
    // if (swidth != null) {
    // try {
    // width = Integer.parseInt(swidth);
    // if (width > 32) {
    // LOG.warn("Width > 32, resetting to 32.");
    // width = 32;
    // }
    // } catch (Exception e) {
    // LOG.warn("Invalid width '" + swidth + "', using 32.");
    // width = 32;
    // }
    // }
    // String sradix = args.get("radix");
    // if (sradix != null) {
    // try {
    // radix = Integer.parseInt(sradix);
    // } catch (Exception e) {
    // 
    // }
    // } else {
    // radix = 10;
    // }
    // initialize(width, radix);
    }

    // void initialize(int width, int radix) {
    // // this.width = width;
    // this.radix = radix;
    // //this.mask = 0xffffffff >>> (32 - width);
    // }
    // protected IndexableField createField(String name, String val,
    // org.apache.lucene.document.FieldType type, float boost){
    // Field f = new Field(name, val, type);
    // f.setBoost(boost);
    // return f;
    // }
    /**
     * Whitespace-separated list of integers represented in {@link #radix} base.
     * <p>
     * NOTE: currently this implementation supports only one int per field.
     * </p>
     */
    @Override
    protected IndexableField createField(String name, String externalVal, org.apache.lucene.index.IndexableFieldType type) {
        long val;
        try {
            val = Long.parseLong(externalVal, radix);
        } catch (Exception e) {
            LOG.warn("invalid value " + externalVal, e);
            return null;
        }
        List<Long> arbitaryList = new ArrayList<Long>(0);
        // arbitaryList.clear();
        for (int i = 0; i < masks.size(); i++) {
            if (i > 0) {
            // sb.append(' ');
            }
            if (val < (masks.get(i) - 1)) {
                break;
            }
            if ((val & masks.get(i)) != 0) {
                // sb.append(String.valueOf(masks.get(i)));
                arbitaryList.add(masks.get(i));
            }
        }
        // TokenStream ts = spaceAnalyzer.tokenStream(name, new
        // StringReader(sb.toString()));
        // ts.reset();
        final int arbitaryListLength = arbitaryList.size();
        byte[] arr = toArray(val);
        if (!type.tokenized()) {
            throw new IllegalStateException("type.tokenized shall be true");
        }
        StoredField f = new StoredField(name, new BytesRef(arr), new org.apache.lucene.document.FieldType(type));
        f.setTokenStream(new TokenStream() {

            private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

            int index = 0;

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                if (index >= arbitaryListLength) {
                    return false;
                }
                try {
                    termAtt.setEmpty().append(String.valueOf(arbitaryList.get(index++)));
                } catch (Throwable e) {
                }
                return true;
            }
        });
        // f.setBoost(boost);
        return f;
    }

    @Override
    public UninvertingReader.Type getUninversionType(SchemaField field) {
        return UninvertingReader.Type.SORTED;
    }

    @Override
    public ValueSource getValueSource(SchemaField field, QParser parser) {
        return new LongFieldSource(field.getName());
    }

    @Override
    public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
        // baisui modfiy for user can alter the occur in qparse
        final Occur occur = "OR".equals(parser.getParam(QueryParsing.OP)) ? Occur.SHOULD : Occur.MUST;
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        long val;
        try {
            val = Long.parseLong(externalVal, radix);
        } catch (Exception e) {
            LOG.warn("Invalid value " + externalVal);
            return null;
        }
        for (int i = 0; i < masks.size(); i++) {
            if (val < (masks.get(i) - 1)) {
                break;
            }
            if ((val & masks.get(i)) != 0) {
                bq.add(new TermQuery(new Term(field.getName(), String.valueOf(masks.get(i)))), occur);
            }
        }
        return bq.build();
    }

    @Override
    public SortField getSortField(SchemaField field, boolean top) {
        return new SortField(field.getName(), SortField.Type.LONG, top);
    }

    @Override
    public String toExternal(IndexableField f) {
        BytesRef arr = f.binaryValue();
        if (arr == null)
            return "BAD VALUE: " + f.toString();
        return Long.toString(toLong(arr.bytes), radix);
    }

    @Override
    public Object toObject(IndexableField f) {
        BytesRef arr = f.binaryValue();
        if (arr == null)
            return "BAD VALUE: " + f.toString();
        return toLong(arr.bytes);
    }

    @Override
    public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
        BytesRef arr = f.binaryValue();
        if (arr == null) {
            // ,
            writer.writeStr(name, "BAD VALUE: " + f.toString(), true);
            // needsEscaping);(name,);
            return;
        }
        writer.writeLong(name, toLong(arr.bytes));
    }

    static byte[] toArray(long val) {
        int off = 0;
        byte[] b = new byte[8];
        b[off + 7] = (byte) (val >>> 0);
        b[off + 6] = (byte) (val >>> 8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off + 0] = (byte) (val >>> 56);
        return b;
    }

    static long toLong(byte[] b) {
        int off = 0;
        return ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL) << 24) + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40) + ((b[off + 1] & 0xFFL) << 48) + (((long) b[off + 0]) << 56);
    }

    public static void main(String[] arg) {
        System.out.println(toLong(toArray(123456)));
        System.out.println(toLong(toArray(999999999999l)));
    }
}
