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
package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class SqlStringBuilder implements Appendable {

    // private final StringBuilder content = new StringBuilder();
    private List<Object> content = Lists.newArrayList();

    public static final ThreadLocal<RewriteProcessContext> inRewriteProcess = new ThreadLocal<RewriteProcessContext>();

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        if (isInRewriteProcess()) {
            return this;
        }
        this.content.add(csq);
        return this;
    }

    public static boolean isInRewriteProcess() {
        return inRewriteProcess.get() != null;
    }

    public static RewriteProcessContext getProcessContext() {
        return inRewriteProcess.get();
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        if (isInRewriteProcess()) {
            // return this.content;
            return this;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(csq, start, end);
        this.content.add(builder);
        return this;
    // return this.content.append(csq, start, end);
    }

    public SqlStringBuilder append(Object content) {
        if (isInRewriteProcess()) {
            return this;
        }
        this.content.add(content);
        return this;
    }

    public SqlStringBuilder append(String content) {
        if (isInRewriteProcess()) {
            return this;
        }
        this.content.add(content);
        return this;
    }

    public SqlStringBuilder appendIgnoreProcess(String content) {
        this.content.add(content);
        return this;
    }

    public SqlStringBuilder append(char content) {
        if (isInRewriteProcess()) {
            return this;
        }
        this.content.add(content);
        return this;
    }

    public String getRawContent() {
        int[] index = new int[1];
        return buildContent((p) -> "callable" + index[0]++);
    }

    @Override
    public String toString() {
        return buildContent((p) -> String.valueOf(((Callable<?>) p).call()));
    }

    private String buildContent(ICallableProcess p) {
        return this.content.stream().map((r) -> {
            try {
                if (r instanceof Callable) {
                    return p.process((Callable) r);
                // return String.valueOf(((Callable<?>) r).call());
                } else {
                    return String.valueOf(r);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining());
    }

    interface ICallableProcess {

        String process(Callable callable) throws Exception;
    }

    public static class RewriteProcessContext {

        public Stack<String> tabAliasStack = new Stack<>();
    }
}
