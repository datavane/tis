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
package org.apache.solr.common.util;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DOMUtil {

    public static final String XML_RESERVED_PREFIX = "xml";

    public static String getAttr(NamedNodeMap attrs, String name) {
        return getAttr(attrs, name, null);
    }

    public static String getAttr(Node nd, String name) {
        return getAttr(nd.getAttributes(), name);
    }

    public static String getAttr(NamedNodeMap attrs, String name, String missing_err) {
        Node attr = attrs == null ? null : attrs.getNamedItem(name);
        if (attr == null) {
            if (missing_err == null)
                return null;
            throw new RuntimeException(missing_err + ": missing mandatory attribute '" + name + "'");
        }
        String val = attr.getNodeValue();
        return val;
    }

    public static String getAttr(Node node, String name, String missing_err) {
        return getAttr(node.getAttributes(), name, missing_err);
    }

    public static Map<String, String> toMap(NamedNodeMap attrs) {
        return toMapExcept(attrs);
    }

    public static Map<String, String> toMapExcept(NamedNodeMap attrs, String... exclusions) {
        Map<String, String> args = new HashMap<String, String>();
        outer: for (int j = 0; j < attrs.getLength(); j++) {
            Node attr = attrs.item(j);
            // automatically exclude things in the xml namespace, ie: xml:base
            if (XML_RESERVED_PREFIX.equals(attr.getPrefix()))
                continue outer;
            String attrName = attr.getNodeName();
            for (String ex : exclusions) if (ex.equals(attrName))
                continue outer;
            String val = attr.getNodeValue();
            args.put(attrName, val);
        }
        return args;
    }
}
