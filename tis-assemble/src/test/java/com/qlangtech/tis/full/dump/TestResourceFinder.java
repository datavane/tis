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
package com.qlangtech.tis.full.dump;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月28日 下午5:44:27
 */
public class TestResourceFinder extends TestCase {

    private static final Pattern CONFIG_RES_PATTERN = Pattern.compile("/(search4.+?)/");

    public void testFind() throws Exception {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] res = resourcePatternResolver.getResources("classpath*:com.qlangtech.tis/assemble/search4*/join.xml");
        System.out.println("res.length:" + res.length);
        Matcher matcher = null;
        for (Resource r : res) {
            matcher = CONFIG_RES_PATTERN.matcher(r.getURI().getPath());
            if (matcher.find()) {
                System.out.println(matcher.group(1));
            }
        // System.out.println(r.getURI());
        // System.out.println(r.getURL());
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}
