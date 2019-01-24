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
package com.qlangtech.tis.solrextend.servlet;

import java.nio.file.Path;
import java.util.Properties;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.core.SolrXmlConfig;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrDispathFilter extends SolrDispatchFilter {

    private static final Logger log = LoggerFactory.getLogger(TisSolrDispathFilter.class);

    private static CoreContainer tisSolrCoreContainer;

    public static CoreContainer getTisSolrCoreContainer() {
        if (tisSolrCoreContainer == null) {
            throw new IllegalStateException("tisSolrCoreContainer can not be null");
        }
        return tisSolrCoreContainer;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        tisSolrCoreContainer = this.getCores();
    }

    @Override
    protected CoreContainer createCoreContainer(Path solrHome, Properties extraProperties) {
        NodeConfig nodeConfig = loadNodeConfig(solrHome, extraProperties);
        cores = new CoreContainer(nodeConfig, extraProperties, true);
        cores.load();
        return cores;
    }

    public static NodeConfig loadNodeConfig(Path solrHome, Properties nodeProperties) {
        SolrResourceLoader loader = new SolrResourceLoader(solrHome, null, nodeProperties);
        if (!StringUtils.isEmpty(System.getProperty("solr.solrxml.location"))) {
            log.warn("Solr property solr.solrxml.location is no longer supported. " + "Will automatically load solr.xml from ZooKeeper if it exists");
        }
        return SolrXmlConfig.fromSolrHome(loader, loader.getInstancePath());
    }
}
