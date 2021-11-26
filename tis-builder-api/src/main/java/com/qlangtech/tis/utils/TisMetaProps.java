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

package com.qlangtech.tis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-11-25 20:30
 **/
public class TisMetaProps {
    public Properties tisMetaProps;

    private static TisMetaProps instance;

    public static TisMetaProps getInstance() {
        if (instance == null) {
            synchronized (TisMetaProps.class) {
                if (instance == null) {
                    try {
                        try (InputStream reader = TisMetaProps.class.getResourceAsStream("/tis-meta")) {
                            Properties p = new Properties();
                            p.load(reader);
                            instance = new TisMetaProps(p);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }


    private TisMetaProps(Properties props) {
        this.tisMetaProps = props;
    }

    public String getVersion() {
        return this.tisMetaProps.getProperty("buildVersion");
    }
}
