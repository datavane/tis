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
package com.qlangtech.tis.sql.parser.tuple.creator.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import com.qlangtech.tis.sql.parser.tuple.creator.IValChain;
import org.apache.commons.lang.StringUtils;
import com.google.common.collect.Lists;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ValChain implements IValChain {

    private List<PropGetter> chain = Lists.newArrayList();

    private PropGetter last = null;

    private PropGetter first = null;

    private final AtomicBoolean everAdd = new AtomicBoolean(false);

    private final AtomicBoolean everReverse = new AtomicBoolean(false);

    @Override
    public boolean useAliasOutputName() {
        return !StringUtils.equals(last.getOutputColName().getName(), first.getOutputColName().getAliasName());
    }

    public void add(PropGetter p) {
        if (everAdd.compareAndSet(false, true)) {
            this.first = p;
        }
        if (this.last != null) {
            // 将他们前后指针串起来
            this.last.setNext(p);
            p.setPrev(this.last);
        }
        this.chain.add(p);
        this.last = p;
    }

    @Override
    public <R> Stream<R> mapChainValve(Function<PropGetter, ? extends R> mapper) {
        return chain.stream().map(mapper);
    }

    @Override
    public Stream<PropGetter> chainStream() {
        if (everReverse.compareAndSet(false, true)) {
            // 将原有的列表反向排列
            Collections.reverse(this.chain);
            AtomicReference<PropGetter> lastFunc = new AtomicReference<>();
            this.chain.stream().filter((r) -> {
                return r.getTupleCreator() != null && (r.getTupleCreator() instanceof FunctionDataTupleCreator);
            }).forEach((r) -> {
                r.setLastFunctInChain(true);
                PropGetter prev = lastFunc.getAndSet(r);
                if (prev != null) {
                    prev.setLastFunctInChain(false);
                }
            });
            this.chain = Collections.unmodifiableList(this.chain);
        }
        return this.chain.stream();
    }

    @Override
    public boolean hasFuncTuple() {
        for (PropGetter p : chain) {
            if (p.getTupleCreator() != null && p.getTupleCreator() instanceof FunctionDataTupleCreator) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PropGetter first() {
        return this.first;
    }

    @Override
    public PropGetter last() {
        return this.last;
    }
}
