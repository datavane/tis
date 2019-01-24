/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.shai.xmodifier.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/*
 * Created by Shenghai on 2014/11/29.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
