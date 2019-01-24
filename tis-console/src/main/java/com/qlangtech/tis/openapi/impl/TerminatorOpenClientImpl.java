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
package com.qlangtech.tis.openapi.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.openapi.Column;
import com.qlangtech.tis.openapi.ModifySchemaExcetpion;
import com.qlangtech.tis.openapi.ModifySchemaParam;
import com.qlangtech.tis.openapi.SnapshotNotFindException;
import com.qlangtech.tis.openapi.TerminatorOpenClient;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.jarcontent.SaveFileContentAction;
import com.qlangtech.tis.runtime.module.action.jarcontent.SaveFileContentAction.CreateSnapshotResult;
import com.qlangtech.tis.runtime.module.action.jarcontent.SnapshotRevsionAction;
import com.qlangtech.tis.runtime.module.misc.DefaultMessageHandler;
import com.qlangtech.tis.runtime.module.misc.MessageHandler;
import com.qlangtech.tis.solrdao.SolrFieldsParser;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorOpenClientImpl implements TerminatorOpenClient {

    private static final long serialVersionUID = 1L;

    private SnapshotDomainGetter snapshotGetter;

    private RunContext runContext;

    public void setRunContext(RunContext runContext) {
        this.runContext = runContext;
        this.snapshotGetter = new SnapshotDomainGetter(runContext);
    }

    private static final SolrFieldsParser solrFiledParser = new SolrFieldsParser();

    @SuppressWarnings("all")
    @Override
    public void modifySchema(String appName, String runtime, ModifySchemaParam param) throws ModifySchemaExcetpion {
    // // String appName, Short groupIndex, RunEnvironment runtime,
    // boolean unmergeglobalparams = true;
    // AppKey appKey = new AppKey(appName, (short) 0,
    // RunEnvironment.getEnum(runtime), unmergeglobalparams);
    // 
    // InputStream schemaReader = null;
    // try {
    // SnapshotDomain snapshot = snapshotGetter.getSnapshot(appKey);
    // schemaReader = new ByteArrayInputStream(snapshot.getSolrSchema()
    // .getContent());
    // 
    // 
    // 
    // List<FieldType> fieldTypes = parseTypes(SolrFieldsParser.schemaDocumentBuilderFactory
    // .newDocumentBuilder().parse(schemaReader));
    // 
    // final List<FieldType> parsedSchema = Collections
    // .unmodifiableList(fieldTypes);
    // 
    // fieldTypes = getFields(fieldTypes, 0);
    // 
    // for (Column col : param.getDelete()) {
    // Iterator<FieldType> it = fieldTypes.iterator();
    // while (it.hasNext()) {
    // if (StringUtils.equals(it.next().getElementName(),
    // col.getName())) {
    // it.remove();
    // }
    // }
    // }
    // 
    // Set<String> fields = new HashSet<String>();
    // for (FieldType f : fieldTypes) {
    // fields.add(f.attribue.get(PROP_NAME));
    // }
    // 
    // FieldType addFieldType = null;
    // for (Column col : param.getAdd()) {
    // 
    // if (fields.contains(col.getName())) {
    // throw new ModifySchemaExcetpion("column:" + col.getName()
    // + " has been duplicated");
    // }
    // 
    // addFieldType = new FieldType("field");
    // addFieldType.addAttribue("name", col.getName());
    // addFieldType.addAttribue("type", col.getFieldType()
    // .getSolrType());
    // addFieldType.addAttribue("indexed",
    // Boolean.toString(col.isIndex()));
    // addFieldType.addAttribue("stored",
    // Boolean.toString(col.isStored()));
    // addFieldType.addAttribue("multiValued", "false");
    // 
    // fieldTypes.add(addFieldType);
    // }
    // 
    // StringBuffer outprint = new StringBuffer();
    // outprint.append("<?xml version=\"1.0\" ?>\n");
    // outprint.append("<!DOCTYPE schema SYSTEM \"http://terminator.admin.tbsite.net:9999/dtd/solrschema.dtd\">\n");
    // 
    // for (FieldType schema : parsedSchema) {
    // this.print(schema, outprint, 0);
    // break;
    // }
    // 
    // // 插入一条记录
    // 
    // final MessageHandler messageHandler = new DefaultMessageHandler();
    // 
    // BasicModule.EContext msgContext = new BasicModule.EContext();
    // 
    // // System.out.println(outprint.toString());
    // 
    // CreateSnapshotResult createResult = SaveFileContentAction
    // .createNewSnapshot(msgContext, snapshot,
    // ConfigFileReader.FILE_SCHEMA, outprint.toString()
    // .getBytes(), this.runContext,
    // messageHandler, "update by foolish api", 0L,
    // "reboot");
    // 
    // if (!createResult.isSuccess()) {
    // List<String> errorMsg = (List<String>) msgContext
    // .get(MessageHandler.ACTION_ERROR_MSG);
    // StringBuffer buffer = new StringBuffer(
    // "create new snapshot falid\n");
    // for (String err : errorMsg) {
    // buffer.append(err).append("\n");
    // }
    // throw new ModifySchemaExcetpion(buffer.toString());
    // }
    // 
    // // 切换版本
    // Application app = null;
    // ApplicationCriteria appCriteria = new ApplicationCriteria();
    // appCriteria.createCriteria().andProjectNameEqualTo(appName);
    // for (Application application : runContext.getApplicationDAO()
    // .selectByExample(appCriteria)) {
    // app = application;
    // break;
    // }
    // 
    // if (app == null) {
    // throw new IllegalStateException("appname:" + appName
    // + " can not fetch object application from db");
    // }
    // 
    // RunEnvironment runEnvironment = RunEnvironment.getEnum(runtime);
    // 
    // ServerGroup group = runContext.getServerGroupDAO().load(appName,
    // (short) 0, runEnvironment.getId());
    // 
    // Assert.assertNotNull(group);
    // 
    // // 切换snapshot 版本
    // SnapshotRevsionAction.change2newSnapshot(createResult.getNewId(),
    // "auto create change to new snapshot", group, app,
    // RunEnvironment.getEnum(runtime), runContext);
    // 
    // // 推送配置文件到服务端
    // 
    // } catch (ModifySchemaExcetpion e) {
    // throw e;
    // } catch (SnapshotNotFindException e) {
    // throw new RuntimeException(e);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // } finally {
    // IOUtils.closeQuietly(schemaReader);
    // }
    }

    private static String[] schemaPath = new String[] { "schema", "fields" };

    private List<FieldType> getFields(List<FieldType> sources, int schemaPathIndex) {
        if (schemaPathIndex >= schemaPath.length) {
            throw new IllegalArgumentException("schemaPathIndex[" + schemaPathIndex + "] >= schemaPath.length[" + schemaPath.length + "]");
        }
        // for (String path : schemaPath) {
        for (FieldType fieldType : sources) {
            if (StringUtils.equals(fieldType.getElementName(), schemaPath[schemaPathIndex])) {
                if (schemaPathIndex == schemaPath.length - 1) {
                    return fieldType.getChildren();
                } else {
                    return getFields(fieldType.getChildren(), schemaPathIndex + 1);
                }
            }
        }
        throw new IllegalStateException("has not find any match fieldtype");
    }

    private static final XPathFactory xpathFactory = XPathFactory.newInstance();

    public List<FieldType> parseTypes(Document document) throws XPathExpressionException {
        List<FieldType> fieldTypes = new ArrayList<FieldType>();
        final XPath xpath = xpathFactory.newXPath();
        // ParseResult parseResult = new ParseResult();
        // final ArrayList<PSchemaField> dFields = parseResult.dFields;
        // 取得fields type
        // String expression =
        // "/schema/types/fieldType|/schema/types/fieldtype";
        String expression = "/schema";
        NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            FieldType fieldType = new FieldType(node.getNodeName());
            parseNode(fieldType, node);
            fieldTypes.add(fieldType);
        }
        return fieldTypes;
    }

    private static final ApplicationContext context;

    static {
        context = new ClassPathXmlApplicationContext("/dal/dal-dao.xml", "/com/taobao/terminator/coredefine/coredefine.context.xml");
    }

    public static void main(String[] arg) throws Exception {
        TerminatorOpenClientImpl terminatorOpenClientImpl = new TerminatorOpenClientImpl();
        RunContext runContext = (RunContext) context.getBean("daoContext");
        terminatorOpenClientImpl.setRunContext(runContext);
        ModifySchemaParam param = new ModifySchemaParam();
        Column column = new Column("testCol");
        column.setFieldType(Column.Type.LONG);
        param.add(column);
        terminatorOpenClientImpl.modifySchema("search4baisui3k", "daily", param);
        System.out.println("success...................");
    // URL url = new URL(
    // "http://10.125.198.32:8080/terminator-search/search4realjhsItem-0/admin/file/?contentType=text/xml;charset=utf-8&file=schema.xml");
    // 
    // InputStream reader = null;
    // try {
    // reader = url.openStream();
    // 
    // List<FieldType> fieldsTypes = terminatorOpenClientImpl
    // .parseTypes(SolrFieldsParser.schemaDocumentBuilderFactory
    // .newDocumentBuilder().parse(reader));
    // StringBuffer outprint = new StringBuffer();
    // for (FieldType type : fieldsTypes) {
    // terminatorOpenClientImpl.print(type, outprint, 0);
    // }
    // 
    // System.out.println(outprint);
    // } finally {
    // IOUtils.closeQuietly(reader);
    // }
    }

    private static final String PROP_NAME = "name";

    private void print(FieldType type, StringBuffer outprint, final int indent) {
        outprint.append("\n");
        indent(outprint, indent).append("<").append(type.elementName).append(" ");
        // 写field属性
        if (StringUtils.isNotBlank(type.attribue.get(PROP_NAME))) {
            outprint.append(PROP_NAME).append("=\"").append(type.attribue.get(PROP_NAME)).append("\" ");
        }
        for (Map.Entry<String, String> entry : type.attribue.entrySet()) {
            if (PROP_NAME.equals(entry.getKey())) {
                continue;
            }
            outprint.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
        }
        if (type.children.isEmpty() && StringUtils.isBlank(type.getTextContent())) {
            outprint.append(" />\n");
            return;
        }
        outprint.append(">");
        if (StringUtils.isNotBlank(type.getTextContent())) {
            outprint.append(type.getTextContent());
        }
        for (FieldType ctype : type.children) {
            print(ctype, outprint, indent + 1);
        }
        indent(outprint, indent, type.children.isEmpty()).append("</").append(type.elementName).append(">\n");
    }

    private StringBuffer indent(StringBuffer outprint, int indent) {
        return indent(outprint, indent, false);
    }

    private StringBuffer indent(StringBuffer outprint, int indent, boolean skip) {
        if (skip) {
            return outprint;
        }
        for (int i = 0; i < indent * 2; i++) {
            outprint.append(" ");
        }
        return outprint;
    }

    private void parseNode(FieldType fieldType, Node node) {
        NamedNodeMap attrs = node.getAttributes();
        Node attr = null;
        for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
            attr = attrs.item(j);
            fieldType.addAttribue(attr.getNodeName(), attr.getNodeValue());
        }
        FieldType child = null;
        String nodeName = null;
        Node n = null;
        for (int k = 0; k < node.getChildNodes().getLength(); k++) {
            n = node.getChildNodes().item(k);
            nodeName = n.getNodeName();
            if ("#text".equals(nodeName)) {
                if (StringUtils.isNotBlank(n.getNodeValue())) {
                    fieldType.setTextContent(n.getNodeValue());
                }
                continue;
            }
            child = new FieldType(nodeName);
            fieldType.children.add(child);
            parseNode(child, n);
        }
    }

    public static class FieldType {

        private final String elementName;

        private String textContent;

        public FieldType(String elementName) {
            super();
            this.elementName = elementName;
        }

        public String getTextContent() {
            return textContent;
        }

        public void setTextContent(String textContent) {
            this.textContent = textContent;
        }

        public String getElementName() {
            return elementName;
        }

        private final Map<String, String> attribue = new HashMap<String, String>();

        private final List<FieldType> children = new ArrayList<FieldType>();

        public void addAttribue(String name, String value) {
            this.attribue.put(name, value);
        }

        public List<FieldType> getChildren() {
            return children;
        }
    }
}
