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

package com.qlangtech.tis.fullbuild.phasestatus;

import java.io.File;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-15 18:49
 **/
public interface IFlush2LocalFactory {

    public static Optional<IFlush2Local> createNew(ClassLoader cl, File localFile) {
        ServiceLoader<IFlush2LocalFactory> svcLoader = ServiceLoader.load(IFlush2LocalFactory.class, cl);
        for (IFlush2LocalFactory factory : svcLoader) {
            return Optional.of(factory.create(localFile));
        }
        // throw new IllegalStateException("can not find any svc loader for :" + IFlush2LocalFactory.class);
        return Optional.empty();
    }


    IFlush2Local create(File localFile);
}
