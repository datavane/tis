/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/* * Copyright 2020 QingLang, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.lang.reflect.Field;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestModilyFinalField {

    private final String testFiled = "2";

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        TestModilyFinalField finalField = new TestModilyFinalField();
        Field field = finalField.getClass().getDeclaredField("testFiled");
        field.setAccessible(true);
        field.set(finalField, "3");
        System.out.println(finalField.testFiled);
    // String a = "abc";
    // Field f = a.getClass().getDeclaredField("value");
    // f.setAccessible(true);
    // char[] ch = new char[3];
    // ch[0] = 'b';
    // ch[1] = 'c';
    // ch[2] = 'd';
    // f.set(a, ch);
    // System.out.println(a);
    }
}
