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
package com.qlangtech.tis.exec;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月15日 上午11:49:59
 */
public class ExecuteResult {

    final boolean success;

    private Object message;


    public static ExecuteResult SUCCESS = new ExecuteResult(true);

    public static ExecuteResult createFaild() {
        return new ExecuteResult(false);
    }

    public static ExecuteResult createSuccess(Object... bizResult) {
        ExecuteResult result = new ExecuteResult(true);
        if (bizResult.length > 0) {
            result.setMessage(bizResult[0]);
        }
        return result;
    }

    public ExecuteResult(boolean success) {
        super();
        this.success = success;
    }


    public Object getMessage() {
        return message;
    }

    public ExecuteResult setMessage(Object message) {
        this.message = message;
        return this;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
