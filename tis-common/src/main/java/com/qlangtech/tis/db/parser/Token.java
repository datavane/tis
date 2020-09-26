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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Token {

    private final TokenTypes token;

    private final String content;

    public Token(TokenTypes token, String content) {
        super();
        this.token = token;
        this.content = content.trim();
    }

    public TokenTypes getToken() {
        return this.token;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isTokenType(TokenTypes tokenType) {
        // System.out.println("judge:" + tokenType);
        return this.token == tokenType;
    }

    @Override
    public String toString() {
        return token + " " + content;
    }
}
