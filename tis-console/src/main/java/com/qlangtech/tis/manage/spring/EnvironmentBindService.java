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
package com.qlangtech.tis.manage.spring;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.common.DefaultFilter;
import com.qlangtech.tis.manage.common.DefaultFilter.AppAndRuntime;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
@SuppressWarnings("all")
public abstract class EnvironmentBindService<T> {

    protected static final Pattern ZK_ADDRESS = Pattern.compile("([^,]+?):\\d+");

    protected void validateServerIsReachable(String ipaddress) {
        try {
            InetAddress address = InetAddress.getByName(ipaddress);
            if (!address.isReachable(2000)) {
                throw new IllegalStateException("ipaddress " + ipaddress + " is not reachable");
            }
        } catch (UnknownHostException e2) {
            throw new RuntimeException(e2);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    private IApplicationDAO applicationDAO;

    @Autowired
    public void setApplicationDAO(IApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    private final Map<RunEnvironment, T> serviceMap = Collections.synchronizedMap(new HashMap<RunEnvironment, T>());

    public T getInstance() {
        AppAndRuntime appAndRuntime = DefaultFilter.getAppAndRuntime();
        if (appAndRuntime == null) {
            appAndRuntime = new AppAndRuntime();
            appAndRuntime.setRuntime(RunEnvironment.getSysRuntime());
        }
        RunEnvironment runtime = appAndRuntime.getRuntime();
        return this.getInstance(runtime);
    }

    protected final void cleanInstance(RunEnvironment runtime) {
        serviceMap.remove(runtime);
    }

    T getInstance(RunEnvironment runtime) {
        T service = null;
        if ((service = serviceMap.get(runtime)) == null) {
            synchronized (serviceMap) {
                if ((service = serviceMap.get(runtime)) == null) {
                    serviceMap.put(runtime, (service = createSerivce(runtime)));
                }
            }
        }
        if (service == null) {
            throw new IllegalStateException("the current factory can not be null,runtime:" + runtime);
        }
        return service;
    }

    protected abstract T createSerivce(RunEnvironment runtime);
}
