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
