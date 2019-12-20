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

public class TestIKTokenFilter extends TestCase {

	public void test() throws Exception {

		final String messageContent = IOUtils.toString(this.getClass().getResourceAsStream("message_content.txt"),
				"utf8");

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
			writer.println(l.getLexemeText() + ",start:" + l.getBegin() + ",end:" + l.getEndPosition() + ",offset:"
					+ l.getOffset() + ",length:" + l.getLength());
			// }
		}

		writer.flush();
		writer.close();
	}
}
