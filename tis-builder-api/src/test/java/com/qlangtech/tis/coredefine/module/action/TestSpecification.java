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

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-13 12:56
 **/
public class TestSpecification extends TestCase {

    public void testNormalizeMemory() {
        Specification mem = Specification.parse("1.5G");
        assertEquals("G", mem.getUnit());
        assertEquals(1536, mem.normalizeMemory());

        assertEquals(691, mem.normalizeMemory(Optional.of(45)));

        mem = Specification.parse("1G");
        assertEquals("G", mem.getUnit());
        assertEquals(1024, mem.normalizeMemory());
        assertEquals(512, mem.normalizeMemory(Optional.of(50)));

        mem = Specification.parse("1500Mi");
        assertEquals("Mi", mem.getUnit());
        assertEquals(1500, mem.normalizeMemory());
        assertEquals(750, mem.normalizeMemory(Optional.of(50)));

        Specification  cpu = Specification.parse("1500");

        assertTrue(StringUtils.isEmpty( cpu.getUnit() ));
        assertEquals(1500*1024, cpu.normalizeCPU());

    }
}
