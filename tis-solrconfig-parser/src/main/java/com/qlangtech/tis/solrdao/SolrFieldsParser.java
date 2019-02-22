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
package com.qlangtech.tis.solrdao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.solrdao.extend.IndexBuildHook;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SolrFieldsParser {

    static final XPathFactory xpathFactory = XPathFactory.newInstance();

    public static XPath createXPath() {
        return xpathFactory.newXPath();
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        SolrFieldsParser parse = new SolrFieldsParser();
        File f = new File("D:/workspace/solrhome/supplyGoods/conf/ccc.xml");
        InputStream is = new FileInputStream(f);
        ParseResult result = parse.parseSchema(is, false);
        System.out.println(result.getIndexBuilder());
    }

    public static boolean hasMultiValuedField(ArrayList<PSchemaField> fields) {
        for (PSchemaField field : fields) {
            if (field.isMltiValued()) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<PSchemaField> readSchemaFields(InputStream is) throws Exception {
        ParseResult result = this.parseSchema(is);
        if (!result.isValid()) {
            throw new IllegalStateException(result.getErrorSummary());
        }
        return result.dFields;
    }

    public ParseResult readSchema(InputStream is) throws Exception {
        ParseResult result = this.parseSchema(is);
        if (!result.isValid()) {
            throw new IllegalStateException(result.getErrorSummary());
        }
        return result;
    }

    public ParseResult parseSchema(InputStream is) throws Exception {
        return parseSchema(is, true);
    }

    public ParseResult parseSchema(InputStream is, boolean shallValidate) throws Exception {
        DocumentBuilderFactory schemaDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        // 只是读取schema不作校验
        schemaDocumentBuilderFactory.setValidating(shallValidate);
        // schemaDocumentBuilderFactory.setSchema(schema)
        final ParseResult result = new ParseResult(shallValidate);
        DocumentBuilder builder = schemaDocumentBuilderFactory.newDocumentBuilder();
        InputSource input = new InputSource(is);
        if (!shallValidate) {
            builder.setEntityResolver(new EntityResolver() {

                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    InputSource source = new InputSource();
                    source.setCharacterStream(new StringReader(""));
                    return source;
                }
            });
        } else {
            final DefaultHandler mh = new DefaultHandler() {

                public void error(SAXParseException e) throws SAXException {
                    result.errlist.add("line:" + e.getLineNumber() + " " + e.getMessage() + "<br/>");
                }

                public void fatalError(SAXParseException e) throws SAXException {
                    this.error(e);
                }
            };
            builder.setErrorHandler(mh);
            builder.setEntityResolver(new EntityResolver() {

                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    // TSearcherConfigFetcher.get().getTerminatorConsoleHostAddress();
                    final String tisrepository = RunEnvironment.getSysRuntime().getInnerRepositoryURL();
                    final URL url = new URL(tisrepository + "/dtd/solrschema.dtd");
                    return new InputSource(new ByteArrayInputStream(ConfigFileContext.processContent(url, new StreamProcess<byte[]>() {

                        @Override
                        public byte[] p(int status, InputStream stream, String md5) {
                            try {
                                return IOUtils.toByteArray(stream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })));
                }
            });
        }
        Document document = null;
        try {
            document = builder.parse(input);
        } catch (Throwable e) {
        }
        if (!result.isValid()) {
            return result;
        }
        return parse(document, shallValidate);
    }

    private static final Pattern SPACE = Pattern.compile("\\s+");

    public ParseResult parse(Document document, boolean shallValidate) throws XPathExpressionException {
        // .newXPath();
        final XPath xpath = createXPath();
        ParseResult parseResult = new ParseResult(shallValidate);
        final ArrayList<PSchemaField> dFields = parseResult.dFields;
        // 取得fields type
        String expression = "/schema/types/fieldType|/schema/types/fieldtype";
        NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        // Map<String, SolrType> types = new HashMap<String, SolrType>();
        String typeName = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attrs = node.getAttributes();
            typeName = DOMUtil.getAttr(attrs, "name");
            if (shallValidate && parseResult.types.containsKey(typeName)) {
                // throw new
                // IllegalStateException("can not define a data type ["
                // + typeName + "] repetitive");
                parseResult.errlist.add("can not define a data type [" + typeName + "] repetitive as the subelement of types");
                return parseResult;
            }
            parseResult.types.put(typeName, parseFieldType(typeName, DOMUtil.getAttr(attrs, "class", "class definition")));
        }
        addExtenionProcessor(parseResult, xpath, document);
        // 取得fields
        expression = "/schema/fields/field";
        ParseResult result = parseField(document, shallValidate, xpath, parseResult, dFields, expression, false);
        if (!result.isValid()) {
            return result;
        }
        expression = "/schema/fields/dynamicField";
        result = parseField(document, shallValidate, xpath, parseResult, dFields, expression, true);
        if (!result.isValid()) {
            return result;
        }
        final String uniqueKey = (String) xpath.evaluate("/schema/uniqueKey", document, XPathConstants.STRING);
        final String sharedKey = (String) xpath.evaluate("/schema/sharedKey", document, XPathConstants.STRING);
        boolean uniqueKeyDefined = false;
        boolean defaultSearchFieldDefined = false;
        final String defaultSearchField = (String) xpath.evaluate("/schema/defaultSearchField", document, XPathConstants.STRING);
        if (shallValidate) {
            for (PSchemaField f : dFields) {
                if (f.getName().equals(uniqueKey)) {
                    uniqueKeyDefined = true;
                    if (!f.isIndexed()) {
                        parseResult.errlist.add("defined uniqueKey:" + defaultSearchField + " field property 'indexed' shall be true");
                        return result;
                    }
                }
                if (f.getName().equals(defaultSearchField)) {
                    defaultSearchFieldDefined = true;
                    if (!f.isIndexed()) {
                        parseResult.errlist.add("defined defaultSearchField:" + defaultSearchField + " field property 'indexed' shall be true");
                        return result;
                    }
                }
            }
            if (!uniqueKeyDefined) {
                parseResult.errlist.add("uniqueKey have not been define in sub element of schema/fields");
                return result;
            }
            // 判断定义了defaultSearchField 但是没有在schema中找到
            if (StringUtils.isNotBlank(defaultSearchField) && !defaultSearchFieldDefined) {
                result.errlist.add("defined defaultSearchField:" + defaultSearchField + " can not be found in the fields list");
                return result;
            }
        }
        parseResult.setUniqueKey(uniqueKey);
        parseResult.setSharedKey(sharedKey);
        final NodeList schemaNodes = (NodeList) xpath.evaluate("/schema", document, XPathConstants.NODESET);
        final String indexBuilder = getIndexBuilder(schemaNodes);
        if (!StringUtils.isBlank(indexBuilder)) {
            parseResult.setIndexBuilder(indexBuilder);
        }
        // 构建全量索引doc构建工厂
        parseResult.setDocumentCreatorType(getDocMaker(schemaNodes));
        return parseResult;
    }

    private static final Pattern PATTERN_COMMENT_PROCESSOR = Pattern.compile("^\\{(\\w+?) (.*)\\}$");

    private static final Pattern PATTERN_INDEX_BUILD_HOOK = Pattern.compile("^\\{\\s*buildhook (.*)\\}$");

    private void addExtenionProcessor(ParseResult parseResult, XPath xPath, Document document) throws XPathExpressionException {
        Node fieldsNode = (Node) xPath.evaluate("/schema/fields", document, XPathConstants.NODE);
        NodeList nodes = fieldsNode.getChildNodes();
        aa: for (int i = 0; i < nodes.getLength(); i++) {
            Node fieldNode = nodes.item(i);
            String comment = fieldNode.getTextContent();
            if (fieldNode.getNodeType() == Node.COMMENT_NODE && !StringUtils.isBlank(comment)) {
                Matcher matcher = PATTERN_INDEX_BUILD_HOOK.matcher(comment);
                if (matcher.find() && matcher.groupCount() == 1) {
                    parseResult.addIndexBuildHook(IndexBuildHook.create(matcher.group(1)));
                    continue aa;
                }
                matcher = PATTERN_COMMENT_PROCESSOR.matcher(comment);
                if (matcher.find() && matcher.groupCount() == 2) {
                    parseResult.addProcessorSchema(ProcessorSchemaField.create(matcher.group(1), matcher.group(2)));
                }
            }
        }
    }

    private String getIndexBuilder(NodeList nodes) {
        return getFirstNodeAtt(nodes, "indexBuilder");
    }

    /**
     * 取得docMaker实现类
     *
     * @param nodes
     * @return
     */
    private String getDocMaker(NodeList nodes) {
        return getFirstNodeAtt(nodes, "docMaker");
    }

    private String getFirstNodeAtt(NodeList nodes, String attriName) {
        String indexBuilder = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = DOMUtil.getAttr(node.getAttributes(), attriName, null);
            if (!StringUtils.isBlank(name)) {
                indexBuilder = name;
                break;
            }
        }
        return indexBuilder;
    }

    private ParseResult parseField(Document document, boolean shallValidate, final XPath xpath, ParseResult parseResult, final ArrayList<PSchemaField> dFields, String expression, boolean dynamicField) throws XPathExpressionException {
        NodeList nodes;
        nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        PSchemaField field = null;
        Matcher matcher = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            field = new PSchemaField();
            field.setDynamic(dynamicField);
            Node node = nodes.item(i);
            NamedNodeMap attrs = node.getAttributes();
            String name = DOMUtil.getAttr(attrs, "name", "field definition");
            matcher = SPACE.matcher(name);
            if (shallValidate && matcher.find()) {
                parseResult.errlist.add("name:" + name + " can not contain space word");
                return parseResult;
            }
            field.setName(name);
            String typeKey = DOMUtil.getAttr(attrs, "type", "field " + name);
            SolrType type = parseResult.types.get(typeKey);
            // + " is not define in fieldtypes", type);
            if (shallValidate && type == null) {
                parseResult.errlist.add("typeKey:" + typeKey + " is not define in fieldtypes");
                return parseResult;
            }
            field.setType(type);
            Map<String, String> args = DOMUtil.toMapExcept(attrs, "name", "type");
            if (args.get("required") != null) {
                field.setRequired(Boolean.valueOf(args.get("required")));
            }
            if (args.get("indexed") != null) {
                field.setIndexed(Boolean.valueOf(args.get("indexed")));
            }
            if (args.get("stored") != null) {
                field.setStored(Boolean.valueOf(args.get("stored")));
            }
            if (args.get("multiValued") != null) {
                field.setMltiValued(Boolean.valueOf(args.get("multiValued")));
            }
            if (args.get("docValues") != null) {
                field.setDocValue(Boolean.valueOf(args.get("docValues")));
            }
            if (args.get("useDocValuesAsStored") != null) {
                field.setUseDocValuesAsStored(Boolean.valueOf(args.get("useDocValuesAsStored")));
            }
            if (args.get("default") != null) {
                field.setDefaultValue(args.get("default"));
            }
            dFields.add(field);
        }
        return parseResult;
    }

    /**
     * 解析字段类型
     *
     * @param fieldType
     * @return
     */
    private SolrType parseFieldType(String name, String fieldType) {
        SolrType type = new SolrType();
        Type t = new Type(name);
        t.setSolrType(fieldType);
        type.setSolrType(t);
        if (isTypeMatch(fieldType, "int")) {
            type.setJavaType(Integer.class);
            return type;
        } else if (isTypeMatch(fieldType, "float")) {
            type.setJavaType(Float.class);
            return type;
        } else if (isTypeMatch(fieldType, "double")) {
            type.setJavaType(Double.class);
            return type;
        } else if (isTypeMatch(fieldType, "long")) {
            type.setJavaType(Long.class);
            return type;
        } else if (isTypeMatch(fieldType, "short")) {
            type.setJavaType(Short.class);
            return type;
        }
        type.setJavaType(String.class);
        return type;
    }

    public static class Type {

        private String solrType;

        private final String name;

        public String getName() {
            return name;
        }

        public Type(String name) {
            super();
            this.name = name;
        }

        public String getSolrType() {
            return solrType;
        }

        public void setSolrType(String solrType) {
            this.solrType = solrType;
        }
    }

    public static class SolrType {

        private Class<?> javaType;

        private Type solrType;

        private Method valueof;

        public Class<?> getJavaType() {
            return javaType;
        }

        public Object valueOf(Object val) throws Exception {
            return valueof.invoke(null, val);
        }

        public void setJavaType(Class<?> javaType) {
            try {
                if (javaType == String.class) {
                    valueof = javaType.getMethod("valueOf", Object.class);
                } else {
                    valueof = javaType.getMethod("valueOf", String.class);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.javaType = javaType;
        }

        public String getSolrType() {
            return solrType.getSolrType();
        }

        public Type getSType() {
            return solrType;
        }

        public void setSolrType(Type solrType) {
            this.solrType = solrType;
        }
    }

    private static boolean isTypeMatch(String fieldType, String matchLetter) {
        return StringUtils.indexOfAny(fieldType, new String[] { matchLetter, StringUtils.capitalize(matchLetter) }) > -1;
    }

    public static class SchemaFields extends ArrayList<PSchemaField> {

        private static final long serialVersionUID = 1L;

        private final Map<String, PSchemaField> fieldMap = new HashMap<String, PSchemaField>();

        @Override
        public boolean add(PSchemaField e) {
            fieldMap.put(e.getName(), e);
            return super.add(e);
        }

        public PSchemaField getField(String fieldName) {
            return fieldMap.get(fieldName);
        }
    }

    private static final Set<String> reserved_words = new HashSet<String>();

    static {
        reserved_words.add("_val_");
        reserved_words.add("fq");
        reserved_words.add("docId");
        reserved_words.add("score");
        reserved_words.add("q");
        reserved_words.add("boost");
    }

    public static class ParseResult {

        public SchemaFields dFields = new SchemaFields();

        private final Map<String, SolrType> types = new HashMap<String, SolrType>();

        private String uniqueKey;

        private String sharedKey;

        private final boolean shallValidate;

        private final List<ProcessorSchemaField> processorSchemas = new LinkedList<ProcessorSchemaField>();

        private final List<IndexBuildHook> indexBuildHooks = new LinkedList<IndexBuildHook>();

        // 索引构建的实现类
        private String indexBuilderClass;

        // 对应接口的实现类
        // = "default";
        private String documentCreatorType;

        public List<ProcessorSchemaField> getProcessorSchemas() {
            return Collections.unmodifiableList(processorSchemas);
        }

        public void addIndexBuildHook(IndexBuildHook indexBuildHook) {
            this.indexBuildHooks.add(indexBuildHook);
        }

        public List<IndexBuildHook> getIndexBuildHooks() {
            return Collections.unmodifiableList(indexBuildHooks);
        }

        public void addProcessorSchema(ProcessorSchemaField processorSchema) {
            processorSchemas.add(processorSchema);
        }

        public ParseResult(boolean shallValidate) {
            this.shallValidate = shallValidate;
        }

        public List<String> errlist = new ArrayList<String>();

        public Collection<SolrType> getFieldTypes() {
            return types.values();
        }

        public Collection<String> getFieldTypesKey() {
            return types.keySet();
        }

        public String getErrorSummary() {
            StringBuffer summary = new StringBuffer();
            for (String err : errlist) {
                summary.append(err);
                summary.append("\n");
            }
            return summary.toString();
        }

        public boolean isValid() {
            if (!this.errlist.isEmpty()) {
                return false;
            }
            if (!shallValidate) {
                return true;
            }
            for (PSchemaField field : dFields) {
                if (!field.isIndexed() && !field.isStored() && !field.isDocValue()) {
                    errlist.add("filed:" + field.getName() + "neither either 'stored' or attribute 'indexed' or attribute 'docvalue' shall be true ");
                }
                String fieldName = StringUtils.lowerCase(field.getName());
                if (reserved_words.contains(fieldName)) {
                    errlist.add("field:" + field.getName() + " can not named as solr reserved words");
                }
            // <field name="gmtCreate" type="tlong" indexed="true"
            // stored="true" multiValued="false"/>
            // 
            // <field name="gmtModified" type="tlong"
            // if ((fieldName.indexOf("date") > -1
            // || fieldName.indexOf("time") > -1
            // || fieldName.indexOf("gmtcreate") > -1 || fieldName
            // .indexOf("gmtmodified") > -1)
            // && (field.getType().getJavaType() != String.class)) {
            // 
            // if (StringUtils.indexOf(field.getType().getSolrType(),
            // "Trie") < 0) {
            // errlist
            // .add("field:"
            // + field.getName()
            // + " shall be a type which the prefix must be 'Trie'");
            // }
            // }
            }
            return errlist.isEmpty();
        }

        public String getUniqueKey() {
            return uniqueKey;
        }

        public void setUniqueKey(String uniqueKey) {
            this.uniqueKey = uniqueKey;
        }

        /**
         * @return the sharedkey
         */
        public String getSharedKey() {
            return sharedKey;
        }

        /**
         * @param sharedkey
         *            the sharedkey to set
         */
        public void setSharedKey(String sharedkey) {
            this.sharedKey = sharedkey;
        }

        public String getIndexBuilder() {
            return indexBuilderClass;
        }

        public void setIndexBuilder(String indexBuilder) {
            this.indexBuilderClass = indexBuilder;
        }

        public String getDocumentCreatorType() {
            return this.documentCreatorType;
        }

        public void setDocumentCreatorType(String documentCreatorType) {
            // documentCreatorType;
            this.documentCreatorType = StringUtils.defaultIfBlank(documentCreatorType, "default");
        }
    }
}
