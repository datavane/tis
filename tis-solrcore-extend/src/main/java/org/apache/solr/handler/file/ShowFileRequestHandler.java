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
package org.apache.solr.handler.file;

import com.qlangtech.tis.solrextend.cloud.TisSolrResourceLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.core.SolrPaths;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.RawResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 显示当前core使用的,reference from:<br>
 * org.apache.solr.handler.admin.ShowFileRequestHandler
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ShowFileRequestHandler extends RequestHandlerBase {

    private static final String DESC = "TISShowFileRequestHandler";

    // private static final Logger log =
    // LoggerFactory.getLogger(ShowFileRequestHandler.class);
    @Override
    public void handleRequestBody(SolrQueryRequest request, SolrQueryResponse response) throws Exception {
        String fname = request.getParams().get("file", null);
        if (StringUtils.isBlank(fname)) {
            throw new IllegalArgumentException("param file can not be null");
        }
        if (TisSolrResourceLoader.CONFIG_FILE_NAME.equals(fname)) {
            // 取配置config.properties文件
            final String collection = request.getCore().getCoreDescriptor().getCollectionName();
            File config = new File(SolrPaths.locateSolrHome().toFile(), "configsets" + File.separator + collection + File.separator + TisSolrResourceLoader.CONFIG_FILE_NAME);
            if (!config.exists()) {
                throw new IllegalStateException("file dose not exist:" + config.getAbsolutePath());
            }
            try (InputStream fileStream = FileUtils.openInputStream(config)) {
                writeContent(request, response, fileStream);
            }
            return;
        }
        if (TisSolrResourceLoader.configFileNames.get(fname) == null) {
            throw new IllegalStateException("fname:" + fname + " can not be fetch");
        }
        try (InputStream fileStream = request.getCore().getResourceLoader().openResource(fname)) {
            writeContent(request, response, fileStream);
        }
    }

    private void writeContent(SolrQueryRequest request, SolrQueryResponse response, InputStream fileStream) throws IOException {
        ModifiableSolrParams params = new ModifiableSolrParams(request.getParams());
        params.set(CommonParams.WT, "raw");
        request.setParams(params);
        final ByteArrayInputStream starem = new ByteArrayInputStream(IOUtils.toByteArray(fileStream));
        // ContentStreamBase content = new ContentStreamBase.FileStream(adminFile);
        // content.setContentType(request.getParams().get(USE_CONTENT_TYPE));
        response.add(RawResponseWriter.CONTENT, new ContentStreamBase() {

            @Override
            public InputStream getStream() throws IOException {
                return starem;
            }

            @Override
            public String getContentType() {
                return "application/xml";
            }
        });
    }

    @Override
    public String getDescription() {
        return DESC;
    }
}
