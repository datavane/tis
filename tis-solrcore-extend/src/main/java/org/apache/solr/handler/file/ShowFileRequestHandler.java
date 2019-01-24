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
package org.apache.solr.handler.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.RawResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import com.qlangtech.tis.solrextend.cloud.TisSolrResourceLoader;

/*
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
            File config = new File(SolrResourceLoader.locateSolrHome().toFile(), "configsets" + File.separator + collection + File.separator + TisSolrResourceLoader.CONFIG_FILE_NAME);
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
