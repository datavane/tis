/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.common;

import java.io.IOException;
import java.io.Writer;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;
import com.opensymphony.xwork2.util.ValueStack;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisPushDirective extends Directive {

	@Override
	public String getName() {
		return "tpush";
	}

	public boolean render(InternalContextAdapter ctx, Writer writer, Node node)
			throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
		ValueStack stack = (ValueStack) ctx.get("stack");

		stack.push(createPropertyMap(ctx, node));
		if (getType() == BLOCK) {
			Node body = node.jjtGetChild(node.jjtGetNumChildren() - 1);
			body.render(ctx, writer);
		}
		stack.pop();
		return true;
	}

	protected Object createPropertyMap(InternalContextAdapter contextAdapter, Node node)
			throws ParseErrorException, MethodInvocationException {
		// Map propertyMap;
		int children = node.jjtGetNumChildren();
		if (getType() == BLOCK) {
			children--;
		}
		// propertyMap = new HashMap();
		return node.jjtGetChild(0).value(contextAdapter);

	}

	@Override
	public int getType() {
		return DirectiveConstants.BLOCK;
	}

}
