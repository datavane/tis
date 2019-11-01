package com.qlangtech.tis.solrextend.analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class IKTokenFilter extends TokenFilter {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

	private IKSegmenter ikSeg;

	public IKTokenFilter(TokenStream input) {
		super(input);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		this.clearAttributes();
		String content = null;
		while (true) {
			if (this.ikSeg == null) {

				if (!this.input.incrementToken()) {
					return false;
				}
				// System.out.println("============================" +
				// this.termAtt.toString());
				ikSeg = new IKSegmenter(new StringReader(this.termAtt.toString()), false /* smart */);
			}

			Lexeme l = null;

			if ((l = ikSeg.next()) != null) {
				content = l.getLexemeText();
				// System.out.println("+++++++" + content);
				
				this.termAtt.copyBuffer(content.toCharArray(), 0, content.length());
				this.posIncrAtt.setPositionIncrement(1);
				return true;
			} else {
				ikSeg = null;
			}
		}
	}

	@Override
	public void end() throws IOException {
		super.end();
	}
	
	

}
