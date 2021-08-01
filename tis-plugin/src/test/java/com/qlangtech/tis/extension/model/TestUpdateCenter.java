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
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
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
    static final File dataDir;

    static {

        //http://mirror.qlangtech.com/update-site/default.json

        dataDir = Config.setTestDataDir();
    }


    public void testIntallPlugin() throws Exception {
        HttpUtils.CacheMockRes cacheMockRes = getUpdateSiteHttpStub();

        assertNotNull(cacheMockRes);

        UpdateCenter updateCenter = new UpdateCenter();
        updateCenter.load();
        updateCenter.updateAllSites();

        assertTrue("verfiyResHasFetch", cacheMockRes.verfiyResHasFetch());

        List<Descriptor<DataSourceFactory>> descriptorList = TIS.get().getDescriptorList(DataSourceFactory.class);
        Optional<Descriptor<DataSourceFactory>> first = descriptorList.stream().filter((r) -> DataSourceFactory.DS_TYPE_MYSQL.equals(r.getDisplayName())).findFirst();
        assertFalse(DataSourceFactory.DS_TYPE_MYSQL + " descriptor must present", first.isPresent());

        UpdateSite.Plugin testPlugin = updateCenter.getPlugin("tis-ds-mysql-plugin");
        assertNotNull(testPlugin);
        /** ==========================================================================
         * 开始安装
         * ==========================================================================*/
        Future<UpdateCenter.UpdateCenterJob> job = testPlugin.deploy(true);
        UpdateCenter.DownloadJob downloadJob = (UpdateCenter.DownloadJob) job.get();
        System.out.println(downloadJob.status);
        assertTrue(downloadJob.status instanceof UpdateCenter.DownloadJob.Success);
        // TIS tis = TIS.get();
//        tis.descriptorLists.clear();
//        tis.extensionLists.clear();

//        Class<?> aClass = tis.getPluginManager().uberClassLoader.findClass("com.qlangtech.tis.plugin.ds.mysql.MySQLDataSourceFactory");
//        assertNotNull(aClass);
//        List<PluginWrapper> activePlugins = tis.getPluginManager().activePlugins;

        // List<ExtensionComponent<Descriptor>> components = tis.getPluginManager().getPluginStrategy().findComponents(Descriptor.class, tis);

        descriptorList = TIS.get().getDescriptorList(DataSourceFactory.class);
        first = descriptorList.stream().filter((r) -> DataSourceFactory.DS_TYPE_MYSQL.equals(r.getDisplayName())).findFirst();
        assertTrue(DataSourceFactory.DS_TYPE_MYSQL + " descriptor must present", first.isPresent());

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
