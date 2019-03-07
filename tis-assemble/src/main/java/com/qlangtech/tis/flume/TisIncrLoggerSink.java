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
package com.qlangtech.tis.flume;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisIncrLoggerSink extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(TisIncrLoggerSink.class);

    private static final Map<String, Logger> loggers = new HashMap<>();

    public Logger getLogger(String name) {
        Logger logger = loggers.get(name);
        if (logger == null) {
            logger = LoggerFactory.getLogger(name);
            loggers.put(name, logger);
        }
        return logger;
    }

    @Override
    public void configure(Context context) {
    // String myProp = context.getString("myProp", "defaultValue");
    // Process the myProp value (e.g. validation)
    // Store myProp for later retrieval by process() method
    // this.myProp = myProp;
    }

    @Override
    public void start() {
    // Initialize the connection to the external repository (e.g. HDFS) that
    // this Sink will forward Events to ..
    }

    @Override
    public void stop() {
    // Disconnect from the external respository and do any
    // additional cleanup (e.g. releasing resources or nulling-out
    // field values) ..
    }

    @Override
    public Status process() throws EventDeliveryException {
        // logger.info("start process");
        Status status = null;
        // Start transaction
        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        txn.begin();
        try {
            int txnEventCount = 0;
            for (txnEventCount = 0; txnEventCount < 200; txnEventCount++) {
                try {
                    Event event = ch.take();
                    if (event == null) {
                        break;
                    }
                    Map<String, String> headers = event.getHeaders();
                    String execGroup = headers.get("incr_exec_group");
                    String application = headers.get("application");
                    String host = headers.get("host");
                    MDC.put("application", application);
                    MDC.put("group", execGroup);
                    MDC.put("host", host);
                    MDC.put("app", headers.get("app"));
                    String logtype = headers.get("logtype");
                    if (StringUtils.isEmpty(logtype)) {
                        logger.info(new String(event.getBody(), "utf8"));
                    } else {
                        getLogger(logtype).info(new String(event.getBody(), "utf8"));
                    }
                } finally {
                    MDC.clear();
                }
            // logger.info("take an event {}", event);
            // System.out.println(new String(event.getBody(), "utf8"));
            // for (Map.Entry<String, String> entry : event.getHeaders()
            // .entrySet()) {
            // System.out.println(entry.getKey() + ":" + entry.getValue());
            // }
            // System.out
            // .println("==========================================");
            // Send the Event to the external repository.
            // storeSomeData(e);
            }
            txn.commit();
            if (txnEventCount < 1) {
                return Status.BACKOFF;
            } else {
                return Status.READY;
            }
        // status = Status.READY;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            txn.rollback();
            status = Status.BACKOFF;
            // re-throw all Errors
            if (t instanceof Error) {
                throw (Error) t;
            }
        } finally {
            txn.close();
            MDC.clear();
        }
        return status;
    }
}
