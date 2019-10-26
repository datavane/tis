package com.qlangtech.tis.solrextend.analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class IKTokenFilter extends TokenFilter {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	private IKSegmenter ikSeg;

	public IKTokenFilter(TokenStream input) {
		super(input);
	}

	@Override
	public final boolean incrementToken() throws IOException {

		String content = null;
		while (true) {
			if (this.ikSeg == null) {

				if (!this.input.incrementToken()) {
					return false;
				}
				System.out.println("============================" + this.termAtt.toString());
				ikSeg = new IKSegmenter(new StringReader(this.termAtt.toString()), false /* smart */);
			}

			Lexeme l = null;

			if ((l = ikSeg.next()) != null) {
				content = l.getLexemeText();
				System.out.println("+++++++" + content);
				this.clearAttributes();
				this.termAtt.copyBuffer(content.toCharArray(), 0, content.length());

				return true;
			} else {
				ikSeg = null;
			}

		}

	}

}
