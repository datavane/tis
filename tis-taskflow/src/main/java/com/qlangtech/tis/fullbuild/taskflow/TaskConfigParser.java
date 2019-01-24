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
package com.qlangtech.tis.fullbuild.taskflow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.qlangtech.tis.dump.hive.HiveColumn;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.taskflow.hive.HiveInsertFromSelectParser;
import com.qlangtech.tis.fullbuild.taskflow.hive.HiveTaskFactory;
import com.qlangtech.tis.fullbuild.taskflow.hive.JoinHiveTask;
import com.qlangtech.tis.fullbuild.taskflow.hive.UnionHiveTask;
import com.qlangtech.tis.git.GitUtils;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskConfigParser {

    public static final DocumentBuilderFactory schemaDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

    static final XPathFactory xpathFactory = XPathFactory.newInstance();

    private static final Logger log = LoggerFactory.getLogger(TaskConfigParser.class);

    static {
        schemaDocumentBuilderFactory.setValidating(false);
    }

    // 总共保留多少个历史partition
    private int partitionSaveCount = 4;

    private final HiveTaskFactory taskFactory;

    // private ITaskFactory taskFactory;
    // public void setTaskFactory(ITaskFactory taskFactory) {
    // this.taskFactory = taskFactory;
    // }
    private TemplateContext tplContext;

    /**
     * @param hiveTaskFactory
     */
    private TaskConfigParser(HiveTaskFactory hiveTaskFactory) {
        super();
        this.taskFactory = hiveTaskFactory;
    }

    public static TaskConfigParser getInstance() {
        return new TaskConfigParser(new HiveTaskFactory());
    }

    /**
     * @param indexname
     * @param execContext
     * @throws Exception
     * @throws FileNotFoundException
     */
    public // importConfigFileStream,
    void startJoinSubTables(// importConfigFileStream,
    String indexname, IExecChainContext execContext) throws Exception, FileNotFoundException {
        InputStream importConfigFileStream = null;
        TemplateContext tplContext = new TemplateContext(execContext);
        this.setTplContext(tplContext);
        HiveTaskFactory.startTaskInitialize(tplContext);
        try {
            importConfigFileStream = getJoinRuleStream(indexname);
            List<ITask> tasklist = this.parseTask(importConfigFileStream);
            for (ITask task : tasklist) {
                task.exexute();
            }
        } finally {
            this.taskFactory.postReleaseTask(tplContext);
            IOUtils.closeQuietly(importConfigFileStream);
        }
    }

    private static final String encode = "utf8";

    /**
     * http://git.2dfire-inc.com/dfire-searcher/tis-fullbuild-workflow/blob/
     * master/mars/hive/search4totalpay/join.xml
     *
     * @return
     */
    private InputStream getJoinRuleStream(String index) {
        if (StringUtils.isEmpty(index)) {
            throw new IllegalArgumentException("param index can not be null");
        }
        Charset charsetUTF8 = Charset.forName(encode);
        TSearcherConfigFetcher tisConfig = TSearcherConfigFetcher.get();
        // RunEnvironment.getEnum(tisConfig.getRunEnvironment());
        RunEnvironment runtime = tisConfig.getRuntime();
        InputStream input = null;
        boolean httpOK = false;
        String dataDir = System.getProperty("data.dir");
        if (StringUtils.isEmpty(dataDir)) {
            throw new IllegalStateException("sys prop 'data.dir' is null");
        }
        File joinFile = new File(new File(dataDir), "git" + File.separator + GitUtils.getHiveJoinPath(index));
        try {
            input = GitUtils.$().getHiveJoinTaskConfig(index, runtime);
            httpOK = true;
            byte[] content = IOUtils.toByteArray(input);
            final String md5 = DigestUtils.md5Hex(content);
            String first = null;
            if (joinFile.exists()) {
                LineIterator it = FileUtils.lineIterator(joinFile, encode);
                if (it.hasNext()) {
                    first = it.nextLine();
                }
                LineIterator.closeQuietly(it);
            }
            if (!StringUtils.equals(md5, first)) {
                FileOutputStream output = FileUtils.openOutputStream(joinFile, false);
                IOUtils.writeLines(Collections.singletonList(md5), null, /* lineEnding */
                output, charsetUTF8);
                IOUtils.write(content, output);
                output.flush();
                IOUtils.closeQuietly(output);
            }
            return new ByteArrayInputStream(content);
        } catch (Throwable e) {
            if (!httpOK && joinFile.exists()) {
                log.warn(e.getMessage());
                FileInputStream localFile = null;
                try {
                    localFile = FileUtils.openInputStream(joinFile);
                    LineIterator it = IOUtils.lineIterator(localFile, charsetUTF8);
                    it.next();
                    StringBuffer content = new StringBuffer();
                    while (it.hasNext()) {
                        content.append(it.nextLine()).append("\n");
                    }
                    log.info("start join xml content----------------------------");
                    log.info(content.toString());
                    log.info("end  ---------------------------------------------");
                    // new StringReader(content.toString());
                    return IOUtils.toInputStream(content.toString(), "utf8");
                // return localFile;
                } catch (IOException e1) {
                    throw new RuntimeException("joinFile:" + joinFile.getAbsolutePath(), e1);
                } finally {
                    IOUtils.closeQuietly(localFile);
                }
            } else {
                throw new RuntimeException(joinFile.getAbsolutePath(), e);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    // String path = "com/dfire/tis/assemble/" + index + "/join.xml";
    // InputStream stream =
    // this.getClass().getClassLoader().getResourceAsStream(path);
    // if (stream == null) {
    // throw new IllegalStateException("path:" + path + " relevant stream can not be
    // null");
    // }
    // return stream;
    }

    // private static Map<String/* indexName */, HiveInsertFromSelectParser>
    // sqlASTCache = new HashMap<String, HiveInsertFromSelectParser>();
    public static HiveInsertFromSelectParser getLastJoinTaskSQLAST(IExecChainContext execContext) {
        try {
            // HiveInsertFromSelectParser ast = sqlASTCache.get(indexName);
            // if (ast == null) {
            HiveTaskFactory hiveTaskFactory = new HiveTaskFactory();
            TaskConfigParser taskConfigParser = new TaskConfigParser(hiveTaskFactory);
            JoinHiveTask joinTask = taskConfigParser.getLastJoinTask(execContext.getIndexName());
            return joinTask.getSQLParserResult(new TemplateContext(execContext));
        // sqlASTCache.put(indexName, ast);
        // }
        // return ast;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得最后一个join task，理论上就应该是，宽表sql
     *
     * @param indexName
     * @return
     * @throws Exception
     */
    private JoinHiveTask getLastJoinTask(String indexName) throws Exception {
        // InputStream is = null;
        final AtomicReference<JoinHiveTask> joinTask = new AtomicReference<JoinHiveTask>();
        // try {
        // is = getJoinRuleStream(indexName);
        // 
        // List<ITask> tasks = parseTask(is);
        // for (ITask task : tasks) {
        // 
        // }
        // } finally {
        // IOUtils.closeQuietly(is);
        // }
        // return joinTask;
        traverseTask(indexName, new ProcessTask() {

            public void process(ITask task) {
                if (task instanceof JoinHiveTask) {
                    // joinTask = (JoinHiveTask) task;
                    joinTask.set((JoinHiveTask) task);
                }
            }
        });
        return joinTask.get();
    }

    public UnionHiveTask getUnionHiveTask(String indexName) throws Exception {
        final AtomicReference<UnionHiveTask> unionTask = new AtomicReference<>();
        traverseTask(indexName, task -> {
            if (task instanceof UnionHiveTask) {
                unionTask.set((UnionHiveTask) task);
            }
        });
        return unionTask.get();
    }

    public void traverseTask(String indexName, ProcessTask taskProcess) throws Exception {
        InputStream is = null;
        // JoinHiveTask joinTask = null;
        try {
            is = getJoinRuleStream(indexName);
            List<ITask> tasks = parseTask(is);
            for (ITask task : tasks) {
                taskProcess.process(task);
            // if (task instanceof JoinHiveTask) {
            // joinTask = (JoinHiveTask) task;
            // }
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static interface ProcessTask {

        public abstract void process(ITask task);
    }

    private List<ITask> parseTask(InputStream is) throws Exception {
        DocumentBuilder builder = schemaDocumentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                InputSource source = new InputSource();
                source.setCharacterStream(new StringReader(""));
                return source;
            }
        });
        Document document = builder.parse(is);
        final XPath xpath = xpathFactory.newXPath();
        setPartitionSaveCount(document, xpath);
        final String expression = "/execute/task|/execute/currentTaks|/execute/forTask|/execute/delhistorypartition|/execute/joinTask|/execute/unionTask";
        NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        return parse(nodes);
    }

    /**
     * @param document
     * @param xpath
     * @throws XPathExpressionException
     */
    private void setPartitionSaveCount(Document document, final XPath xpath) throws XPathExpressionException {
        Node execNode = (Node) xpath.evaluate("/execute", document, XPathConstants.NODE);
        String partitionSaveCount = getAttr(execNode, "partitionSaveCount", null, true);
        if (StringUtils.isNotBlank(partitionSaveCount)) {
            this.partitionSaveCount = Integer.parseInt(partitionSaveCount);
        }
    }

    // private static final Pattern SPACE = Pattern.compile("\\s+");
    public List<ITask> parse(NodeList nodes) throws Exception {
        List<ITask> tasks = new ArrayList<ITask>();
        ITask task = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ("task".equals(node.getNodeName())) {
                task = createTask(node);
                setTaskName(node, (AdapterTask) task);
                tasks.add(task);
                continue;
            }
            if ("delhistorypartition".equals(node.getNodeName())) {
                // tasks.add(task);
                continue;
            }
            Concurrent concurrent = null;
            if ("currentTaks".equals(node.getNodeName())) {
                concurrent = new Concurrent();
                concurrent.addTask(parse(node.getChildNodes()));
                setTaskName(node, concurrent);
                tasks.add(concurrent);
                continue;
            }
            ForTask forTask = null;
            if ("forTask".equals(node.getNodeName())) {
                int start = Integer.parseInt(getAttr(node, "from", "shall set from"));
                int to = Integer.parseInt(getAttr(node, "to", "shall set from"));
                forTask = new ForTask(start, to, this.createTask(node));
                setTaskName(node, forTask);
                tasks.add(forTask);
                continue;
            }
            if ("joinTask".equals(node.getNodeName())) {
                task = taskFactory.createJoinTask(node.getTextContent(), this.tplContext);
                setTaskName(node, (AdapterTask) task);
                tasks.add(task);
                continue;
            }
            if ("unionTask".equals(node.getNodeName())) {
                task = taskFactory.createUnionTask(node, this.tplContext);
                setTaskName(node, (AdapterTask) task);
                tasks.add(task);
                continue;
            }
        // throw new IllegalStateException("nodename:" + node.getNodeName()
        // + " is not valid");
        }
        return tasks;
    }

    /**
     * @param node
     * @param task
     */
    private void setTaskName(Node node, AdapterTask task) {
        task.setName(getAttr(node, "name", "name", true));
    }

    public static String getAttr(Node node, String name, String missing_err) {
        return getAttr(node, name, missing_err, false);
    }

    public static String getAttr(Node node, String name, String missing_err, boolean ignoreNull) {
        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs == null ? null : attrs.getNamedItem(name);
        if (!ignoreNull && attr == null) {
            if (missing_err == null)
                return null;
            throw new RuntimeException(missing_err + ": missing mandatory attribute '" + name + "'");
        }
        if (attr == null) {
            return null;
        }
        return attr.getNodeValue();
    }

    /**
     * @param node
     * @return
     */
    private ITask createTask(Node node) {
        return taskFactory.createTask(node.getTextContent(), this.tplContext);
    }

    public TemplateContext getTplContext() {
        return tplContext;
    }

    public void setTplContext(TemplateContext tplContext) {
        this.tplContext = tplContext;
    }

    public static void main(String[] args) throws Exception {
        // String ps = args[0];
        // 
        // String filePath = null;
        // try {
        // filePath = args[1];
        // } catch (Throwable e) {
        // }
        // final String ps = "20151031105641";
        // File importFile = new File(
        // StringUtils
        // .defaultIfEmpty(filePath,
        // "D:\\j2ee_solution\\mvn_test\\terminator-cdp-test\\odps-sql\\create_table3.xml"));
        // String configFile =
        // "D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\dfire-order-full-dump\\src\\main\\resources\\orderinfo_join.xml";
        HiveTaskFactory factory = new HiveTaskFactory();
        TaskConfigParser parse = new TaskConfigParser(factory);
        JoinHiveTask joinTask = parse.getLastJoinTask("search4supplyGoods");
        // JoinHiveTask joinTask = parse.getLastJoinTask("search4totalpay");
        HiveInsertFromSelectParser sqlAST = joinTask.getSQLParserResult(new TemplateContext(null));
        System.out.println("start=======================================================");
        // <field name="score_item" type="double" stored="true" indexed="true"
        // />
        String blank = "                                         ";
        for (HiveColumn c : sqlAST.getCols()) {
            System.out.println("<field name=\"" + c.getName() + "\" " + StringUtils.substring(blank, 0, 20 - StringUtils.length(c.getName())) + " type=\"string\" stored=\"true\" indexed=\"false\" />");
        }
        System.out.println("end=======================================================");
        // parse.setTaskFactory(hiveTaskFactory);
        // parse.startBuildIndex(new File(configFile), ps);
        System.out.println("execute over");
    }
}
