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
import org.apache.solr.client.solrj.io.ops.ReduceOperation;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import java.io.IOException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExtendDistinctOperation implements ReduceOperation {

	private static final long serialVersionUID = 1L;

	private Tuple current;

	public ExtendDistinctOperation(StreamExpression expression, StreamFactory factory) throws IOException {
		init();
	}

	public ExtendDistinctOperation() {
		init();
	}

	private void init() {
	}

	public StreamExpressionParameter toExpression(StreamFactory factory) throws IOException {
		StreamExpression expression = new StreamExpression(factory.getFunctionName(this.getClass()));
		return expression;
	}

	public Tuple reduce() {
		// Return the tuple after setting current to null. This will ensure the
		// next call to
		// operate stores that tuple
		Tuple toReturn = current;
		current = null;
		return toReturn;
	}

	public void operate(Tuple tuple) {
		// we only care about the first one seen. Drop all but the first
		current = tuple;
	}

	@Override
	public Explanation toExplanation(StreamFactory factory) throws IOException {

		return null;
	}
}
