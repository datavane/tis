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
