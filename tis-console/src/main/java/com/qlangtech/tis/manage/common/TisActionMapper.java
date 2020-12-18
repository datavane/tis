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
package com.qlangtech.tis.manage.common;

import com.opensymphony.xwork2.config.ConfigurationManager;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TisActionMapper extends DefaultActionMapper {

  public static final String REQUEST_EXTENDSION_AJAX = "ajax";
  public static final String ACTION_TOKEN = "#action";

  protected void parseNameAndNamespace(String uri, ActionMapping mapping, ConfigurationManager configManager) {
    super.parseNameAndNamespace(uri, mapping, configManager);
    // StringBuffer parsedName = new StringBuffer();
    // char[] nameAry = mapping.getName().toCharArray();
    // for (int i = 0; i < nameAry.length; i++) {
    // if (Character.isUpperCase(nameAry[i])) {
    // parsedName.append('_')
    // .append(Character.toLowerCase(nameAry[i]));
    // } else {
    // parsedName.append(nameAry[i]);
    // // .append(Character.toLowerCase());
    // }
    // }
    // mapping.setMethod(BasicModule.parseMehtodName());
    mapping.setName(addUnderline(mapping.getName()).toString());
    mapping.setNamespace(mapping.getNamespace() + "#screen");
  }

  public static StringBuffer addUnderline(String value) {
    StringBuffer parsedName = new StringBuffer();
    char[] nameAry = value.toCharArray();
    boolean firstAppend = true;
    for (int i = 0; i < nameAry.length; i++) {
      if (Character.isUpperCase(nameAry[i])) {
        if (firstAppend) {
          parsedName.append(Character.toLowerCase(nameAry[i]));
          firstAppend = false;
        } else {
          parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
        }
      } else {
        parsedName.append(nameAry[i]);
        firstAppend = false;
        // .append(Character.toLowerCase());
      }
    }
    return parsedName;
  }

  @Override
  public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {
    ActionMapping mapping = super.getMapping(request, configManager);
    String action = null;
    if (StringUtils.isNotEmpty(action = request.getParameter("action"))) {
      mapping.setName(action);
      mapping.setNamespace(StringUtils.split(mapping.getNamespace(), "#")[0] + ACTION_TOKEN);
    }
    return mapping;
  }
}
