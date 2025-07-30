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
package com.qlangtech.tis.realtime.transfer;

import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO.RateControllerType;

import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class ListenerStatusKeeper {

    private int bufferQueueRemainingCapacity;

    private int bufferQueueUsedSize;

    private int consumeErrorCount;

    private int ignoreRowsCount;

    private String uuid;

    // 增量任务是否暂停
    private boolean incrProcessPaused;

    private long tis30sAvgRT;

    public boolean isIncrProcessPaused() {
        return incrProcessPaused;
    }

    public void setIncrProcessPaused(boolean incrProcessPaused) {
        this.incrProcessPaused = incrProcessPaused;
    }

    public int getBufferQueueRemainingCapacity() {
        return bufferQueueRemainingCapacity;
    }

    public void setBufferQueueRemainingCapacity(int bufferQueueRemainingCapacity) {
        this.bufferQueueRemainingCapacity = bufferQueueRemainingCapacity;
    }

    public int getBufferQueueUsedSize() {
        return bufferQueueUsedSize;
    }

    public void setBufferQueueUsedSize(int bufferQueueUsedSize) {
        this.bufferQueueUsedSize = bufferQueueUsedSize;
    }

    public int getConsumeErrorCount() {
        return consumeErrorCount;
    }

    public void setConsumeErrorCount(int consumeErrorCount) {
        this.consumeErrorCount = consumeErrorCount;
    }

    public int getIgnoreRowsCount() {
        return ignoreRowsCount;
    }

    public void setIgnoreRowsCount(int ignoreRowsCount) {
        this.ignoreRowsCount = ignoreRowsCount;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    private long getTis30sAvgRT() {
        return tis30sAvgRT;
    }

    private void setTis30sAvgRT(long tis30sAvgRT) {
        this.tis30sAvgRT = tis30sAvgRT;
    }

    public void setIncrRateLimitConfig(LimitRateTypeAndRatePerSecNums config) {
        this.setTis30sAvgRT(config.serialize());
    }

    public LimitRateTypeAndRatePerSecNums getIncrRateLimitConfig() {
        return LimitRateTypeAndRatePerSecNums.create(this.getTis30sAvgRT());
    }

    /**
     * java 中 一个long 类型如何，保存两个 不同的类型变量，一个变量类型为short，另外一个变量类型为integer
     * ，基本思路是 long的 前16位保存short类型变量，16位后的32位保存int类型变量。请帮我实现该long类型变量的序列化和反序列化代码 <br/>
     * <p>
     * 在Java中，可以通过位操作实现使用long类型保存short和int变量。具体思路是将short（16位）存储在long的高16位，将int（32位）存储在中间的32位，剩余低16位未使用。以下是完整代码实现：
     *
     * @author: 百岁（baisui@qlangtech.com）
     * @create: 2025-07-18 22:32
     **/
    public static class LimitRateTypeAndRatePerSecNums {

        // 序列化：将short和int合并为long
        public static long serialize(short s, int i) {
            // 将short放入高16位（48-63位）
            long shortPart = ((long) s & 0xFFFFL) << 48;
            // 将int放入中间的32位（16-47位）
            long intPart = ((long) i & 0xFFFFFFFFL) << 16;
            // 合并两部分
            return shortPart | intPart;
        }

        // 反序列化：从long中提取short（高16位）
        public static short getShort(long composite) {
            // 无符号右移48位，保留低16位并转为short
            return (short) (composite >>> 48);
        }

        // 反序列化：从long中提取int（中间32位）
        public static int getInt(long composite) {
            // 右移16位后，用掩码保留低32位并转为int
            return (int) ((composite >>> 16) & 0xFFFFFFFFL);
        }

        private final int perSecRateNums;
        private final Optional<RateControllerType> controllerType;

        public LimitRateTypeAndRatePerSecNums(RateControllerType controllerType, int perSecRateNums) {
            this.perSecRateNums = perSecRateNums;
            this.controllerType = Optional.ofNullable(controllerType);
        }

        public static LimitRateTypeAndRatePerSecNums create(long gcCounter) {
            int perSecRateNums = getInt(gcCounter);
            return new LimitRateTypeAndRatePerSecNums(RateControllerType.parse(getShort(gcCounter)), perSecRateNums);
        }

        public int getPerSecRateNums() {
            return perSecRateNums;
        }

        public Optional<RateControllerType> getControllerType() {
            return controllerType;
        }

        public long serialize() {
            return LimitRateTypeAndRatePerSecNums.serialize(controllerType.map((c) -> c.getTypeToken())
                            .orElse(RateControllerType.NoLimitParam.getTypeToken())
                    , this.perSecRateNums);
        }

        // 测试
        public static void main(String[] args) {
            short originalShort = -12345;
            int originalInt = 0xABCD1234;

            // 序列化
            long composite = serialize(originalShort, originalInt);
            System.out.println("Composite long: 0x" + Long.toHexString(composite));

            // 反序列化
            short extractedShort = getShort(composite);
            int extractedInt = getInt(composite);

            // 验证结果
            System.out.println("Original short: " + originalShort + ", Extracted: " + extractedShort);
            System.out.println("Original int: 0x" + Integer.toHexString(originalInt) +
                    ", Extracted: 0x" + Integer.toHexString(extractedInt));

            // 断言校验
            assert originalShort == extractedShort;
            assert originalInt == extractedInt;
        }
    }
}
