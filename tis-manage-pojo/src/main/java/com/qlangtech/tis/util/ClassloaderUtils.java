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

package com.qlangtech.tis.util;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-01-09 13:16
 **/
public class ClassloaderUtils {

    public static <T> T processByResetThreadClassloader(Class clazz, SupplierThrowable<T> resultSupplier) throws Exception {
        final ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
            return resultSupplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }


    @FunctionalInterface
    public interface SupplierThrowable<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Exception;
    }
}
