package com.qlangtech.tis.extension;

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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;

/**
 * 支持Descriptor 创建基于Zeppelin的notebook
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-10 15:07
 **/
public interface INotebookable {

    public enum InterpreterGroup {
        JDBC_GROUP("jdbc-tis", "org.apache.zeppelin.jdbc.TISJDBCInterpreter"),
        ELASTIC_GROUP("elasticsearch-tis", "org.apache.zeppelin.elasticsearch.TISElasticsearchInterpreter");
        private final String groupName;
        private final String itprtGroupClass;

        private InterpreterGroup(String groupName, String itprtGroupClass) {
            this.groupName = groupName;
            this.itprtGroupClass = itprtGroupClass;
        }

        public String getGroupName() {
            return this.groupName;
        }

        public String getInterpreterGroupClass() {
            return this.itprtGroupClass;
        }
    }

    /**
     * @param msgHandler
     * @param context
     * @param postFormVals
     * @return notebookId
     * @throws Exception
     */
    String createOrGetNotebook(IControlMsgHandler msgHandler
            , Context context, Descriptor.PostFormVals postFormVals) throws Exception;
}
