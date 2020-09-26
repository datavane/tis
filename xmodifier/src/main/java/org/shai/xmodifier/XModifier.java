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

/**
 * Created by Shenghai on 14-11-24.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
