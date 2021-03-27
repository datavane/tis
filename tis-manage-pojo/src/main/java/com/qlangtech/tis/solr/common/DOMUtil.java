/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.solr.common;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月7日 上午10:36:33
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
