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
package com.qlangtech.tis.manage.common.solr;

import java.net.URLEncoder;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrDocument;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GeneralXMLResponseParser extends XMLResponseParser {

    protected SolrDocument readDocument(XMLStreamReader parser) throws XMLStreamException {
        if (XMLStreamConstants.START_ELEMENT != parser.getEventType()) {
            throw new RuntimeException("must be start element, not: " + parser.getEventType());
        }
        if (!"doc".equals(parser.getLocalName().toLowerCase())) {
            throw new RuntimeException("must be 'lst', not: " + parser.getLocalName());
        }
        SolrDocument doc = new SolrDocument();
        StringBuilder builder = new StringBuilder();
        KnownType type = null;
        String name = null;
        // just eat up the events...
        int depth = 0;
        while (true) {
            switch(parser.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    depth++;
                    // reset the text
                    builder.setLength(0);
                    type = KnownType.get(parser.getLocalName());
                    if (type == null) {
                        type = KnownType.STR;
                    }
                    // if (type == null) {
                    // throw new RuntimeException("this must be known type! not: "
                    // + parser.getLocalName());
                    // }
                    // 百岁修改 2012/04/23 为了solr不同版本之间兼容 end
                    name = null;
                    int cnt = parser.getAttributeCount();
                    for (int i = 0; i < cnt; i++) {
                        if ("name".equals(parser.getAttributeLocalName(i))) {
                            name = parser.getAttributeValue(i);
                            break;
                        }
                    }
                    if (name == null) {
                        throw new XMLStreamException("requires 'name' attribute: " + parser.getLocalName(), parser.getLocation());
                    }
                    // Handle multi-valued fields
                    if (type == KnownType.ARR) {
                        for (Object val : readArray(parser)) {
                            doc.addField(name, val);
                        }
                        // the array reading clears out the 'endElement'
                        depth--;
                    }
                    // }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (--depth < 0) {
                        return doc;
                    }
                    // System.out.println( "FIELD:"+type+"::"+name+"::"+builder );
                    Object val = type.read(builder.toString().trim());
                    if (val == null) {
                        throw new XMLStreamException("error reading value:" + type, parser.getLocation());
                    }
                    doc.addField(name, val);
                    break;
                // TODO? should this be trimmed? make
                case XMLStreamConstants.SPACE:
                // sure it only gets one/two space?
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.CHARACTERS:
                    builder.append(parser.getText());
                    break;
            }
        }
    }

    public static void main(String[] arg) throws Exception {
    }
}
