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

package com.qlangtech.tis.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.qlangtech.tis.datax.job.SSERunnable;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-02-09 10:00
 **/
public class WaittingProcessCollectorAppender extends UnsynchronizedAppenderBase<ch.qos.logback.classic.spi.LoggingEvent> {
  private PatternLayout layout;

  @Override
  public void start() {
    this.layout = new PatternLayout();
    this.layout.setPattern("%msg%n");
    this.layout.setContext(this.getContext());
    super.start();
    this.layout.start();
  }

  @Override
  protected void append(ch.qos.logback.classic.spi.LoggingEvent e) {
    // System.out.println(e.getClass());

    // try (BufferedReader msgReader = new BufferedReader(new StringReader(e.getFormattedMessage()))) {

    Level level = null;
    if (SSERunnable.sseAware()) {
      SSERunnable sse = SSERunnable.getLocal();
      level = e.getLevel();
      if (level.isGreaterOrEqual(Level.ERROR)) {
        sse.error(null, e.getTimeStamp(), this.layout.doLayout(e));
        return;
      }

      if (level.isGreaterOrEqual(Level.DEBUG)) {
        sse.info(null, e.getTimeStamp(), e.getFormattedMessage());
        return;
      }


      throw new IllegalStateException("unhandler error level:" + level + " msg:" + e.getFormattedMessage());
    }
  }


}
