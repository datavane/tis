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
package org.apache.solr.core;

import com.qlangtech.tis.solrextend.cloud.TisConfigSetService;
import java.lang.reflect.Field;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisCoreContainer extends CoreContainer {

    public TisCoreContainer(NodeConfig config, boolean asyncSolrCoreLoad) {
        super(config, asyncSolrCoreLoad);
    }
    // static final Field coreConfigServiceField;
    // 
    // static {
    // try {
    // coreConfigServiceField = CoreContainer.class
    // .getDeclaredField("coreConfigService");
    // coreConfigServiceField.setAccessible(true);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // @Override
    // public void load() {
    // super.load();
    // TisConfigSetService configSetService = new TisConfigSetService(loader, this.getZkController());
    // try {
    // coreConfigServiceField.set(this, configSetService);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
}
