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

/*
 *
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
