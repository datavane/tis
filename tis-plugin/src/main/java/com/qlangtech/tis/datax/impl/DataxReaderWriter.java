///**
// * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
// * <p>
// * This program is free software: you can use, redistribute, and/or modify
// * it under the terms of the GNU Affero General Public License, version 3
// * or later ("AGPL"), as published by the Free Software Foundation.
// * <p>
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.
// * <p>
// * You should have received a copy of the GNU Affero General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package com.qlangtech.tis.datax.impl;
//
//import com.qlangtech.tis.TIS;
//import com.qlangtech.tis.datax.IDataxReader;
//import com.qlangtech.tis.datax.IDataxWriter;
//import com.qlangtech.tis.extension.Describable;
//import com.qlangtech.tis.extension.Descriptor;
//import com.qlangtech.tis.plugin.IdentityName;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @author 百岁（baisui@qlangtech.com）
// * @date 2021-04-07 14:54
// */
//public abstract class DataxReaderWriter implements Describable<DataxReaderWriter>, IdentityName, IDataxWriter, IDataxReader {
//
//
//
//    @Override
//    public Descriptor<DataxReaderWriter> getDescriptor() {
//        return TIS.get().getDescriptor(this.getClass());
//    }
//}
