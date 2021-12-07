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
package com.qlangtech.tis.solrj.io.stream;

import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.comp.StreamComparator;
import org.apache.solr.client.solrj.io.eq.FieldEqualitor;
import org.apache.solr.client.solrj.io.eq.StreamEqualitor;
import org.apache.solr.client.solrj.io.stream.ReducerStream;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.Expressible;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionNamedParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionValue;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 这个unique流，会在unique的时候，使用最新的一条记录
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExtendUniqueStream extends TupleStream implements Expressible {

    private static final long serialVersionUID = 1;

    private TupleStream originalStream;

    private StreamEqualitor originalEqualitor;

    private ReducerStream reducerStream;

    public ExtendUniqueStream(TupleStream stream, StreamEqualitor eq) throws IOException {
        init(stream, eq);
    }

    public ExtendUniqueStream(StreamExpression expression, StreamFactory factory) throws IOException {
        // grab all parameters out
        List<StreamExpression> streamExpressions = factory.getExpressionOperandsRepresentingTypes(expression, Expressible.class, TupleStream.class);
        StreamExpressionNamedParameter overExpression = factory.getNamedOperand(expression, "over");
        // validate expression contains only what we want.
        if (expression.getParameters().size() != streamExpressions.size() + 1) {
            throw new IOException(String.format(Locale.ROOT, "Invalid expression %s - unknown operands found", expression));
        }
        if (1 != streamExpressions.size()) {
            throw new IOException(String.format(Locale.ROOT, "Invalid expression %s - expecting a single stream but " + "found %d", expression, streamExpressions.size()));
        }
        if (null == overExpression || !(overExpression.getParameter() instanceof StreamExpressionValue)) {
            throw new IOException(String.format(Locale.ROOT, "Invalid expression %s - expecting single 'over' " + "parameter listing fields to unique over but didn't find one", expression));
        }
        init(factory.constructStream(streamExpressions.get(0)), factory.constructEqualitor(((StreamExpressionValue) overExpression.getParameter()).getValue(), FieldEqualitor.class));
    }

    private void init(TupleStream stream, StreamEqualitor eq) throws IOException {
        this.originalStream = stream;
        this.originalEqualitor = eq;
        this.reducerStream = new ReducerStream(stream, eq, new ExtendDistinctOperation());
        if (!eq.isDerivedFrom(stream.getStreamSort())) {
            throw new IOException("Invalid UniqueStream - substream comparator (sort) must be a superset of this " + "stream's equalitor.");
        }
    }

    @Override
    public StreamExpression toExpression(StreamFactory factory) throws IOException {
        // function name
        StreamExpression expression = new StreamExpression(factory.getFunctionName(this.getClass()));
        // streams
        if (originalStream instanceof Expressible) {
            expression.addParameter(((Expressible) originalStream).toExpression(factory));
        } else {
            throw new IOException("This UniqueStream contains a non-expressible TupleStream - it cannot be converted " + "to an expression");
        }
        // over
        if (originalEqualitor instanceof Expressible) {
            expression.addParameter(new StreamExpressionNamedParameter("over", ((Expressible) originalEqualitor).toExpression(factory)));
        } else {
            throw new IOException("This UniqueStream contains a non-expressible equalitor - it cannot be converted to" + " an expression");
        }
        return expression;
    }

    public void setStreamContext(StreamContext context) {
        this.originalStream.setStreamContext(context);
        this.reducerStream.setStreamContext(context);
    }

    public List<TupleStream> children() {
        List<TupleStream> l = new ArrayList<TupleStream>();
        l.add(originalStream);
        return l;
    }

    public void open() throws IOException {
        reducerStream.open();
    // opens originalStream as well
    }

    public void close() throws IOException {
        reducerStream.close();
    // closes originalStream as well
    }

    public Tuple read() throws IOException {
        return reducerStream.read();
    }

    /**
     * Return the stream sort - ie, the order in which records are returned
     */
    public StreamComparator getStreamSort() {
        return reducerStream.getStreamSort();
    }

    @Override
    public Explanation toExplanation(StreamFactory factory) throws IOException {
        return null;
    }
}
