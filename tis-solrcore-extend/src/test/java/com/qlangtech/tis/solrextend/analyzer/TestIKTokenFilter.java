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
package com.qlangtech.tis.solrextend.analyzer;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import com.qlangtech.tis.solrextend.fieldtype.st.STConvertType;
import com.qlangtech.tis.solrextend.fieldtype.st.STConverter;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class TestIKTokenFilter extends TestCase {

    public void test() throws Exception {
        final String messageContent = IOUtils.toString(this.getClass().getResourceAsStream("message_content.txt"), "utf8");
        final String converted = STConverter.getInstance().convert(messageContent, STConvertType.TRADITIONAL_2_SIMPLE);
        System.out.println(converted);
        System.out.println("messageContent.length():" + messageContent.length());
        OutputStream output = FileUtils.openOutputStream(new File("terms.txt"));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "utf8"));
        writer.println(converted);
        writer.println("converted========================================================");
        TisIKConfiguration ikConfig = new TisIKConfiguration();
        ikConfig.setUseSmart(false);
        IKSegmenter ikSeg = new IKSegmenter(new StringReader(converted), ikConfig);
        Lexeme l = null;
        while ((l = ikSeg.next()) != null) {
            // if (!"open".equals(l.getLexemeText())) {
            // continue;
            // }
            // if (StringUtils.length(l.getLexemeText()) > 1) {
            writer.println(l.getLexemeText() + ",start:" + l.getBegin() + ",end:" + l.getEndPosition() + ",offset:" + l.getOffset() + ",length:" + l.getLength());
        // }
        }
        writer.flush();
        writer.close();
    }
}
