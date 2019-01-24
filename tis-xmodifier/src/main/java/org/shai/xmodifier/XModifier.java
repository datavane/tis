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
package org.shai.xmodifier;

import org.shai.xmodifier.exception.XModifyFailException;
import org.shai.xmodifier.util.ArrayUtils;
import org.shai.xmodifier.util.StringUtils;
import org.w3c.dom.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.*;

/*
 * Created by Shenghai on 14-11-24.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class XModifier {

    private final Document document;

    private Map<String, String> nsMap = new HashMap<String, String>();

    private List<XModifyNode> xModifyNodes = new ArrayList<XModifyNode>();

    private XPath xPathEvaluator;

    public XModifier(Document document) {
        this.document = document;
    }

    public void setNamespace(String prefix, String url) {
        nsMap.put(prefix, url);
    }

    public void addModify(String xPath, String value) {
        xModifyNodes.add(new XModifyNode(nsMap, xPath, value));
    }

    public void addModify(String xPath) {
        xModifyNodes.add(new XModifyNode(nsMap, xPath, null));
    }

    public void modify() {
        initXPath();
        for (XModifyNode xModifyNode : xModifyNodes) {
            try {
                create(document, xModifyNode);
            } catch (Exception e) {
                throw new XModifyFailException(xModifyNode.toString(), e);
            }
        }
    }

    private void create(Node parent, XModifyNode node) throws XPathExpressionException {
        Node newNode;
        if (node.isAttributeModifier()) {
            // attribute
            createAttributeByXPath(parent, node.getCurNode().substring(1), node.getValue());
        } else {
            // element
            if (node.isRootNode()) {
                // root node
                newNode = parent;
                boolean canMoveToNext = node.moveNext();
                if (!canMoveToNext) {
                    // last node
                    newNode.setTextContent(node.getValue());
                } else {
                    // next node
                    create(newNode, node);
                }
            } else if (node.getCurNode().equals("text()")) {
                parent.setTextContent(node.getValue());
            } else {
                // element
                findOrCreateElement(parent, node);
            }
        }
    }

    private void initXPath() {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                return nsMap.get(prefix);
            }

            @Override
            public String getPrefix(String namespaceURI) {
                for (Map.Entry<String, String> entry : nsMap.entrySet()) {
                    if (entry.getValue().equals(namespaceURI)) {
                        return entry.getKey();
                    }
                }
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return nsMap.keySet().iterator();
            }
        });
        this.xPathEvaluator = xPath;
    }

    private void createAttributeByXPath(Node node, String current, String value) {
        ((Element) node).setAttribute(current, value);
    }

    private void findOrCreateElement(Node parent, XModifyNode node) throws XPathExpressionException {
        if (node.isAdding()) {
            // create new element without double check
            Node newCreatedNode = createNewElement(node.getNamespaceURI(), node.getLocalName(), node.getConditions());
            parent.appendChild(newCreatedNode);
            boolean canMoveToNext = node.moveNext();
            if (!canMoveToNext) {
                // last node
                newCreatedNode.setTextContent(node.getValue());
            } else {
                // next node
                create(newCreatedNode, node);
            }
            return;
        }
        if (node.isInsertBefore()) {
            // create new element without double check
            Node newCreatedNode = createNewElement(node.getNamespaceURI(), node.getLocalName(), node.getConditions());
            Node referNode = (Node) xPathEvaluator.evaluate(node.getInsertBeforeXPath(), parent, XPathConstants.NODE);
            parent.insertBefore(newCreatedNode, referNode);
            boolean canMoveToNext = node.moveNext();
            if (!canMoveToNext) {
                // last node
                newCreatedNode.setTextContent(node.getValue());
            } else {
                // next node
                create(newCreatedNode, node);
            }
            return;
        }
        NodeList existNodeList = (NodeList) xPathEvaluator.evaluate(node.getCurNodeXPath(), parent, XPathConstants.NODESET);
        if (existNodeList.getLength() > 0) {
            for (int i = 0; i < existNodeList.getLength(); i++) {
                XModifyNode newNode = node.duplicate();
                Node item = existNodeList.item(i);
                if (node.isDeleting()) {
                    parent.removeChild(item);
                    continue;
                }
                boolean canMoveToNext = newNode.moveNext();
                if (!canMoveToNext) {
                    // last node
                    item.setTextContent(node.getValue());
                } else {
                    // next node
                    create(item, newNode);
                }
            }
        } else {
            Node newCreatedNode = createNewElement(node.getNamespaceURI(), node.getLocalName(), node.getConditions());
            parent.appendChild(newCreatedNode);
            Node checkExistNode = (Node) xPathEvaluator.evaluate(node.getCurNodeXPath(), parent, XPathConstants.NODE);
            if (!newCreatedNode.equals(checkExistNode)) {
                throw new XModifyFailException("Error to create " + node.getCurNode());
            }
            boolean canMoveToNext = node.moveNext();
            if (!canMoveToNext) {
                // last node
                newCreatedNode.setTextContent(node.getValue());
            } else {
                // next node
                create(newCreatedNode, node);
            }
        }
    }

    private Element createNewElement(String namespaceURI, String local, String[] conditions) throws XPathExpressionException {
        Element newElement = null;
        if (namespaceURI != null) {
            newElement = document.createElementNS(namespaceURI, local);
        } else {
            newElement = document.createElement(local);
        }
        if (ArrayUtils.isNotEmpty(conditions)) {
            for (String condition : conditions) {
                if (StringUtils.containsOnly(condition, "0123456789")) {
                    continue;
                }
                // TODO: support not( ) function, need to refactory
                if (condition.startsWith("not")) {
                    continue;
                }
                String[] strings = StringUtils.splitBySeparator(condition, '=');
                String xpath = strings[0];
                String value = StringUtils.unquote(strings[1]);
                create(newElement, new XModifyNode(nsMap, xpath, value));
            }
        }
        return newElement;
    }
}
