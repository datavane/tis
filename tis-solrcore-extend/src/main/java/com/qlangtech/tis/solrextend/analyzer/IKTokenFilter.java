package com.qlangtech.tis.solrextend.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import com.google.common.collect.Lists;

public class IKTokenFilter extends TokenFilter {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	static final Comparator<Lexeme> compar = new Comparator<Lexeme>() {
		public int compare(Lexeme o1, Lexeme o2) {
			int r = o1.getBegin() - o2.getBegin();
			return r == 0 ? o1.getEndPosition() - o2.getEndPosition() : r;
		}
	};

	// private IKSegmenter ikSeg;
	private Iterator<Lexeme> lsIterator;
	private int preBegin = -1;

	public IKTokenFilter(TokenStream input) {
		super(input);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		this.clearAttributes();
		String content = null;
		while (true) {
			if (this.lsIterator == null) {

				if (!this.input.incrementToken()) {
					return false;
				}
				IKSegmenter ikSeg = new IKSegmenter(new StringReader(this.termAtt.toString()), false /* smart */);
				List<Lexeme> ls = Lists.newArrayList();
				Lexeme l = null;
				while ((l = ikSeg.next()) != null) {
					// if (StringUtils.length(l.getLexemeText()) > 1) {
					ls.add(l);
					// }
				}
				ls.sort(compar);
				this.lsIterator = ls.iterator();
			}

			Lexeme l = null;

			if (this.lsIterator.hasNext()) {
				l = lsIterator.next();
				content = l.getLexemeText();
				// 必须是普通word
				if ((l.getBeginPosition() > preBegin)) {
					// System.out.println(content);
					// if (StringUtils.length(content) > 1) {
					this.posIncrAtt.setPositionIncrement(1);
					this.preBegin = l.getBeginPosition();
					// }
				} else {
					this.posIncrAtt.setPositionIncrement(0);
				}
				offsetAtt.setOffset(l.getBegin(), l.getEndPosition());
				this.termAtt.copyBuffer(content.toCharArray(), 0, content.length());
				return true;
			} else {
				this.lsIterator = null;
				this.preBegin = -1;
			}
		}
	}

	@Override
	public void end() throws IOException {
		super.end();
	}

	public static void main(String[] args) throws Exception {
		IKSegmenter ikSeg = new IKSegmenter(new StringReader("我爱北京天安门"), false /* smart */);
		Lexeme l = null;
		// while ((l = ikSeg.next()) != null) {
		// System.out.println(
		// l + ",begin:" + l.getBeginPosition() + ",end:" + l.getEndPosition() +
		// ",offset:" + l.getOffset());
		// }

		System.out.println("=========================================");

		ikSeg = new IKSegmenter(new StringReader("马尼拉二手交易市场"), false /* smart */);
		List<Lexeme> ls = Lists.newArrayList();
		while ((l = ikSeg.next()) != null) {
			ls.add(l);
			// System.out.println(l);
		}

		ls.sort(compar);

		int preBegin = -1;
		int preEnd = -1;
		for (Lexeme le : ls) {
			// if (le.getLexemeType() == 4 && (le.getBeginPosition() >
			// preBegin)) {
			// System.out.println(le + "," + le.getLexemeType());
			// preBegin = le.getBeginPosition();
			// }

			System.out.println(le + "," + le.getLexemeType());

			// System.out.println(le + "," + le.getLexemeType());
		}

	}

}
