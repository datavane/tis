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
//package com.qlangtech.tis.runtime.module.misc.impl;
//
//import com.alibaba.citrus.turbine.Context;
//import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
//import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
//
//import java.io.PrintWriter;
//
///**
// * @author: 百岁（baisui@qlangtech.com）
// * @create: 2024-06-16 20:11
// **/
//public class AdaperControlMsgHandler implements IControlMsgHandler {
//
//
//
//    private final IControlMsgHandler delegate;
//
//    public AdaperControlMsgHandler(IFieldErrorHandler msgHandler) {
//        this((IControlMsgHandler) msgHandler);
//    }
//
//    public AdaperControlMsgHandler(IControlMsgHandler delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public boolean getBoolean(String key) {
//        return delegate.getBoolean(key);
//    }
//
//    @Override
//    public String getString(String key, String dftVal) {
//        return delegate.getString(key, dftVal);
//    }
//
//    @Override
//    public String getString(String key) {
//        return delegate.getString(key);
//    }
//
//    @Override
//    public String getRequestHeader(String key) {
//        return delegate.getRequestHeader(key);
//    }
//
//    @Override
//    public void addErrorMessage(Context context, String msg) {
//        delegate.addErrorMessage(context, msg);
//    }
//
//    @Override
//    public void setBizResult(Context context, Object result, boolean overwriteable) {
//        delegate.setBizResult(context, result, overwriteable);
//    }
//
//    @Override
//    public void setBizResult(Context context, Object result) {
//        delegate.setBizResult(context, result);
//    }
//
//    @Override
//    public void addActionMessage(Context context, String msg) {
//        delegate.addActionMessage(context, msg);
//    }
//
//    @Override
//    public void errorsPageShow(Context context) {
//        delegate.errorsPageShow(context);
//    }
//
//    @Override
//    public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
//        return delegate.validateBizLogic(logicType, context, fieldName, value);
//    }
//
//    @Override
//    public void addFieldError(Context context, String fieldName, String msg, Object... params) {
//        delegate.addFieldError(context, fieldName, msg, params);
//    }
//
//
//    @Override
//    public PrintWriter getEventStreamWriter() {
//        return delegate.getEventStreamWriter();
//    }
//    @Override
//    public String getTISDataXName() {
//        return delegate.getTISDataXName();
//    }
//
//    @Override
//    public String getCollectionName() {
//        return delegate.getCollectionName();
//    }
//
//    @Override
//    public boolean isCollectionAware() {
//        return delegate.isCollectionAware();
//    }
//}
