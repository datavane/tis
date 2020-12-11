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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DBTokenizer {

    private String scannerBuffer;

    private final ArrayList<Token> tokenList = new ArrayList<>();

    private final List<ScanRecognizer> recognizerPatterns;

    public DBTokenizer(String content) {
        this.scannerBuffer = content;
        this.recognizerPatterns = ScannerPatterns.loadPatterns();
    }

    public TokenBuffer getTokenBuffer() {
        return new TokenBuffer(this.tokenList);
    }

    public void parse() {
        boolean parseInProgress = true;
        while (parseInProgress) {
            Iterator<ScanRecognizer> patternIterator = recognizerPatterns.iterator();
            parseInProgress = matchToken(patternIterator);
        }
    }

    /**
     * @param patternIterator
     * @return
     */
    private boolean matchToken(Iterator<ScanRecognizer> patternIterator) {
        boolean tokenMatch;
        ScanRecognizer recognizer;
        Pattern pattern;
        Matcher matcher;
        boolean result;
        tokenMatch = false;
        result = true;
        String content = null;
        do {
            recognizer = patternIterator.next();
            pattern = recognizer.getPattern();
            matcher = pattern.matcher(scannerBuffer);
            if (matcher.find()) {
                // System.out.println(matcher.group());
                if (recognizer.isOutputToken()) {
                    content = recognizer.getToken().getGourpIndex() > 0 ? matcher.group(recognizer.getToken().getGourpIndex()) : matcher.group();
                    tokenList.add(new Token(recognizer.getToken(), content));
                }
                tokenMatch = true;
                scannerBuffer = scannerBuffer.substring(matcher.end());
            // System.out.println(scannerBuffer);
            }
        } while (patternIterator.hasNext() && !tokenMatch);
        if (// || (matcher.end() == scannerBuffer.length())
        !tokenMatch) {
            result = false;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        File f = new File("./db_config.txt");
        String content = FileUtils.readFileToString(f, "utf8");
        System.out.println(content);
        DBTokenizer tokenizer = new DBTokenizer(content);
        tokenizer.parse();
        for (Token t : tokenizer.tokenList) {
            System.out.println(t.getContent() + "               " + t.getToken());
        }
    }
}
