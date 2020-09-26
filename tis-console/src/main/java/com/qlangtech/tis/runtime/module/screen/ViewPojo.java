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
package com.qlangtech.tis.runtime.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.servlet.DownloadServlet;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.solrdao.IBuilderContext;
import com.qlangtech.tis.solrdao.SolrPojoBuilder;
import org.apache.commons.lang3.StringUtils;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ViewPojo {

    private static final long serialVersionUID = 1L;

    public static interface ResourcePrep {

        public void prepare(IBuilderContext builderContext);
    }

    // @Override
    // public boolean isEnableDomainView() {
    // return false;
    // }
    public static boolean downloadResource(Context context, final AppDomainInfo appDomainInfo, BasicModule module, final Writer writer) throws Exception {
        return downloadResource(context, appDomainInfo, module, writer, new ResourcePrep() {

            @Override
            public void prepare(IBuilderContext builderContext) {
            }
        });
    }

    public static boolean downloadResource(Context context, final AppDomainInfo appDomainInfo, BasicModule module, final Writer writer, ResourcePrep prepare) throws Exception {
        ServerGroup group = DownloadServlet.getServerGroup(appDomainInfo.getAppid(), (short) 0, appDomainInfo.getRunEnvironment().getId(), module.getServerGroupDAO());
        if (group == null) {
            module.addErrorMessage(context, "您还没有为该应用配置Snapshot");
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
            public byte[] getResourceInputStream() {
                return snapshot.getSolrSchema().getContent();
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
