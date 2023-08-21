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

package com.qlangtech.tis.config.authtoken;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import org.junit.Assert;

import java.util.function.Consumer;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-07-24 13:43
 **/
public class UserTokenUtils {

    public static UserToken createNoneAuthToken() {
        return createToken("com.qlangtech.tis.config.authtoken.impl.OffUserToken", (formData -> {
        }));
    }

    public static UserToken createKerberosToken() {
        return createKerberosToken("k2");
    }

    public static UserToken createKerberosToken(String kerRef) {
        return createToken("com.qlangtech.tis.config.authtoken.impl.KerberosUserToken"
                , (kform) -> {
                    kform.addProp("kerberos", kerRef);
                });
    }

    private static UserToken createToken(String describleClazzName, Consumer<Descriptor.FormData> formProcess) {
        Descriptor.FormData kform = new Descriptor.FormData();
        formProcess.accept(kform);
        Descriptor kuserTokenDesc = TIS.get().getDescriptor(describleClazzName);
        Assert.assertNotNull("kuserTokenDesc can not be null", kuserTokenDesc);
        return (UserToken) kuserTokenDesc.newInstance("test", kform).getInstance();
    }
}
