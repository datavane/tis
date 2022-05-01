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
package com.qlangtech.tis.lang;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * 底层运行时异常运行时可直达web，届时可添加一些格式化处理
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-23 18:56
 */
public class TisException extends RuntimeException {

    public static String getErrMsg(Throwable throwable) {
        TisException except = find(throwable);
        if (except == null) {
            return org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(except);
        } else {
            return except.getMessage();
        }
    }

    public static TisException find(Throwable throwable) {
        final Throwable[] throwables = ExceptionUtils.getThrowables(throwable);
        for (Throwable ex : throwables) {
            if (TisException.class.isAssignableFrom(ex.getClass())) {
                return (TisException) ex;
            }
        }
        return null;
    }

    public TisException(String message, Throwable cause) {
        super(message, cause);
    }

    public TisException(String message) {
        super(message);
    }
}
