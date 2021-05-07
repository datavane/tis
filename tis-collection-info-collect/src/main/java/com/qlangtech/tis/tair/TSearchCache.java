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
package com.qlangtech.tis.tair;

import java.io.Serializable;
import com.qlangtech.tis.tair.imp.ITSearchCache;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年8月29日下午5:21:51
 */
public class TSearchCache implements ITSearchCache {

    @Override
    public boolean put(Serializable key, Serializable obj) {
        return false;
    }

    @Override
    public boolean put(Serializable key, Serializable obj, int expir) {
        return false;
    }

    @Override
    public boolean invalid(Serializable key) {
        return false;
    }

    @Override
    public <T> T getObj(Serializable key) {
        return null;
    }
    // private static final Logger LOGGER_LOG =
    // LoggerFactory.getLogger(TSearchCache.class);
    // 
    // private static final int NAMESPACE = 728;
    // private MultiClusterTairManager mdbTairManager;
    // 
    // public MultiClusterTairManager getMdbTairManager() {
    // return mdbTairManager;
    // }
    // 
    // @Autowired
    // public void setMdbTairManager(MultiClusterTairManager mdbTairManager) {
    // this.mdbTairManager = mdbTairManager;
    // }
    // 
    // @Override
    // public boolean put(Serializable key, Serializable obj) {
    // ResultCode rc = mdbTairManager.put(NAMESPACE, key, obj);
    // return rc.isSuccess();
    // }
    // 
    // @Override
    // public boolean put(Serializable key, Serializable obj, int expir) {
    // ResultCode rc = mdbTairManager.put(NAMESPACE, key, obj, 0, expir);
    // return rc.isSuccess();
    // }
    // 
    // @Override
    // public boolean invalid(Serializable key) {
    // ResultCode rc = mdbTairManager.invalid(NAMESPACE, key);
    // return rc.isSuccess();
    // }
    // 
    // @SuppressWarnings("all")
    // public <T> T getObj(Serializable key) {
    // Result<DataEntry> result = mdbTairManager.get(NAMESPACE, key);
    // if (ResultCode.DATANOTEXSITS.equals(result.getRc())) {
    // return null;
    // }
    // if (ResultCode.SUCCESS.equals(result.getRc())) {
    // return (T) result.getValue().getValue();
    // }
    // 
    // LOGGER_LOG.error("get " + key + " failed," + result.getRc().getCode());
    // return null;
    // 
    // 
    // }
}
