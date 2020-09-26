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
package com.qlangtech.tis.maven.plugins.tpi;

import org.codehaus.plexus.util.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 TODO this is duplicated from the resources plugin-migrate to plexus-utils
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public final class PropertyUtils {

    private PropertyUtils() {
    // prevent instantiation
    }

    /**
     * Reads a property file, resolving all internal variables.
     *
     * @param propfile       The property file to load
     * @param fail           whether to throw an exception when the file cannot be loaded or to return null
     * @param useSystemProps whether to incorporate System.getProperties settings into the returned Properties object.
     * @return the loaded and fully resolved Properties object
     */
    public static Properties loadPropertyFile(File propfile, boolean fail, boolean useSystemProps) throws IOException {
        Properties props = new Properties();
        if (useSystemProps) {
            props = new Properties(System.getProperties());
        }
        if (propfile.exists()) {
            FileInputStream inStream = new FileInputStream(propfile);
            try {
                props.load(inStream);
            } finally {
                IOUtil.close(inStream);
            }
        } else if (fail) {
            throw new FileNotFoundException(propfile.toString());
        }
        for (Enumeration n = props.propertyNames(); n.hasMoreElements(); ) {
            String k = (String) n.nextElement();
            props.setProperty(k, PropertyUtils.getPropertyValue(k, props));
        }
        return props;
    }

    /**
     * Retrieves a property value, replacing values like ${token}
     * using the Properties to look them up.
     * <p>
     * It will leave unresolved properties alone, trying for System
     * properties, and implements reparsing (in the case that
     * the value of a property contains a key), and will
     * not loop endlessly on a pair like
     * test = ${test}.
     */
    private static String getPropertyValue(String k, Properties p) {
        // This can also be done using InterpolationFilterReader,
        // but it requires reparsing the file over and over until
        // it doesn't change.
        String v = p.getProperty(k);
        String ret = "";
        int idx, idx2;
        while ((idx = v.indexOf("${")) >= 0) {
            // append prefix to result
            ret += v.substring(0, idx);
            // strip prefix from original
            v = v.substring(idx + 2);
            // if no matching } then bail
            if ((idx2 = v.indexOf('}')) < 0) {
                break;
            }
            // strip out the key and resolve it
            // resolve the key/value for the ${statement}
            String nk = v.substring(0, idx2);
            v = v.substring(idx2 + 1);
            String nv = p.getProperty(nk);
            // try global environment..
            if (nv == null) {
                nv = System.getProperty(nk);
            }
            // taking recursion into account.
            if (nv == null || nv.equals(k)) {
                ret += "${" + nk + "}";
            } else {
                v = nv + v;
            }
        }
        return ret + v;
    }
}
