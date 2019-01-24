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

import java.util.HashMap;
import java.util.Map;

/*
 * User: Shenghai.Geng
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class XPathPatternQuoter {

    private Map<Character, Character> quoterMap = new HashMap<Character, Character>();

    private Map<Character, Boolean> statusMap = new HashMap<Character, Boolean>();

    /**
     * Check specified character is in quotes collection
     *
     * @param c Character use to check
     */
    public void check(char c) {
        for (Map.Entry<Character, Character> entry : quoterMap.entrySet()) {
            Character start = entry.getKey();
            Character end = entry.getValue();
            if (c == start) {
                Boolean status = statusMap.get(start);
                if (status == null) {
                    status = false;
                }
                statusMap.put(start, !status);
                break;
            }
            if (c == end) {
                Boolean status = statusMap.get(start);
                if (status == null) {
                    status = false;
                }
                statusMap.put(start, !status);
                break;
            }
        }
    }

    /**
     * Check quotes status.
     *
     * @return true if the character is used.
     */
    public boolean isQuoting() {
        for (Map.Entry<Character, Boolean> entry : statusMap.entrySet()) {
            if (entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add quotes
     *
     * @param start
     * @param end
     */
    public void addQuoter(char start, char end) {
        quoterMap.put(start, end);
    }
}
