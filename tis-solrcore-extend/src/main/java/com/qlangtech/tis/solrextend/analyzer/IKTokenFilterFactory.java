package com.qlangtech.tis.solrextend.analyzer;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class IKTokenFilterFactory extends TokenFilterFactory {

	public IKTokenFilterFactory(Map<String, String> args) {
		super(args);

	}

	@Override
	public TokenStream create(TokenStream input) {
		return new IKTokenFilter(input);
	}

}
