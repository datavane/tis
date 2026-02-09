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

package com.qlangtech.tis.datax;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/22
 * @see DataXJobSubmit 运行时可以修改运行所依赖的参数
 */
public interface DataXJobRunEnvironmentParamsSetter {

    public static void main(String[] args) {
        System.out.println(createSysPramsSuppiler().serialize());
    }

    public static DataXJobRunEnvironmentParamsSetter.ExtraJavaSystemPramsSuppiler createSysPramsSuppiler() {
        return new ExtraJavaSystemPramsSuppiler(false) {
            @Override
            public List<String> get() {
                List<String> params = Lists.newArrayList(super.get());
                params.add("-D" + Config.KEY_JAVA_RUNTIME_PROP_ENV_PROPS + "=true");
                params.add("-D" + Config.KEY_ASSEMBLE_HOST + "=" + Config.getAssembleHost());
                params.add("-D" + Config.KEY_TIS_HOST + "=" + Config.getTisHost());
                params.add("-D" + Config.KEY_RUNTIME + "=" + RunEnvironment.getSysRuntime().getKeyName());
                //  params.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=50005");
                return params;
            }
        };
    }

    public void setClasspath(String classpath);

    public void setWorkingDirectory(File workingDirectory);

    public void setExtraJavaSystemPramsSuppiler(ExtraJavaSystemPramsSuppiler extraJavaSystemPramsSuppiler);

    public static class ExtraJavaSystemPramsSuppiler implements Supplier<List<String>> {

        private final boolean notFetchFromCenterRepository;

        public ExtraJavaSystemPramsSuppiler(boolean notFetchFromCenterRepository) {
            this.notFetchFromCenterRepository = notFetchFromCenterRepository;
        }

        public String serialize() {
            return String.join(" ", this.get());
        }

        @Override
        public List<String> get() {
            return Lists.newArrayList("-D" + CenterResource.KEY_notFetchFromCenterRepository + "=" + notFetchFromCenterRepository);
        }
    }
}
