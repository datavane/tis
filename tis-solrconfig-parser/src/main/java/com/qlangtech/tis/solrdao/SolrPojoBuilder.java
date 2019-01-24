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

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SolrPojoBuilder {

    final IBuilderContext buildContext;

    public SolrPojoBuilder(IBuilderContext buildContext) {
        super();
        this.buildContext = buildContext;
    }

    public void create() throws Exception {
        SolrFieldsParser parser = new SolrFieldsParser();
        PrintWriter writer = null;
        InputStream reader = null;
        try {
            writer = new PrintWriter((buildContext.getOutputStream()));
            reader = buildContext.getResourceInputStream();
            ArrayList<PSchemaField> fields = parser.readSchemaFields(reader);
            writer.print("package ");
            writer.println(this.buildContext.getTargetNameSpace() + ";");
            writer.println();
            writer.println();
            if (SolrFieldsParser.hasMultiValuedField(fields)) {
                writer.println("import java.util.List;");
            }
            writer.println("import org.apache.solr.client.solrj.beans.Field;");
            writer.println();
            writer.println("public class " + this.buildContext.getPojoName() + "{");
            for (PSchemaField f : fields) {
                if (f.isStored()) {
                    writer.println("\t@Field(\"" + f.getName() + "\")");
                    writer.println("\tprivate " + f.getFileTypeLiteria() + " " + f.getPropertyName() + ";");
                }
                writer.println();
            // System.out.println("name:" + f.getName() + ", type:"
            // + f.getPropertyName() + ", get:"
            // + f.getGetterMethodName() + " set:"
            // + f.getSetMethodName());
            }
            // 生成get set方法
            for (PSchemaField f : fields) {
                // setter mehod
                if (!f.isStored()) {
                    continue;
                }
                writer.println("\tpublic void " + f.getSetMethodName() + "(" + f.getFileTypeLiteria() + " " + f.getPropertyName() + "){");
                writer.println("\t   this." + f.getPropertyName() + " = " + f.getPropertyName() + ";");
                writer.println("\t}");
                writer.println();
                // getter mehod
                writer.println("\tpublic " + f.getFileTypeLiteria() + " " + f.getGetterMethodName() + "(){");
                writer.println("\t   return this." + f.getPropertyName() + ";");
                writer.println("\t}");
                writer.println();
            // writer.println("\t@Field(\"" + f.getName() + "\")");
            // writer.println("\tprivate " + f.getFileTypeLiteria() +
            // " "
            // + f.getPropertyName() + ";");
            // writer.println();
            // System.out.println("name:" + f.getName() + ", type:"
            // + f.getPropertyName() + ", get:"
            // + f.getGetterMethodName() + " set:"
            // + f.getSetMethodName());
            }
            writer.println("}");
            // File targetFile = getNewFileName();
            System.out.println(" successful create new pojo file  ");
        } finally {
            IOUtils.closeQuietly(reader);
            writer.flush();
            buildContext.closeWriter(writer);
        }
    }

    private static class Assert {

        private static void assertNotNull(String msg, Object o) {
            if (o == null) {
                throw new NullPointerException(msg);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_address");
        String indexName = System.getProperty("index_name");
        String pojoName = System.getProperty("pojo_name");
        String pkg = System.getProperty("pkg");
        String outdir = System.getProperty("outdir");
        Assert.assertNotNull("serverAddress", serverAddress);
        Assert.assertNotNull("indexName", indexName);
        Assert.assertNotNull("pojoName", pojoName);
        Assert.assertNotNull("pkg", pkg);
        Assert.assertNotNull("outdir", outdir);
        BuilderContext buildContext = new BuilderContext();
        buildContext.setServerAddress(serverAddress);
        buildContext.setAppName(indexName);
        buildContext.setPojoName(pojoName);
        buildContext.setTargetNameSpace(pkg);
        buildContext.setTargetDir(outdir);
        SolrPojoBuilder builder = new SolrPojoBuilder(buildContext);
        builder.create();
    }
}
