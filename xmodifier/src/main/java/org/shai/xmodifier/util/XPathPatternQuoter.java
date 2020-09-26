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

import java.util.HashMap;
import java.util.Map;

/**
 * User: Shenghai.Geng
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
