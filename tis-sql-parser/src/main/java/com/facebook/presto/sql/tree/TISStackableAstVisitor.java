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
package com.facebook.presto.sql.tree;

import java.util.LinkedList;
import java.util.Optional;
import com.facebook.presto.sql.tree.AstVisitor;
import com.facebook.presto.sql.tree.Node;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TISStackableAstVisitor<R, C> extends AstVisitor<R, TISStackableAstVisitor.StackableAstVisitorContext<C>> {

    public R process(Node node, StackableAstVisitorContext<C> context) {
        context.push(node);
        try {
            // context.getStackDeepth());
            return node.accept(this, context);
        } finally {
            context.pop();
        }
    }

    public static class StackableAstVisitorContext<C> {

        private final LinkedList<Node> stack = new LinkedList<>();

        private final C context;

        private int stackDeepth;

        public boolean processSelect = false;

        public StackableAstVisitorContext(C context) {
            this.context = context;
        }

        public C getContext() {
            return context;
        }

        private void pop() {
            stackDeepth--;
            stack.pop();
        }

        public int getStackDeepth() {
            return stackDeepth;
        }

        void push(Node node) {
            stackDeepth++;
            stack.push(node);
        }

        public Optional<Node> getPreviousNode() {
            if (stack.size() > 1) {
                return Optional.of(stack.get(1));
            }
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        LinkedList<Integer> stack = new LinkedList<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for (int i = 0; i < 3; i++) {
            System.out.println(stack.get(i));
        }
        System.out.println(stack.poll());
        System.out.println(stack.poll());
        System.out.println(stack.poll());
    }
}
