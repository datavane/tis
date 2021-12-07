/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
