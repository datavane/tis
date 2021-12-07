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
