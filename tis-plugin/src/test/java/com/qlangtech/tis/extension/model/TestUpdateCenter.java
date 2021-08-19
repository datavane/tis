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

package com.qlangtech.tis.extension.model;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.util.TestHeteroList;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-10 09:13
 **/
public class TestUpdateCenter extends TestCase {
    File dataDir;


    @Override
    protected void setUp() throws Exception {
        HttpUtils.mockConnMaker = new HttpUtils.DefaultMockConnectionMaker();
        HttpUtils.addMockGlobalParametersConfig();
        CenterResource.setNotFetchFromCenterRepository();
        //http://mirror.qlangtech.com/update-site/default.json
        System.clearProperty(Config.KEY_DATA_DIR);
        dataDir = Config.setTestDataDir();
        TestHeteroList.setTISField();
        TIS.clean();
    }

    public void testIntallPlugin() throws Exception {
        HttpUtils.CacheMockRes cacheMockRes = getUpdateSiteHttpStub();

        final String mysqlV5Ds = DataSourceFactory.DS_TYPE_MYSQL + "-V5";

        assertNotNull(cacheMockRes);

        UpdateCenter updateCenter = new UpdateCenter();
        updateCenter.load();
        updateCenter.updateAllSites();

        assertTrue("verfiyResHasFetch", cacheMockRes.verfiyResHasFetch());

        List<Descriptor<DataSourceFactory>> descriptorList = TIS.get().getDescriptorList(DataSourceFactory.class);
        Optional<Descriptor<DataSourceFactory>> first = descriptorList.stream().filter((r) -> mysqlV5Ds.equals(r.getDisplayName())).findFirst();
        assertFalse(DataSourceFactory.DS_TYPE_MYSQL + " descriptor must NOT present", first.isPresent());
        String pluginName = "tis-ds-mysql-v5-plugin";
        UpdateSite.Plugin mysqlDSPlugin = updateCenter.getPlugin(pluginName);
        assertNotNull(pluginName + " can not be null", mysqlDSPlugin);
        /** ==========================================================================
         * 开始安装
         * ==========================================================================*/
        Future<UpdateCenter.UpdateCenterJob> job = mysqlDSPlugin.deploy(true);
        UpdateCenter.DownloadJob downloadJob = (UpdateCenter.DownloadJob) job.get();
        System.out.println(downloadJob.status);
        // 安装成功
        assertTrue(downloadJob.status instanceof UpdateCenter.DownloadJob.Success);
        // TIS.clean();
        // 重新获取插件实例
        descriptorList = TIS.get().getDescriptorList(DataSourceFactory.class);
        first = descriptorList.stream().filter((r) -> mysqlV5Ds.equals(r.getDisplayName())).findFirst();
        assertTrue(mysqlV5Ds + " descriptor must present", first.isPresent());

    }

    private HttpUtils.CacheMockRes getUpdateSiteHttpStub() {
        if (HttpUtils.mockConnMaker != null) {
            // HttpUtils.mockConnMaker.clearStubs();
        }
        return HttpUtils.addMockApply(0, "http://mirror.qlangtech.com/update-site/default.json", "default-update-site.json", TestUpdateCenter.class);
    }

    public void testLoad() throws Exception {

        HttpUtils.CacheMockRes cacheMockRes = getUpdateSiteHttpStub();

        File localDftUpdateSiteJSON = new File(TIS.pluginCfgRoot, "updates/default.json");
        FileUtils.deleteQuietly(localDftUpdateSiteJSON);

        UpdateCenter updateCenter = new UpdateCenter();
        updateCenter.load();

        List<FormValidation> formValidations = updateCenter.updateAllSites();
        assertEquals(1, formValidations.size());

        assertTrue("verfiyResHasFetch", cacheMockRes.verfiyResHasFetch());

        assertTrue(updateCenter.getSiteList().size() > 0);


        List<UpdateSite.Plugin> availables = updateCenter.getAvailables();
        assertTrue(availables.size() > 0);
    }

}
