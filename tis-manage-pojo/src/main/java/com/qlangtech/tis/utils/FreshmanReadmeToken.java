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

package com.qlangtech.tis.utils;

import com.google.common.util.concurrent.RateLimiter;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-06-20 11:52
 **/
public class FreshmanReadmeToken {

    //private static final AtomicLong lastReadFreshManReadmeCheckTimestamp = new AtomicLong();
    public static final RateLimiter rateLimiter = RateLimiter.create(0.5);
    private static FreshmanReadmeToken freshmanReadmeToken = null;
    private final File readFreshManTokenFile;
    private final long lastAccessEpochMilli;

    private boolean neverOpenAgain = false;

    public FreshmanReadmeToken(File readFreshManTokenFile) {
        this.readFreshManTokenFile = readFreshManTokenFile;
        try {
            if (!readFreshManTokenFile.exists()) {
                this.lastAccessEpochMilli = 0;
            } else {
                String tokenContent = FileUtils.readFileToString(readFreshManTokenFile, TisUTF8.get());
                this.lastAccessEpochMilli = Long.parseLong(tokenContent);
                if (this.lastAccessEpochMilli == Long.MAX_VALUE) {
                    this.neverOpenAgain = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置新人文档已经被阅读了
     *
     * @return
     */
    public static void setFreshManReadmeHasRead(boolean remindMeLater) {
        FreshmanReadmeToken readFreshManToken = getReadFreshManTokenFile();
        readFreshManToken.setHasRead(remindMeLater);
        FreshmanReadmeToken.freshmanReadmeToken = null;
    }

    public static FreshmanReadmeToken getReadFreshManTokenFile() {
        if (freshmanReadmeToken == null) {
            synchronized (FreshmanReadmeToken.class) {
                if (freshmanReadmeToken == null) {
                    freshmanReadmeToken
                            = new FreshmanReadmeToken(new File(Config.getDataDir(), "freshman_readme_token"));
                }
            }
        }
        return freshmanReadmeToken;
    }

    /**
     * 是否已经阅读TIS新人指南，有的新人刚下载了TIS还没有阅读过TIS的文档，当TIS系统一打开先跳出一个新人指南对话框
     *
     * @return
     */
    public static boolean hasReadFreshManReadme() {
        // 2秒钟之内只允许一个ajax 请求校验失败，防止前端页面中重复打开新人欢迎页面
        if (!rateLimiter.tryAcquire()) {
            // 两秒内有其他访问请求都要放行，防止前端页面中重复打开新人欢迎页面
            return true;
        }
        FreshmanReadmeToken readFreshManToken = getReadFreshManTokenFile();
        return readFreshManToken.isValid();
    }

    /**
     * 设置已经阅读过了
     */
    public void setHasRead(boolean remindMeLater) {
        try {
            FileUtils.write(readFreshManTokenFile
                    , remindMeLater ? String.valueOf(System.currentTimeMillis() + 86400000 * 7) : String.valueOf(Long.MAX_VALUE)
                    , TisUTF8.get(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValid() {
        if (this.neverOpenAgain) {
            return true;
        }
        return System.currentTimeMillis() < lastAccessEpochMilli;
    }
}
