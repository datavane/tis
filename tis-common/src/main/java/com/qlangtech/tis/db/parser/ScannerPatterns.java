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
package com.qlangtech.tis.db.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ScannerPatterns {

    public enum TokenTypes {

        // 
        TT_HOST_DESC("^host", true),
        // 
        TT_PASSWORD("^password", true),
        // 
        TT_USERNAME("^username", true),
        // 
        TT_PORT("^port", true),
        // 
        TT_MYSQL("^mysql", true),
        // 
        TT_LEFT("^\\{", true),
        // 
        TT_RIGHT("^\\}", true),
        // 
        TT_DBDESC_SPLIT("^,", true),
        // 
        TT_RANGE_LEFT("^\\[", true),
        // 
        TT_RANGE_RIGHT("^\\]", true),
        // 
        TT_RANGE_MINUS("^-", true),
        // http://www.regular-expressions.info/ip.html
        TT_IP(// 
        "^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])", true),
        // 
        TT_RANGE_NUMBER("^\\d+", true),
        // 
        TT_WHITESPACE("^(\\s)+", false),
        TT_HOST(// 
        "^([a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])(\\.([a-z0-9][a-z0-9\\-]{0,61}[a-z0-9]))+", true),
        // 
        TT_COLON("^:", true),
        // 
        TT_IDENTIFIER("^(\\S+)[\\r|\\n]?", true, 1),
        TT_EOF("^EOF", false);

        private final String regExpattern;

        private final boolean outputToken;

        private final int gourpIndex;

        public int getGourpIndex() {
            return gourpIndex;
        }

        TokenTypes(String regExpattern, boolean outputToken) {
            this(regExpattern, outputToken, -1);
        }

        TokenTypes(String regExpattern, boolean outputToken, int group) {
            this.regExpattern = regExpattern;
            this.outputToken = outputToken;
            this.gourpIndex = group;
        }
    }

    private static List<ScanRecognizer> patternMatchers;

    public static List<ScanRecognizer> loadPatterns() {
        Pattern pattern = null;
        if (patternMatchers == null) {
            patternMatchers = new ArrayList<ScanRecognizer>();
            for (TokenTypes t : TokenTypes.values()) {
                pattern = Pattern.compile(t.regExpattern);
                patternMatchers.add(new ScanRecognizer(t, pattern, t.outputToken));
            }
        }
        return patternMatchers;
    }

    public static void main(String[] args) {
    }
}
