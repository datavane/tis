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

package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.realtime.transfer.UnderlineUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-15 15:59
 **/
public class TargetResName extends ResName {


    public static final TargetResName K8S_DATAX_INSTANCE_NAME = new TargetResName("datax-worker");

    public static TargetResName createTargetName(DataXName dataXName) {
      dataXName.assetCheckDataAppType();
      return new TargetResName(dataXName.getPipelineName());
    }

    public String getStreamSourceHandlerClass() {

        return "com.qlangtech.tis.realtime.transfer." + this.getName() + "." + UnderlineUtils.getJavaName(this.getName()) + "SourceHandle";
    }

    public TargetResName(String name) {


        super(name);
    }


    public boolean equalWithName(String name) {
        return this.getName().equals(name);
    }


    public String getK8SResName() {
        return StringUtils.replace(this.getName(), "_", "-");
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + getName() + '\'' +
                '}';
    }
}
