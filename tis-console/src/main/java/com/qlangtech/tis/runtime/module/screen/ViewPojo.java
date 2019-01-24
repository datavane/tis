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
package com.qlangtech.tis.runtime.module.screen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.servlet.DownloadServlet;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.solrdao.IBuilderContext;
import com.qlangtech.tis.solrdao.SolrPojoBuilder;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ViewPojo extends BasicScreen {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        final AppDomainInfo appDomainInfo = this.getAppDomain();
        final StringWriter writer = new StringWriter();
        if (!downloadResource(appDomainInfo, this, writer)) {
            return;
        }
        context.put("pojoContent", writer.toString());
        writer.close();
    }

    public static interface ResourcePrep {

        public void prepare(IBuilderContext builderContext);
    }

    public static boolean downloadResource(final AppDomainInfo appDomainInfo, BasicModule module, final Writer writer) throws Exception {
        return downloadResource(appDomainInfo, module, writer, new ResourcePrep() {

            @Override
            public void prepare(IBuilderContext builderContext) {
            }
        });
    }

    public static boolean downloadResource(final AppDomainInfo appDomainInfo, BasicModule module, final Writer writer, ResourcePrep prepare) throws Exception {
        ServerGroup group = DownloadServlet.getServerGroup(appDomainInfo.getAppid(), (short) 0, appDomainInfo.getRunEnvironment().getId(), module.getServerGroupDAO());
        if (group == null) {
            module.addActionError("您还没有为该应用配置Snapshot");
            return false;
        }
        final SnapshotDomain snapshot = module.getSnapshotViewDAO().getView(group.getPublishSnapshotId(), appDomainInfo.getRunEnvironment());
        // final StringWriter writer = new StringWriter();
        final IBuilderContext builderContext = new IBuilderContext() {

            @Override
            public void closeWriter(PrintWriter writer) {
            }

            @Override
            public Writer getOutputStream() throws Exception {
                return writer;
            }

            @Override
            public String getPojoName() {
                return StringUtils.capitalize(StringUtils.substringAfter(appDomainInfo.getAppName(), "search4"));
            }

            @Override
            public InputStream getResourceInputStream() throws Exception {
                return new ByteArrayInputStream(snapshot.getSolrSchema().getContent());
            }

            @Override
            public String getTargetNameSpace() {
                return "com.qlangtech.tis";
            }
        };
        prepare.prepare(builderContext);
        SolrPojoBuilder builder = new SolrPojoBuilder(builderContext);
        builder.create();
        // return writer;
        return true;
    }
}
