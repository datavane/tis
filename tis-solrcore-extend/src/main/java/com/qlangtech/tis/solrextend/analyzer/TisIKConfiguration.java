/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.analyzer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.wltea.analyzer.cfg.Configuration;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年12月20日
 */
public class TisIKConfiguration implements Configuration {

    private static final String PATH_DIC_MAIN = "org/wltea/analyzer/dic/main2012.dic";

    private static final String PATH_DIC_QUANTIFIER = "org/wltea/analyzer/dic/quantifier.dic";

    private static final String FILE_NAME = "itsIKAnalyzer.cfg.xml";

    private static final String EXT_DICT = "ext_dict";

    private static final String EXT_STOP = "ext_stopwords";

    private Properties props;

    private boolean useSmart;

    public TisIKConfiguration() {
        super();
        this.props = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
        if (input != null) {
            try {
                this.props.loadFromXML(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @SuppressWarnings("all")
    public List<String> getExtDictionarys() {
        List extDictFiles = new ArrayList(2);
        String extDictCfg = this.props.getProperty(EXT_DICT);
        if (extDictCfg != null) {
            String[] filePaths = extDictCfg.split(";");
            if (filePaths != null) {
                for (String filePath : filePaths) {
                    if ((filePath != null) && (!"".equals(filePath.trim()))) {
                        extDictFiles.add(filePath.trim());
                    }
                }
            }
        }
        return extDictFiles;
    }

    @Override
    @SuppressWarnings("all")
    public List<String> getExtStopWordDictionarys() {
        List extStopWordDictFiles = new ArrayList(2);
        String extStopWordDictCfg = this.props.getProperty(EXT_STOP);
        if (extStopWordDictCfg != null) {
            String[] filePaths = extStopWordDictCfg.split(";");
            if (filePaths != null) {
                for (String filePath : filePaths) {
                    if ((filePath != null) && (!"".equals(filePath.trim()))) {
                        extStopWordDictFiles.add(filePath.trim());
                    }
                }
            }
        }
        return extStopWordDictFiles;
    }

    @Override
    public String getMainDictionary() {
        return PATH_DIC_MAIN;
    }

    @Override
    public String getQuantifierDicionary() {
        return PATH_DIC_QUANTIFIER;
    }

    @Override
    public void setUseSmart(boolean smart) {
        this.useSmart = smart;
    }

    @Override
    public boolean useSmart() {
        return this.useSmart;
    }
}
