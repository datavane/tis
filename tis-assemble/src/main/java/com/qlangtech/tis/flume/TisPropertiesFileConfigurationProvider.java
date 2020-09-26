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
package com.qlangtech.tis.flume;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.flume.conf.FlumeConfiguration;
import org.apache.flume.node.AbstractConfigurationProvider;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月18日
 */
public class TisPropertiesFileConfigurationProvider extends AbstractConfigurationProvider {

    /**
     * @param agentName
     */
    public TisPropertiesFileConfigurationProvider(String agentName) {
        super(agentName);
    }

    @Override
    protected FlumeConfiguration getFlumeConfiguration() {
        InputStream reader = null;
        try {
            reader = this.getClass().getResourceAsStream("/flume.conf");
            Properties properties = new Properties();
            properties.load(reader);
            return new FlumeConfiguration(toMap(properties));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }
    // return new FlumeConfiguration(new HashMap<String, String>());
    }
}
