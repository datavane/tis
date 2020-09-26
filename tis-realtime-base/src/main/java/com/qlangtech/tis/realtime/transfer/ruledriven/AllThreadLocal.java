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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import com.qlangtech.tis.realtime.transfer.IPk;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年6月25日
 */
public class AllThreadLocal {

    private static final Set<ThreadLocal<?>> allThreadLocal = Sets.newHashSet();

    public static DefaultThreadLocal<IPk> pkThreadLocal = addThreadLocalVal(true, () -> null);

    // /**
    // * consume 中在
    // *
    // * @param call
    // * @return
    // */
    public static IPk getPkThreadLocal() {
        IPk pk = pkThreadLocal.get();
        if (pk == null) {
            throw new IllegalStateException("pk can not be null");
        }
        return pk;
    }

    /**
     * 创建一个新的线程绑定緩存，銷毀會統一在線程執行結束之後
     *
     * @return
     */
    public static <T> DefaultThreadLocal<T> addThreadLocalVal() {
        return addThreadLocalVal(true, /* clearable */
        () -> null);
    }

    public static <T> DefaultThreadLocal<T> addThreadLocalVal(boolean clearable, Callable<T> call) {
        DefaultThreadLocal<T> tl = new DefaultThreadLocal<T>(call, clearable);
        allThreadLocal.add(tl);
        return tl;
    }

    public static class DefaultThreadLocal<T> extends ThreadLocal<T> {

        private Callable<T> call;

        private final boolean clearable;

        public DefaultThreadLocal(Callable<T> call, boolean clearable) {
            this.call = call;
            this.clearable = clearable;
        }

        public void setCall(Callable<T> call) {
            this.call = call;
        }

        @Override
        protected T initialValue() {
            try {
                return call.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            if (!clearable) {
                return;
            }
            super.remove();
        }
    }

    /**
     * 清除所有线程缓存数据
     */
    public static void cleanAllThreadLocalVal() {
        allThreadLocal.forEach((e) -> {
            e.remove();
        });
    }
}
