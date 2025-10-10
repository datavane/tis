/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.datax.job;

import com.alibaba.fastjson.JSONObject;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/7
 */
public class SSEEventWriter extends FilterWriter {
    // private final PrintWriter writer;
    private final BiConsumer<SSERunnable.SSEEventType, JSONObject> sseEventConsumer;

    public SSEEventWriter(Writer writer) {
        this(writer, (event, data) -> {
        });
    }

    public SSEEventWriter(Writer writer, BiConsumer<SSERunnable.SSEEventType, JSONObject> sseEventConsumer) {
        super(writer);
        this.sseEventConsumer = Objects.requireNonNull(sseEventConsumer, "sseEventConsumer can not be null");
    }

    public final void writeSSEEvent(SSERunnable.SSEEventType event, JSONObject data) {
        this.writeSSEEvent(event, data.toJSONString());
        sseEventConsumer.accept(event, data);
    }

    public final void writeSSEEvent(SSERunnable.SSEEventType event, String data) {
        synchronized (lock) {
            try {
                this.out.write("event: " + event.getEventType() + "\n");
                this.out.write("data: " + data + "\n\n");
                this.out.flush();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
