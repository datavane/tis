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
package com.qlangtech.tis.pubhook.common;

import com.qlangtech.tis.manage.common.Config;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-25
 */
public enum RunEnvironment {

    // ///////////
    DAILY("daily", (short) 0, "日常环境", () -> Config.getConfigRepositoryHost()),
    // //////////////////////
    ONLINE(//
            "online", //
            (short) 2, // /
            "线上环境", // /
            null);


    public static final String KEY_RUNTIME = "runtime";

    public static boolean isDevelopMode() {
        return (getSysRuntime() == DAILY);
    }

    public static void setSysRuntime(RunEnvironment runtime) {
        System.setProperty(RunEnvironment.KEY_RUNTIME, runtime.getKeyName());
    }

    public static boolean isOnlineMode() {
        return (getSysRuntime() == ONLINE);
    }

    // private static RunEnvironment runtime;
    public static RunEnvironment getSysRuntime() {
        return RunEnvironment.getEnum(Config.getInstance().getRuntime());
    }

    public static RunEnvironment getSysEnvironment() {
        return getSysRuntime();
    }

    private final Short id;

    private final String keyName;

    private final String describe;

    private final Callable<String> innerRepositoryURL;


    private RunEnvironment(String keyName, Short id, String describe, Callable<String> innerRepositoryURL) {
        this.id = id;
        this.keyName = keyName;
        this.describe = describe;
        this.innerRepositoryURL = innerRepositoryURL;
    }

    public String getInnerRepositoryURL() {
        try {
            return innerRepositoryURL.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Short getId() {
        return id;
    }

    public String getDescribe() {
        return describe;
    }

    public String getKeyName() {
        return keyName;
    }

    public static RunEnvironment getEnum(String key) {
        EnumSet<RunEnvironment> all = EnumSet.allOf(RunEnvironment.class);
        for (RunEnvironment r : all) {
            if (r.getKeyName().equals(key)) {
                return r;
            }
        }
        throw new IllegalArgumentException("key:" + key + " is invalid");
    }

    public static RunEnvironment getEnum(short key) {
        EnumSet<RunEnvironment> all = EnumSet.allOf(RunEnvironment.class);
        for (RunEnvironment r : all) {
            if (r.getId() == key) {
                return r;
            }
        }
        throw new IllegalArgumentException("key:" + key + " is invalid");
    }

    private static List<RunEnvironment> environmentList = new ArrayList<RunEnvironment>();

    static {
        try {
            RunEnvironment[] fields = RunEnvironment.values();
            // Object o = null;
            for (RunEnvironment f : fields) {
                // o = f.get(null);
                // if (o instanceof RunEnvironment) {
                // environmentList.add(((RunEnvironment) o));
                // }
                environmentList.add(f);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<RunEnvironment> getRunEnvironmentList() {
        return environmentList;
    }

    public static void main(String[] arg) throws Exception {
        List<RunEnvironment> environmentList = RunEnvironment.getRunEnvironmentList();
        for (RunEnvironment envir : environmentList) {
            System.out.println(envir);
        }
    }
}
