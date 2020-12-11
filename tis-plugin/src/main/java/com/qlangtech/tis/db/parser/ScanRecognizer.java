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

import com.qlangtech.tis.db.parser.ScannerPatterns.TokenTypes;

import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ScanRecognizer {

    private final TokenTypes tokenTypes;

    private final Pattern pattern;

    private final boolean outputToken;

    public ScanRecognizer(TokenTypes tokenTypes, Pattern pattern, boolean outputToken) {
        super();
        this.tokenTypes = tokenTypes;
        this.pattern = pattern;
        this.outputToken = outputToken;
    }

    public TokenTypes getToken() {
        return this.tokenTypes;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public boolean isOutputToken() {
        return this.outputToken;
    }
}
