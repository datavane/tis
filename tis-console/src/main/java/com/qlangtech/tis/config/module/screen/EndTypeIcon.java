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
//package com.qlangtech.tis.config.module.screen;
//
//import com.alibaba.citrus.turbine.Context;
//import com.opensymphony.xwork2.Action;
//import com.qlangtech.tis.plugin.IEndTypeGetter;
//import com.qlangtech.tis.runtime.module.screen.BasicScreen;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
//
//import javax.servlet.http.HttpServletResponse;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * @author 百岁 (baisui@qlangtech.com)
// * @date 2023/11/4
// */
//public class EndTypeIcon extends BasicScreen {
//  private static Pattern endtypeWithSize = Pattern.compile("(.+?)_(x[\\d]{1})");
//
//  @Override
//  public void execute(Context context) throws Exception {
//
//    String token = this.getString("endtype");
//    Matcher matcher = endtypeWithSize.matcher(token);
//    if (!matcher.matches()) {
//      throw new IllegalArgumentException("illegal token:" + token + " is not match pattern:" + endtypeWithSize);
//    }
//
//    IEndTypeGetter.EndType endtype = IEndTypeGetter.EndType.parse(matcher.group(1));
//    IEndTypeGetter.Icon icon = endtype.getIcon();
//    if (icon == null) {
//      throw new IllegalStateException("endtype:" + endtype + " relevant icon can not be null");
//    }
//    HttpServletResponse response = getResponse();
//    response.setContentType("image/png; charset=UTF-8");
//    String size = matcher.group(2);
//    switch (size) {
//      case "x1":
//        IOUtils.write(icon.x1(), response.getOutputStream());
//        return;
//      case "x2":
//        IOUtils.write(icon.x2(), response.getOutputStream());
//        return;
//      case "x3":
//        IOUtils.write(icon.x3(), response.getOutputStream());
//        return;
//      case "x4":
//        IOUtils.write(icon.x4(), response.getOutputStream());
//        return;
//      default:
//        throw new IllegalStateException("illegal size:" + size);
//    }
//  }
//
//  @Override
//  protected String getReturnCode() {
//    return Action.NONE;
//  }
//}
