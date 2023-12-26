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
package com.qlangtech.tis.trigger.socket;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public enum InfoType {

    INFO(1, "info"), WARN(2, "warn"), ERROR(3, "error"), FATAL(4, "fatal");

    private final int type;
    private final String token;

    private InfoType(int type, String token) {
        this.type = type;
        this.token = token;
    }

    public int getType() {
        return type;
    }

    public String getToken() {
        return this.token;
    }

    public static InfoType getType(String token) {

        for (InfoType t : InfoType.values()) {
            if (t.token.equalsIgnoreCase(token)) {
                return t;
            }
        }
        throw new IllegalStateException("token:" + token + " is illegal");
    }

    public static InfoType getType(int type) {

        for (InfoType t : InfoType.values()) {
            if (t.type == (type)) {
                return t;
            }
        }

//        if (INFO.type == type) {
//            return INFO;
//        }
//        if (WARN.type == type) {
//            return WARN;
//        }
//        if (ERROR.type == type) {
//            return ERROR;
//        }
//        if (FATAL.type == type) {
//            return FATAL;
//        }
        throw new IllegalArgumentException("type:" + type + " is invalid");
    }
}
