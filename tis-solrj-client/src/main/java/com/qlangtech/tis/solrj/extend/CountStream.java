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
package com.qlangtech.tis.solrj.extend;

import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.comp.StreamComparator;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.Expressible;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CountStream extends TupleStream implements Expressible {

    private TupleStream stream;

    private long count;

    private transient boolean finished = false;

    private transient LinkedList<Tuple> tupleList;

    public CountStream(TupleStream tupleStream) throws IOException {
        init(tupleStream);
    }

    public CountStream(StreamExpression expression, StreamFactory factory) throws IOException {
        List<StreamExpression> streamExpressions = factory.getExpressionOperandsRepresentingTypes(expression, Expressible.class, TupleStream.class);
        if (1 != expression.getParameters().size()) {
            throw new IOException(String.format("Invalid expression %s - expecting a single stream and no params", expression));
        }
        if (1 != streamExpressions.size()) {
            throw new IOException(String.format(Locale.ROOT, "Invalid expression %s - expecting a single stream but found %d", expression, streamExpressions.size()));
        }
        TupleStream stream = factory.constructStream(streamExpressions.get(0));
        init(stream);
    }

    private void init(TupleStream tupleStream) {
        this.stream = tupleStream;
        this.count = 0L;
        this.tupleList = new LinkedList<>();
    }

    @Override
    public void setStreamContext(StreamContext context) {
        this.stream.setStreamContext(context);
    }

    @Override
    public List<TupleStream> children() {
        List<TupleStream> l = new ArrayList();
        l.add(stream);
        return l;
    }

    @Override
    public void open() throws IOException {
        stream.open();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public Tuple read() throws IOException {
        if (!finished) {
            while (true) {
                Tuple tuple = stream.read();
                if (tuple.EOF) {
                    finished = true;
                    Map<String, Long> map = new HashMap<>();
                    map.put("count", count);
                    Tuple countTuple = new Tuple(map);
                    tupleList.add(countTuple);
                    tupleList.add(tuple);
                    break;
                } else {
                    count++;
                }
            }
        }
        return tupleList.pollFirst();
    }

    @Override
    public StreamComparator getStreamSort() {
        return null;
    }

    @Override
    public StreamExpressionParameter toExpression(StreamFactory factory) throws IOException {
        // function name
        StreamExpression expression = new StreamExpression(factory.getFunctionName(this.getClass()));
        // stream
        if (stream instanceof Expressible) {
            expression.addParameter(((Expressible) stream).toExpression(factory));
        } else {
            throw new IOException("This CountStream contains a non-expressible TupleStream - it cannot be converted to an expression");
        }
        return expression;
    }

    @Override
    public Explanation toExplanation(StreamFactory factory) throws IOException {
        return null;
    }
}
