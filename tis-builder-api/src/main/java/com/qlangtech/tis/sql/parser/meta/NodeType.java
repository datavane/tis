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
package com.qlangtech.tis.sql.parser.meta;

/**
 * 节点类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年4月30日
 */
public enum NodeType {

    JOINER_SQL("join"), DUMP("table"), UNION_SQL("union");

    private final String type;

    private NodeType(String type) {
        this.type = type;
    }

    public static NodeType parse(String type) throws NodeTypeParseException {


        for (NodeType t : NodeType.values()) {
            if (t.type.equals(type)) {
                return t;
            }
        }

//        if (JOINER_SQL.type.equals(type)) {
//            return NodeType.JOINER_SQL;
//        }
//        if (UNION_SQL.type.equals(type)) {
//            return UNION_SQL;
//        }
//        if (DUMP.type.equals(type)) {
//            return DUMP;
//        }
        throw new NodeTypeParseException("illegal type:'" + type + "'");
    }

    public String getType() {
        return type;
    }


    public static class NodeTypeParseException extends Exception {
        public NodeTypeParseException(String message) {
            super(message);
        }
    }
}
