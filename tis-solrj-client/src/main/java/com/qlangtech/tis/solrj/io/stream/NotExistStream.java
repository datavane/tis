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
package com.qlangtech.tis.solrj.io.stream;

import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.comp.FieldComparator;
import org.apache.solr.client.solrj.io.comp.StreamComparator;
import org.apache.solr.client.solrj.io.eq.FieldEqualitor;
import org.apache.solr.client.solrj.io.eq.StreamEqualitor;
import org.apache.solr.client.solrj.io.stream.PushBackStream;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.UniqueStream;
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

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2/28/2017.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NotExistStream extends TupleStream implements Expressible {

	private static final long serialVersionUID = 1;

	private PushBackStream streamA;

	private PushBackStream streamB;

	private TupleStream originalStreamB;

	private StreamEqualitor eq;

	private FieldComparator comparator;

	public NotExistStream(TupleStream streamA, TupleStream streamB, StreamEqualitor eq) throws IOException {
		init(streamA, streamB, eq);
	}

	public NotExistStream(StreamExpression expression, StreamFactory factory) throws IOException {
		// grab all parameters out
		List<StreamExpression> streamExpressions = factory.getExpressionOperandsRepresentingTypes(expression,
				Expressible.class, TupleStream.class);
		StreamExpressionNamedParameter onExpression = factory.getNamedOperand(expression, "on");
		// validate expression contains only what we want.
		if (expression.getParameters().size() != streamExpressions.size() + 1) {
			throw new IOException(
					String.format(Locale.ROOT, "Invalid expression %s - unknown operands found", expression));
		}
		if (2 != streamExpressions.size()) {
			throw new IOException(String.format(Locale.ROOT,
					"Invalid expression %s - expecting two streams but found %d (must be TupleStream types)",
					expression, streamExpressions.size()));
		}
		if (null == onExpression || !(onExpression.getParameter() instanceof StreamExpressionValue)) {
			throw new IOException(String.format(Locale.ROOT,
					"Invalid expression %s - expecting single 'on' parameter listing fields to merge on but didn't find one",
					expression));
		}
		init(factory.constructStream(streamExpressions.get(0)), factory.constructStream(streamExpressions.get(1)),
				factory.constructEqualitor(((StreamExpressionValue) onExpression.getParameter()).getValue(),
						FieldEqualitor.class));
	}

	private void init(TupleStream streamA, TupleStream streamB, StreamEqualitor eq) throws IOException {
		this.streamA = new PushBackStream(streamA);
		this.streamB = new PushBackStream(new UniqueStream(streamB, eq));
		// hold onto this for toExpression
		this.originalStreamB = streamB;
		this.eq = eq;
		FieldEqualitor fieldEqualitor = (FieldEqualitor) eq;
		FieldComparator fieldComparator = (FieldComparator) streamA.getStreamSort();
		this.comparator = new FieldComparator(fieldEqualitor.getLeftFieldName(), fieldEqualitor.getRightFieldName(),
				fieldComparator.getOrder());
		// streamA and streamB must both be sorted so that comp can be derived
		// from
		if (!eq.isDerivedFrom(streamA.getStreamSort()) || !eq.isDerivedFrom(streamB.getStreamSort())) {
			throw new IOException(
					"Invalid ComplementStream - both substream comparators (sort) must be a superset of this stream's equalitor.");
		}
	}

	@Override
	public StreamExpression toExpression(StreamFactory factory) throws IOException {
		// function name
		StreamExpression expression = new StreamExpression(factory.getFunctionName(this.getClass()));
		// streams
		if (streamA instanceof Expressible) {
			expression.addParameter(((Expressible) streamA).toExpression(factory));
		} else {
			throw new IOException(
					"This IntersectionStream contains a non-expressible TupleStream - it cannot be converted to an expression");
		}
		if (originalStreamB instanceof Expressible) {
			expression.addParameter(((Expressible) originalStreamB).toExpression(factory));
		} else {
			throw new IOException(
					"This IntersectStream contains a non-expressible TupleStream - it cannot be converted to an expression");
		}
		// on
		expression.addParameter(new StreamExpressionNamedParameter("on", eq.toExpression(factory)));
		return expression;
	}

	public void setStreamContext(StreamContext context) {
		this.streamA.setStreamContext(context);
		this.streamB.setStreamContext(context);
	}

	public List<TupleStream> children() {
		List<TupleStream> l = new ArrayList();
		l.add(streamA);
		l.add(streamB);
		return l;
	}

	public void open() throws IOException {
		streamA.open();
		streamB.open();
	}

	public void close() throws IOException {
		streamA.close();
		streamB.close();
	}

	public Tuple read() throws IOException {
		while (true) {
			Tuple a = streamA.read();
			Tuple b = streamB.read();
			// if a is EOF then we're done
			if (a.EOF) {
				return a;
			}
			// if b is EOF then return a
			if (b.EOF) {
				streamB.pushBack(b);
				return a;
			}
			// if a != b && a < b then we know there is no b which a might equal
			// so return a
			if (!eq.test(a, b) && this.comparator.compare(a, b) < 0) {
				streamB.pushBack(b);
				return a;
			}
			// else we know that b < a so we can ignore b
			if (eq.test(a, b)) {
				streamB.pushBack(b);
			} else {
				streamA.pushBack(a);
			}
		}
	}

	/**
	 * Return the stream sort - ie, the order in which records are returned
	 */
	public StreamComparator getStreamSort() {
		return streamA.getStreamSort();
	}

	public int getCost() {
		return 0;
	}

	@Override
	public Explanation toExplanation(StreamFactory factory) throws IOException {

		return null;
	}
}
