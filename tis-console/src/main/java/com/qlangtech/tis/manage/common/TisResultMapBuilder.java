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
package com.qlangtech.tis.manage.common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import org.apache.struts2.convention.ResultMapBuilder;
import org.apache.struts2.convention.annotation.Action;
import org.springframework.util.StringUtils;
import com.opensymphony.xwork2.ActionChainResult;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-6-25
 */
public class TisResultMapBuilder implements ResultMapBuilder {

    //private static final ResultConfig ACTION_RESULT_CONFIG = (new ResultConfig.Builder(BasicModule.key_FORWARD, TerminatorForwardResult.class.getName())).build();

    @Override
    public Map<String, ResultConfig> build(Class<?> actionClass, Action annotation, String actionName, PackageConfig packageConfig) {
        ResultConfig.Builder build = null;
        final String resultName = actionClass.getSimpleName();
        Map<String, ResultConfig> resultsConfig = new HashMap<String, ResultConfig>();
       // resultsConfig.put(BasicModule.key_FORWARD, ACTION_RESULT_CONFIG);
        Matcher matcher = TisPackageBasedActionConfigBuilder.NAMESPACE_PATTERN.matcher(actionClass.getName());
        if (// || (matcher = TisPackageBasedActionConfigBuilder.NAMESPACE_TIS_PATTERN.matcher(actionClass.getName())).matches()
        matcher.matches()) {
            if ("action".equals(matcher.group(2))) {
                // process ajax
                String resultCode = resultName + "_ajax";
                build = new ResultConfig.Builder(resultCode, AjaxValve.class.getName());
                resultsConfig.put(resultCode, build.build());
                // process action submit
                resultCode = resultName + "_action";
                // final String resultCode = resultName.toString() + "_ajax";
                build = new ResultConfig.Builder(resultCode, ActionChainResult.class.getName());
                build.addParam("actionName", TisActionMapper.addUnderline(resultName).toString());
                build.addParam("namespace", "/" + matcher.group(1)
                  + StringUtils.replace(matcher.group(3), ".", "/") + "#screen");
                resultsConfig.put(resultCode, build.build());
            } else {
                build = new ResultConfig.Builder(resultName, TerminatorVelocityResult.class.getName());
                build.addParam("location", "/" + matcher.group(1) + "/templates/"
                  + matcher.group(2) + StringUtils.replace(matcher.group(3), ".", "/") + '/' + getViewName(resultName) + ".vm");
                resultsConfig.put(resultName, build.build());
            }
        } else {
            throw new IllegalStateException("class name :" + actionClass.getName() + " is illegal");
        }
        return resultsConfig;
    }

    private String getViewName(String resultName) {
        char[] simpleClassName = resultName.toCharArray();
        StringBuffer resultKey = new StringBuffer(String.valueOf(Character.toLowerCase(simpleClassName[0])));
        resultKey.append(simpleClassName, 1, simpleClassName.length - 1);
        return resultKey.toString();
    }
}
