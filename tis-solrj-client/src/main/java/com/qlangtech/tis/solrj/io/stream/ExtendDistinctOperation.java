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
package com.qlangtech.tis.solrj.io.stream;

import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.ops.ReduceOperation;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import java.io.IOException;

/**
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
