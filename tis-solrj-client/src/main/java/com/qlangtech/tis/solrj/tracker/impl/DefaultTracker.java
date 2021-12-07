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
package com.qlangtech.tis.solrj.tracker.impl;

import org.apache.solr.client.solrj.SolrQuery;
import com.qlangtech.tis.solrj.tracker.ISpan;
import com.qlangtech.tis.solrj.tracker.ITracker;

/**
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
                    // if (kafkaEnabled) {
                    // tracker = new DefaultTracker();
                    // } else {
                    tracker = new MockTracker();
                // }
                }
            }
        }
        return tracker;
    }

    @Override
    public ISpan start(String collection, SolrQuery query) {
        // span.setTag(Tags.HTTP_URL.getKey(), collection + ":" + query.toString());
        return new ISpan() {

            @Override
            public void finish() {
            // span.finish();
            }

            @Override
            public void error(Throwable e) {
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
