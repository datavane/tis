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
package com.qlangtech.tis.manage.spring;

import com.qlangtech.tis.manage.common.Config;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-06 11:46
 */
public class TISWebApplicationContext extends XmlWebApplicationContext {

  private static final String RESOURCE_PREFIX_TIS_CLASSPATH = "tis:";

  @Override
  protected ResourcePatternResolver getResourcePatternResolver() {
    return new TISResourcePatternResolver(this);
  }

  private static class TISResourcePatternResolver extends ServletContextResourcePatternResolver {
    public TISResourcePatternResolver(ResourceLoader resourceLoader) {
      super(resourceLoader);
    }

    public Resource[] getResources(String locationPattern) throws IOException {
      if (StringUtils.startsWith(locationPattern, RESOURCE_PREFIX_TIS_CLASSPATH)) {
        return new Resource[]{new TISClassPathResource(StringUtils.substringAfter(locationPattern, RESOURCE_PREFIX_TIS_CLASSPATH))};
      }
      return super.getResources(locationPattern);
    }
  }

  private static class TISClassPathResource extends ClassPathResource {
    public TISClassPathResource(String path) {
      super(path);
    }

    public InputStream getInputStream() throws IOException {
      try {
        return super.getInputStream();
      } catch (IOException e) {
        Config.TestCfgStream stream = Config.openTestCfgStream();
        stream.validate();
//        if (stream.getPropsStream() == null) {
//          throw new RuntimeException("cfg props can not be find,prop file:" + stream.getPropsFile(), e);
//        }
        // 当前是测试模式
        Config.setTest(true);
        return stream.getPropsStream();
      }
    }
  }
}
