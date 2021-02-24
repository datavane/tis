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
package com.qlangtech.tis.manage.spring;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.common.DefaultFilter;
import com.qlangtech.tis.manage.common.DefaultFilter.AppAndRuntime;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-8-13
 */
@SuppressWarnings("all")
public abstract class EnvironmentBindService<T> {

  final static List<Runnable> cleanableListeners = Lists.newArrayList();

  public static void cleanCacheService() {
    cleanableListeners.forEach((runnable) -> {
      runnable.run();
    });
  }

  protected static final Pattern ZK_ADDRESS = Pattern.compile("([^,]+?):\\d+");
  protected final Map<RunEnvironment, T> serviceMap = Collections.synchronizedMap(new HashMap<RunEnvironment, T>());

  public EnvironmentBindService() {
    cleanableListeners.add(() -> {
      serviceMap.clear();
    });
  }

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
