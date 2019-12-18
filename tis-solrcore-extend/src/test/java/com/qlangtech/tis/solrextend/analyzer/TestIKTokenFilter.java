package com.qlangtech.tis.solrextend.analyzer;

import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import junit.framework.TestCase;

public class TestIKTokenFilter extends TestCase {

	public void test() throws Exception {

		final String messageContent = IOUtils.toString(this.getClass().getResourceAsStream("message_content.txt"),
				"utf8");

		System.out.println(messageContent);

		System.out.println("messageContent.length():" + messageContent.length());

		IKSegmenter ikSeg = new IKSegmenter(new StringReader(messageContent), false /* smart */);
		Lexeme l = null;
		while ((l = ikSeg.next()) != null) {

//			if (!"open".equals(l.getLexemeText())) {
//				continue;
//			}
			// if (StringUtils.length(l.getLexemeText()) > 1) {
			System.out.println(l.getLexemeText() + ",start:" + l.getBegin() + ",end:" + l.getEndPosition() + ",offset:"
					+ l.getOffset() + ",length:" + l.getLength());
			// }
		}
	}
}
