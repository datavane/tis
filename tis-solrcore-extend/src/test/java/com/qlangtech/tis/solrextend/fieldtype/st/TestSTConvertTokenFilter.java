package com.qlangtech.tis.solrextend.fieldtype.st;

import junit.framework.TestCase;

public class TestSTConvertTokenFilter extends TestCase {

	public void testST() {
		String converted = STConverter.getInstance().convert("我爱北京天安门", STConvertType.SIMPLE_2_TRADITIONAL);

		System.out.println(converted);

		System.out.println(STConverter.getInstance().convert(converted, STConvertType.TRADITIONAL_2_SIMPLE));

	}
}
