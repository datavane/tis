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
package com.qlangtech.tis.wangjubao.jingwei;

import com.google.common.base.Joiner;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TableCluster {

    private static final Logger logger = LoggerFactory.getLogger(TableCluster.class);

    private static final Map<String, SolrFieldsParser.ParseResult> fieldsMap = new HashMap<>();

    private final SolrFieldsParser.ParseResult schemaFieldMeta;

    public TableCluster(SolrFieldsParser.ParseResult schemaFieldMeta) {
        if (schemaFieldMeta == null) {
            throw new IllegalArgumentException("schemaFieldMeta can not be null");
        }
        this.schemaFieldMeta = schemaFieldMeta;
    }

    public SolrFieldsParser.ParseResult getSchemaFieldMeta() {
        return schemaFieldMeta;
    }

    public static TableCluster getTableRowProcessor(String collectionName) throws Exception {
        if (StringUtils.isEmpty(collectionName)) {
            throw new IllegalArgumentException("argument collectionName can not be empty");
        }
        final String fieldTransferPath = "com/qlangtech/tis/realtime/transfer/" + collectionName + "/field-transfer.xml";
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fieldTransferPath)) {
            if (inputStream == null) {
                throw new IllegalStateException("classpath resource:" + fieldTransferPath + " can not be null");
            } else {
                return TableClusterParser.parse(IOUtils.toString(inputStream, TisUTF8.get()), getSchemaFields(collectionName));
            }
        }
    }

    /**
     * 取得schema中的字段
     *
     * @param collection
     * @return
     * @throws Exception
     */
    public static SolrFieldsParser.ParseResult getSchemaFields(String collection) throws Exception {
        SolrFieldsParser.ParseResult parseResult = fieldsMap.get(collection);
        if (parseResult == null) {
            synchronized (fieldsMap) {
                parseResult = fieldsMap.get(collection);
                if (parseResult == null) {
                    SnapshotDomain domain = HttpConfigFileReader.getResource(collection, RunEnvironment.getSysRuntime(), ConfigFileReader.FILE_SCHEMA);
                    IIndexMetaData meta = SolrFieldsParser.parse(() -> ConfigFileReader.FILE_SCHEMA.getContent(domain));
                    parseResult = meta.getSchemaParseResult();
                    fieldsMap.put(collection, parseResult);
                    logger.info("doc acceptKeys:" + Joiner.on(",").join(parseResult.dFieldsNames));
                // try (ByteArrayInputStream reader = new ByteArrayInputStream(ConfigFileReader.FILE_SCHEMA.getContent(domain))) {
                // parseResult = (new SolrFieldsParser()).parseSchema(reader, false);
                // 
                // return parseResult;
                // }
                }
            }
        }
        return parseResult;
    }

    private final Map<String, Table> tables = new ConcurrentHashMap<String, Table>();

    private String sharedKey;

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public Collection<Table> getTables() {
        return tables.values();
    }

    public Table getTable(String name) {
        return this.tables.get(name);
    }

    public void add(Table table) {
        this.tables.put(table.getName(), table);
    }
}
