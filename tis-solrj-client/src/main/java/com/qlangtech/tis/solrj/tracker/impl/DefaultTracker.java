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
package com.qlangtech.tis.solrj.tracker.impl;

import org.apache.solr.client.solrj.SolrQuery;
//import com.dfire.magiceye.Tracing;
//import com.dfire.magiceye.exception.ThrowableHandler;
//import com.dfire.magiceye.util.TraceUtils;
import com.qlangtech.tis.solrj.tracker.ISpan;
import com.qlangtech.tis.solrj.tracker.ITracker;
//import io.opentracing.Span;
//import io.opentracing.Tracer;
//import io.opentracing.tag.Tags;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultTracker implements ITracker {

    // private static final String TRACER_SPAN = "trace_span";
    private static final String TIS_COMPONENT = "TIS";

    private static boolean kafkaEnabled = false;

    static {
        try {
            Class.forName("org.apache.kafka.clients.consumer.KafkaConsumer");
            kafkaEnabled = true;
        } catch (Throwable e) {
        }
        System.out.println("kafkaEnabled:" + kafkaEnabled);
    }

    private static ITracker tracker;

    private DefaultTracker() {
    }

    public static ITracker create() {
        if (tracker == null) {
            synchronized (DefaultTracker.class) {
                if (tracker == null) {
//                    if (kafkaEnabled) {
//                        tracker = new DefaultTracker();
//                    } else {
                        tracker = new MockTracker();
                    //}
                }
            }
        }
        return tracker;
    }

    @Override
    public ISpan start(String collection, SolrQuery query) {
//        query.set("traceid", TraceUtils.traceId());
//        Tracer tracer = Tracing.current().tracer();
//        final Span span = tracer.buildSpan("tisquery").startManual();
//        Tags.COMPONENT.set(span, TIS_COMPONENT);
//        span.setTag(Tags.HTTP_URL.getKey(), collection + ":" + query.toString());
        return new ISpan() {

            @Override
            public void finish() {
                //span.finish();
            }

            @Override
            public void error(Throwable e) {
//                if (e != null) {
//                    // span.setTag(com.dfire.magiceye.Constants.RESPONSE_STATUS,
//                    // Config.STATUS.ERR.getType());
//                    ThrowableHandler.handle(span, e);
//                }
            // this.finish();
            }
        };
    }

    private static class MockTracker implements ITracker {

        private final ISpan span;

        public MockTracker() {
            super();
            this.span = new MockSpan();
        }

        @Override
        public ISpan start(String collection, SolrQuery query) {
            return this.span;
        }
    }

    private static class MockSpan implements ISpan {

        @Override
        public void finish() {
        }

        @Override
        public void error(Throwable e) {
        }
    }
}
