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

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisResultMapBuilder implements ResultMapBuilder {

    private static final ResultConfig ACTION_RESULT_CONFIG = (new ResultConfig.Builder(BasicModule.key_FORWARD, TisForwardResult.class.getName())).build();

    @Override
    public Map<String, ResultConfig> build(Class<?> actionClass, Action annotation, String actionName, PackageConfig packageConfig) {
        ResultConfig.Builder build = null;
        final String resultName = actionClass.getSimpleName();
        Map<String, ResultConfig> resultsConfig = new HashMap<String, ResultConfig>();
        resultsConfig.put(BasicModule.key_FORWARD, ACTION_RESULT_CONFIG);
        Matcher matcher = TisPackageBasedActionConfigBuilder.NAMESPACE_PATTERN.matcher(actionClass.getName());
        if (matcher.matches() || (matcher = TisPackageBasedActionConfigBuilder.NAMESPACE_TIS_PATTERN.matcher(actionClass.getName())).matches()) {
            if ("action".equals(matcher.group(2))) {
                // process ajax
                String resultCode = resultName.toString() + "_ajax";
                build = new ResultConfig.Builder(resultCode, AjaxValve.class.getName());
                resultsConfig.put(resultCode, build.build());
                // process action submit
                resultCode = resultName.toString() + "_action";
                // final String resultCode = resultName.toString() + "_ajax";
                build = new ResultConfig.Builder(resultCode, ActionChainResult.class.getName());
                build.addParam("actionName", TisActionMapper.addUnderline(resultName).toString());
                build.addParam("namespace", "/" + matcher.group(1) + StringUtils.replace(matcher.group(3), ".", "/") + "#screen");
                resultsConfig.put(resultCode, build.build());
            } else {
                build = new ResultConfig.Builder(resultName.toString(), TisVelocityResult.class.getName());
                build.addParam("location", "/" + matcher.group(1) + "/templates/" + matcher.group(2) + StringUtils.replace(matcher.group(3), ".", "/") + '/' + getViewName(resultName) + ".vm");
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
