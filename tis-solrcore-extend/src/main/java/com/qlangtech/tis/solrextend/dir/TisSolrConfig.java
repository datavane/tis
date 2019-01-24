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
package com.qlangtech.tis.solrextend.dir;

import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrConfig extends SolrConfig {

    private CoreDescriptor coreDesc;

    public TisSolrConfig(SolrResourceLoader loader, CoreDescriptor cd, InputSource is) throws ParserConfigurationException, IOException, SAXException {
        super(loader, SolrConfig.DEFAULT_CONF_FILE, is);
        this.coreDesc = cd;
    }

    @Override
    @SuppressWarnings("all")
    public List<PluginInfo> readPluginInfos(String tag, boolean requireName, boolean requireClass) {
        List<PluginInfo> plugins = super.readPluginInfos(tag, requireName, requireClass);
        for (PluginInfo info : plugins) {
        // info.initArgs.add(TIS_CORE_DESC, coreDesc);
        }
        return plugins;
    }
}
