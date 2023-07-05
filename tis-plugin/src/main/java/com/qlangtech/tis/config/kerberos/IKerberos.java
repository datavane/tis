/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.config.kerberos;

import com.qlangtech.tis.config.ParamsConfig;

import java.io.File;

/**
 * 为HDFS，Hive 提供Kerberos 认证
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-06-01 09:54
 **/
public interface IKerberos {
    static final String IDENTITY = "kerberos";
    public static IKerberos getKerberosCfg(String idName) {
        return ParamsConfig.getItem(idName, IKerberos.IDENTITY);
    }
//    /**
//     * must be type of: org.apache.hadoop.hive.conf.Configuration
//     *
//     * @param config
//     */
  //  public <Configuration> void setConfiguration(Configuration config);


    public String getPrincipal();
    public String getKeytabPath();
    public File getKeyTabPath();

   // java.security.krb5.conf

    /**
     * system prop of 'java.security.krb5.kdc'
     * @see sun.security.krb5.Config
     * @return
     */
    public Krb5Res getKrb5Res();
}
