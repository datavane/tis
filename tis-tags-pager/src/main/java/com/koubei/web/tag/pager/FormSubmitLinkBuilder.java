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
package com.koubei.web.tag.pager;

/*
 * 页面直接跳转，用javascript 来submitform
 *         2011-3-12
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FormSubmitLinkBuilder implements LinkBuilder {

    private final String funcName;

    public FormSubmitLinkBuilder(String funcName) {
        this.funcName = funcName;
    }

    @Override
    public StringBuffer getPageUrl(int page) {
        StringBuffer funcUrl = new StringBuffer();
        funcUrl.append("javascript:");
        funcUrl.append(funcName);
        funcUrl.append("(");
        funcUrl.append(page);
        funcUrl.append(")");
        return funcUrl;
    }

    @Override
    public StringBuffer getPagerUrl() {
        throw new UnsupportedOperationException("getPagerUrl are not support,if you have any question please consult to baisui");
    }

    public static void main(String[] arg) {
        FormSubmitLinkBuilder builder = new FormSubmitLinkBuilder("goPage");
        System.out.println(builder.getPageUrl(2));
    }
}
