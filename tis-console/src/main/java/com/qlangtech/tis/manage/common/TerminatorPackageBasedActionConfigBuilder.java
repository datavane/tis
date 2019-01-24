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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.PackageBasedActionConfigBuilder;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.DefaultInterceptorRef;
import com.google.common.collect.Lists;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig.Builder;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.AnnotationUtils;
import com.opensymphony.xwork2.util.finder.ClassFinder;
import com.opensymphony.xwork2.util.finder.ClassFinder.ClassInfo;
import com.opensymphony.xwork2.util.finder.Test;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorPackageBasedActionConfigBuilder extends PackageBasedActionConfigBuilder {

    private final PackageConfig parentPkgConfig;

    private static final Logger LOG = LoggerFactory.getLogger(TerminatorPackageBasedActionConfigBuilder.class);

    @Inject
    public TerminatorPackageBasedActionConfigBuilder(Configuration configuration, Container container, ObjectFactory objectFactory, @Inject("struts.convention.redirect.to.slash") String redirectToSlash, @Inject("struts.convention.default.parent.package") String defaultParentPackage) {
        super(configuration, container, objectFactory, redirectToSlash, defaultParentPackage);
        this.parentPkgConfig = configuration.getPackageConfig("default");
        Assert.assertNotNull(this.parentPkgConfig);
    }

    @Override
    protected Test<ClassInfo> getActionClassTest() {
        return super.getActionClassTest();
    }

    public static final Pattern NAMESPACE_PATTERN = Pattern.compile("com\\.taobao\\.terminator\\.(\\w+)\\.module\\.(screen|action|control)(.*)(\\..*)$");

    public static final Pattern NAMESPACE_TIS_PATTERN = Pattern.compile("com\\.baisui\\.tis\\.(\\w+)\\.module\\.(screen|action|control)(.*)(\\..*)$");

    public static void main(String[] arg) {
       
    }

    // @Override
    // protected String determineActionName(Class<?> actionClass) {
    // 
    // return super.determineActionName(actionClass);
    // }
    @Override
    protected ClassFinder buildClassFinder(Test<String> classPackageTest, final List<URL> urls) {
        File jar = new File("./");
        String[] targetJars = jar.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return StringUtils.endsWith(name, ".jar");
            }
        });
        if (targetJars == null || targetJars.length != 1) {
            return super.buildClassFinder(classPackageTest, urls);
        }
        try {
            List<URL> url = Lists.newArrayList();
            for (String f : targetJars) {
                url.add((new File(jar, f).toURI().toURL()));
            }
            return super.buildClassFinder(classPackageTest, url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Builder getPackageConfig(Map<String, Builder> packageConfigs, String actionNamespace, String actionPackage, Class<?> actionClass, Action action) {
        Matcher matcher = NAMESPACE_PATTERN.matcher(actionClass.getName());
        // 解析struts2的命名空间
        String name = null;
        if (matcher.matches() || (matcher = NAMESPACE_TIS_PATTERN.matcher(actionClass.getName())).matches()) {
            name = '/' + matcher.group(1) + StringUtils.replace(matcher.group(3), ".", "/") + "#" + matcher.group(2);
        } else {
            throw new IllegalStateException("actionPackage:" + actionPackage + " is not a valid actionPackage");
        }
        PackageConfig.Builder pkgConfig = packageConfigs.get(name);
        if (pkgConfig == null) {
            pkgConfig = new PackageConfig.Builder(name).namespace(name).addParent(this.parentPkgConfig);
            packageConfigs.put(name, pkgConfig);
            // check for @DefaultInterceptorRef in the package
            DefaultInterceptorRef defaultInterceptorRef = AnnotationUtils.findAnnotation(actionClass, DefaultInterceptorRef.class);
            if (defaultInterceptorRef != null) {
                pkgConfig.defaultInterceptorRef(defaultInterceptorRef.value());
                if (LOG.isTraceEnabled())
                    LOG.debug("Setting [#0] as the default interceptor ref for [#1]", defaultInterceptorRef.value(), pkgConfig.getName());
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Created package config named [#0] with a namespace [#1]", name, actionNamespace);
        }
        return pkgConfig;
    // return super.getPackageConfig(packageConfigs, actionNamespace,
    // actionPackage, actionClass, action);
    }
    // @Override
    // protected Test<String> getClassPackageTest() {
    // 
    // return new Test<String>() {
    // public boolean test(String className) {
    // return includeClassNameInActionScan(className);
    // }
    // };
    // }
}
