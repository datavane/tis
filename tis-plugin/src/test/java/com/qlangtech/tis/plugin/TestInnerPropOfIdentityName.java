/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin;

import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.kerberos.IKerberos;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.innerprop.KerberosCfg;
import com.qlangtech.tis.plugin.innerprop.TestUploadKrb5Res;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.XStream2;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.List;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-04 13:52
 **/
public class TestInnerPropOfIdentityName extends TestCase {

    private static final String KERBEROS_NAME = "kerberos";
    private static final String FILE_TEST_NAME = "test.cfg";
    IPluginStore<ParamsConfig> targetPluginStore;

    @Override
    public void setUp() throws Exception {
        this.targetPluginStore = ParamsConfig.getTargetPluginStore(
                ParamsConfig.CONTEXT_PARAMS_CFG, IKerberos.IDENTITY + "_test", Optional.empty());

    }

    @Override
    public void tearDown() throws Exception {
        this.targetPluginStore.getTargetFile().delete();
    }

    /**
     * @see XStream2.IdentityNameConvert
     * @see XStream2.InnerPropOfIdentityNameConvert
     */
    public void testPropertyDoUnmarshal() {


        IPluginContext pluginContext = EasyMock.createMock("pluginContext", IPluginContext.class);
        EasyMock.expect(pluginContext.getRequestHeader(DataxReader.HEAD_KEY_REFERER)).andReturn("");
        List<Descriptor.ParseDescribable<ParamsConfig>> testPlugin = createTestPlugin();
        targetPluginStore.setPlugins(pluginContext, Optional.empty(), testPlugin);

        KerberosCfg saved = testPlugin.get(0).getInstance();
        Assert.assertNotNull(saved);
        TestUploadKrb5Res savedkrb5Res = (TestUploadKrb5Res) saved.getKrb5Res();
        Assert.assertNotNull("krb5Res can not be null", savedkrb5Res);
        Assert.assertEquals(KERBEROS_NAME + "_" + FILE_TEST_NAME, savedkrb5Res.getStoreFileName());


        KerberosCfg kerberos = (KerberosCfg) targetPluginStore.getPlugin();
        Assert.assertNotNull(kerberos);

        TestUploadKrb5Res krb5Res = (TestUploadKrb5Res) kerberos.getKrb5Res();
        Assert.assertNotNull("krb5Res can not be null", krb5Res);

        ;
        Assert.assertEquals(KERBEROS_NAME + "_" + FILE_TEST_NAME, krb5Res.getStoreFileName());
    }


    public static List<Descriptor.ParseDescribable<ParamsConfig>> createTestPlugin() {
        KerberosCfg p = new KerberosCfg();
        p.name = KERBEROS_NAME;
        TestUploadKrb5Res krb5Res = new TestUploadKrb5Res();
        krb5Res.file = FILE_TEST_NAME;
        p.krb5Res = krb5Res;
        List<Descriptor.ParseDescribable<ParamsConfig>> dlist = Lists.newArrayList();
        Descriptor.ParseDescribable parseDescribable = new Descriptor.ParseDescribable(p);

        dlist.add(parseDescribable);
        return dlist;
    }
}
