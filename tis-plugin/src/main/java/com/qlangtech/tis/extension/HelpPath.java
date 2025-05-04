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

package com.qlangtech.tis.extension;

import com.qlangtech.tis.pubhook.common.Nullable;
import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-05-04 08:43
 **/
public class HelpPath {
    private final String path;

    public static HelpPath NULL_PATH = new NullHelpPath();

    public static HelpPath create(String path) {
        return StringUtils.isEmpty(path) ? NULL_PATH : new HelpPath(path);
    }

    private static class NullHelpPath extends HelpPath {
        public NullHelpPath() {
            super(null);
        }

        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public String getPath() {
            throw new UnsupportedOperationException();
        }
    }

    public HelpPath(String path) {
        this.path = path;
    }

    public boolean isNull() {
        return false;
    }

    public String getPath() {
        return this.path;
    }
}
