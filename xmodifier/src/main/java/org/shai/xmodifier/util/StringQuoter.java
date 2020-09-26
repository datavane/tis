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
package org.shai.xmodifier.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Shenghai on 2014/11/29.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class StringQuoter {

    private List<Cons<String, String>> quoterList = new ArrayList<Cons<String, String>>();

    private Stack<Cons<String, String>> stack = new Stack<Cons<String, String>>();

    public int check(String str) {
        if (isQuoting()) {
            Cons<String, String> peek = stack.peek();
            if (str.startsWith(peek.getRight())) {
                Cons<String, String> pop = stack.pop();
                return pop.getRight().length();
            }
        }
        if (quoterList != null) {
            for (Cons<String, String> quoter : quoterList) {
                if (str.startsWith(quoter.getLeft())) {
                    stack.push(quoter);
                    return quoter.getLeft().length();
                }
            }
        }
        return 0;
    }

    public boolean isQuoting() {
        return !stack.empty();
    }

    public void addQuoter(Cons<String, String> quoter) {
        quoterList.add(quoter);
    }

    public void addAllQuoters(List<Cons<String, String>> quoterList) {
        if (quoterList == null) {
            return;
        }
        this.quoterList.addAll(quoterList);
    }
}
