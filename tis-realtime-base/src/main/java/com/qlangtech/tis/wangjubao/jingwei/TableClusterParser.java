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
package com.qlangtech.tis.wangjubao.jingwei;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.qlangtech.tis.common.utils.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TableClusterParser {

    public static final DocumentBuilderFactory schemaDocumentBuilderFactory;

    static final XPathFactory xpathFactory = XPathFactory.newInstance();

    static {
        schemaDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        schemaDocumentBuilderFactory.setValidating(false);
    }

    public TableCluster parse(String value) throws Exception {
        // javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory
        // .newInstance();
        // dbf.setValidating(false);
        // // dbf.setXIncludeAware(false);
        // dbf.setNamespaceAware(true);
        DocumentBuilder builder = schemaDocumentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                InputSource source = new InputSource();
                source.setCharacterStream(new StringReader(""));
                return source;
            }
        });
        TableCluster tableCluster = new TableCluster();
        Table table = null;
        InputSource source = new InputSource();
        StringReader reader = null;
        try {
            reader = new StringReader(value);
            source.setCharacterStream(reader);
            Document document = builder.parse(source);
            final XPath xpath = xpathFactory.newXPath();
            String expression = "/doc/shareKey";
            String shareKey = (String) xpath.evaluate(expression, document, XPathConstants.STRING);
            if (StringUtils.isBlank(shareKey)) {
                throw new IllegalStateException("sharekey id not define");
            }
            tableCluster.setSharedKey(StringUtils.trimToEmpty(shareKey));
            expression = "/doc/table";
            NodeList tables = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            NodeList fields = null;
            String tabName = null;
            Node fieldNode = null;
            NamedNodeMap attrs = null;
            String name = null;
            // String alias = null;
            String primaryKey = null;
            String indexName = null;
            TabField tabField = null;
            String pk = null;
            for (int i = 0; i < tables.getLength(); i++) {
                Node node = tables.item(i);
                attrs = node.getAttributes();
                tabName = getAttr(attrs, "name");
                primaryKey = getAttr(attrs, "primaryKey");
                Assert.assertNotNull("primaryKey", primaryKey);
                indexName = getAttr(attrs, "index");
                Assert.assertNotNull("index", indexName);
                int groupSize = Integer.parseInt(getAttr(attrs, "groupSize"));
                table = new Table(tabName, indexName, groupSize);
                table.setPrimaryKey(primaryKey);
                if (attrs.getNamedItem("logkeys") != null) {
                    String[] logkeys = StringUtils.split(getAttr(attrs, "logkeys"), ",");
                    table.setLogKeys(logkeys);
                }
                fields = node.getChildNodes();
                for (int fcount = 0; fcount < fields.getLength(); fcount++) {
                    fieldNode = fields.item(fcount);
                    if (fieldNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    if ("alias".equals(fieldNode.getNodeName())) {
                        attrs = fieldNode.getAttributes();
                        name = getAttr(attrs, "name");
                        Assert.assertNotNull("name can not be null", name);
                        tabField = new TabField(tabName, name);
                        tabField.setGroovyScript(StringUtils.trim(fieldNode.getTextContent()));
                        table.addAliasField(tabField);
                        continue;
                    }
                    // <remove cols="sellerId" />
                    String cols = null;
                    if ("remove".equals(fieldNode.getNodeName())) {
                        attrs = fieldNode.getAttributes();
                        cols = getAttr(attrs, "cols");
                        if (StringUtils.isEmpty(cols)) {
                            throw new IllegalStateException("element remove's attribute cols can not be null");
                        }
                        table.setIgnorFiles(cols);
                        continue;
                    }
                    String script = null;
                    if ("ignor".equals(fieldNode.getNodeName())) {
                        script = StringUtils.trim(fieldNode.getTextContent());
                        if (StringUtils.isEmpty(script)) {
                            throw new IllegalStateException("element ignor's content can not be null");
                        }
                        TableIgnorRule ignorRule = new TableIgnorRule();
                        ignorRule.setGroovyScript(table.getName(), fcount, script);
                        table.addRecordIgnorRule(ignorRule);
                        continue;
                    }
                    if ("deletecriteria".equalsIgnoreCase(fieldNode.getNodeName())) {
                        tabField = new TabField(tabName, "deletecriteria");
                        tabField.setGroovyScript(StringUtils.trim(fieldNode.getTextContent()));
                        table.setDeleteCriteria(tabField);
                        continue;
                    }
                }
                tableCluster.add(table);
            }
        } finally {
            reader.close();
        }
        return tableCluster;
    }

    private static String getAttr(NamedNodeMap attrs, String name) {
        Node attr = attrs == null ? null : attrs.getNamedItem(name);
        if (attr == null) {
            throw new RuntimeException(name + ": missing mandatory attribute '" + name + "'");
        }
        String val = attr.getNodeValue();
        return val;
    }

    public static void main(String[] args) throws Exception {
        TableClusterParser parse = new TableClusterParser();
        TableCluster cluster = parse.parse(FileUtils.readFileToString(new File("D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\tis-realtime-transfer\\src\\main\\resources\\wjb.xml_bak")));
        System.out.println(cluster.getSharedKey());
        Table table = cluster.getTable("t_buyer");
        System.out.println(table.getName());
        System.out.println(table.getPrimaryKey());
        System.out.println(table.shallIgnor("sellerId"));
    // <field column="aaa" solrFieldName="bbbb" type="" />
    // String alias = table.findAliasColumn("aaa");
    // 
    // System.out.println(alias);
    // System.out.println(field.getType());
    }
}
