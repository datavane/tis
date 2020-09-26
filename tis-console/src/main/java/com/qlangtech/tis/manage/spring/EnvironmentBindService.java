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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-8-13
 */
@SuppressWarnings("all")
public abstract class EnvironmentBindService<T> {

    protected static final Pattern ZK_ADDRESS = Pattern.compile("([^,]+?):\\d+");

    protected void validateServerIsReachable(String ipaddress) {
    // try {
    // InetAddress address = InetAddress.getByName(ipaddress);
    // if (!address.isReachable(2000)) {
    // throw new IllegalStateException("ipaddress " + ipaddress + " is not reachable");
    // }
    // } catch (UnknownHostException e2) {
    // throw new RuntimeException(e2);
    // } catch (IOException e2) {
    // throw new RuntimeException(e2);
    // }
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
            appAndRuntime.setRuntime(DefaultFilter.getRuntime());
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
