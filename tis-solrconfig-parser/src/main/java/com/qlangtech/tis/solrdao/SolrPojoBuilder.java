/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.solrdao;

import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-6-7
 */
public class SolrPojoBuilder {

    final IBuilderContext buildContext;

    public SolrPojoBuilder(IBuilderContext buildContext) {
        super();
        this.buildContext = buildContext;
    }

    public void create() throws Exception {
        PrintWriter writer = null;
        InputStream reader = null;
        try {
            writer = new PrintWriter((buildContext.getOutputStream()));
            IIndexMetaData metaData = SolrFieldsParser.parse(() -> buildContext.getResourceInputStream());
            // parser.readSchemaFields(reader);
            ArrayList<PSchemaField> fields = metaData.getSchemaParseResult().dFields;
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
