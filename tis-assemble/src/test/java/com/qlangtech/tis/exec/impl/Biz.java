///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.qlangtech.tis.exec.impl;
//
//import org.easymock.EasyMock;
//
///**
// * @author: 百岁（baisui@qlangtech.com）
// * @create: 2025-03-19 17:26
// **/
//public class Biz {
//
//    interface Context {
//
//    }
//
//    public class DefaultContext implements Context {
//
//
//    }
//
//    public interface User {
//
//        public void setContext(Context context);
//
//        public Context getContext();
//
//    }
//
//
//    public class UserManager {
//
//        public void process(User user) {
//            Context context = new DefaultContext();
//            user.setContext(context);
//            // do some logical biz
//            context = user.getContext();
//        }
//
//    }
//
//
//    public class TestUser {
//@Test
//        public void testProcess() {
//            UserManager userManager = new UserManager();
//            User user = EasyMock.mock(User.class);
//            user.setContext(EasyMock.anyObject(Context.class));
//
//            EasyMock.expect( user.getContext()).andReturn( /**此处如何获得process内部创建的context对象实例*/);
//    EasyMock.replay(user);
//            userManager.process(user);
//
//    EasyMock.verify(user);
//        }
//
//    }
//}
