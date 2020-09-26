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
package com.qlangtech.tis.solrextend.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.*;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.nio.file.Path;
import java.util.Properties;

/**
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
        cores = new TisCoreContainer(nodeConfig, true);
        cores.load();
        return cores;
    }

    public static NodeConfig loadNodeConfig(Path solrHome, Properties nodeProperties) {
        SolrResourceLoader loader = new SolrResourceLoader(solrHome);
        if (!StringUtils.isEmpty(System.getProperty("solr.solrxml.location"))) {
            log.warn("Solr property solr.solrxml.location is no longer supported. " + "Will automatically load solr.xml from ZooKeeper if it exists");
        }
        return SolrXmlConfig.fromSolrHome(loader.getInstancePath(), nodeProperties);
    }
}
