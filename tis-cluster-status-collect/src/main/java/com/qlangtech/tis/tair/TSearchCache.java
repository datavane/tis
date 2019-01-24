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
package com.qlangtech.tis.tair;

import java.io.Serializable;
import com.qlangtech.tis.tair.imp.ITSearchCache;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
    // private static final Log LOGGER_LOG =
    // LogFactory.getLog(TSearchCache.class);
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
