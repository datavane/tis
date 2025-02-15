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
package com.qlangtech.tis.db.parser;

import com.google.common.collect.Lists;
import com.qlangtech.tis.plugin.ValidatorCommons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ScannerPatterns {

    public static final String HOST_KEY = "host";
    private static final Pattern a2zPattern = Pattern.compile("[a-z]");

    public enum TokenTypes {
        //
        TT_HOST_DESC("^" + HOST_KEY, true, 999),
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
                "^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])", true, 995),
        //
        TT_RANGE_NUMBER("^\\d+", true),
        //
        TT_WHITESPACE("^(\\s)+", false),
        TT_HOST(//
                "(^([a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])(\\.([a-z0-9][a-z0-9\\-]{0,61}[a-z0-9]))*)|^localhost"
                , true, new Group(-1), 996, (testToken) -> {
            Matcher matcher = a2zPattern.matcher(testToken);
            // 存在 a-z的字符 说明是一个host地址
            return matcher.find();
        }),
        //
        TT_COLON("^:", true, 998),
        //
        TT_IDENTIFIER("^(" + ValidatorCommons.pattern_identity.pattern() + ")[\\r|\\n]?", true, new Group(1)),
        TT_PASSWORD_VALUE("^(\\S+)[\\r|\\n]?", true, new Group(1)),
        TT_EOF("^EOF", false);

        private final String regExpattern;

        private final boolean outputToken;

        private final Group gourpIndex;
        private final int priority;
        /**
         * 有些情况需要进一步确认，例如 TT_HOST，"192.168.28" 会作为合法的host地址，但是其实它应该是一个不合法的ip地址
         */
        public final Function<String, Boolean> furtherChecker;

        public int getGourpIndex() {
            return gourpIndex.gourpIndex;
        }

        TokenTypes(String regExpattern, boolean outputToken) {
            this(regExpattern, outputToken, 0);
        }

        TokenTypes(String regExpattern, boolean outputToken, int priority) {
            this(regExpattern, outputToken, new Group(-1), priority, (test) -> true);
        }

        TokenTypes(String regExpattern, boolean outputToken, Group group) {
            this(regExpattern, outputToken, group, 0, (test) -> true);
        }

        TokenTypes(String regExpattern, boolean outputToken, Group group, int priority, Function<String, Boolean> furtherChecker) {
            this.regExpattern = regExpattern;
            this.outputToken = outputToken;
            this.gourpIndex = group;
            this.priority = priority;
            this.furtherChecker = furtherChecker;
        }

        public Pattern createPattern() {
            return Pattern.compile(regExpattern);
        }

    }

    private static class Group {
        final int gourpIndex;

        public Group(int group) {
            this.gourpIndex = group;
        }
    }

    private static List<ScanRecognizer> patternMatchers;

    public static List<ScanRecognizer> loadPatterns() {
        Pattern pattern = null;
        if (patternMatchers == null) {
            synchronized (ScannerPatterns.class) {
                if (patternMatchers == null) {
                    patternMatchers = new ArrayList<ScanRecognizer>();

                    ArrayList<TokenTypes> tokens = Lists.newArrayList(TokenTypes.values());
                    Collections.sort(tokens, new Comparator<TokenTypes>() {
                        @Override
                        public int compare(TokenTypes t1, TokenTypes t2) {
                            return t2.priority - t1.priority;
                        }
                    });
                    for (TokenTypes t : tokens) {
                        pattern = t.createPattern();
                        patternMatchers.add(new ScanRecognizer(t, pattern, t.outputToken));
                    }
                    patternMatchers = Collections.unmodifiableList(patternMatchers);
                }
            }
        }
        return patternMatchers;
    }

    public static void main(String[] args) {
    }
}
