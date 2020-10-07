///**
// * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
// *
// * This program is free software: you can use, redistribute, and/or modify
// * it under the terms of the GNU Affero General Public License, version 3
// * or later ("AGPL"), as published by the Free Software Foundation.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package com.qlangtech.tis.runtime.module.action;
//
//import java.net.InetAddress;
//import com.alibaba.citrus.turbine.Context;
//import org.apache.commons.lang.StringUtils;
//import com.qlangtech.tis.manage.PermissionConstant;
//import com.qlangtech.tis.manage.biz.dal.pojo.ServerPool;
//import com.qlangtech.tis.manage.biz.dal.pojo.ServerPoolCriteria;
//import com.qlangtech.tis.manage.common.ManageUtils;
//import com.qlangtech.tis.manage.spring.aop.Func;
//
///**
// * @author 百岁（baisui@qlangtech.com）
// * @date 2012-4-9
// */
//public class ServerPoolAction extends BasicModule {
//
//    /**
//     */
//    private static final long serialVersionUID = 1L;
//
//    // /**
//    // * 向服务器池中添加新的服务器
//    // *
//    // * @param context
//    // */
//    // @Func(PermissionConstant.GLOBAL_SERVER_POOL_SET)
//    // public void doAddPoolServer(@FormGroup("serverpooladd") ServerPool server,
//    // Context context) {
//    //
//    // Assert.assertNotNull(server);
//    //
//    // server.setCreateTime(new Date());
//    // server.setDeleteFlag(0);
//    //
//    // ServerPoolCriteria criteria = new ServerPoolCriteria();
//    //
//    // criteria.createCriteria().andServerNameEqualTo(server.getServerName())
//    // .andRuntEnvironmentEqualTo(server.getRuntEnvironment());
//    //
//    // criteria.or(criteria.createCriteria().andIpAddressEqualTo(
//    // server.getIpAddress()).andRuntEnvironmentEqualTo(
//    // server.getRuntEnvironment()));
//    //
//    // if (this.getServerPoolDAO().countByExample(criteria) > 0) {
//    // this.addErrorMessage(context, "不能重复添加相同的服务器");
//    // return;
//    // }
//    //
//    // this.getServerPoolDAO().insertSelective(server);
//    //
//    // this.addActionMessage(context, "添加新的服务器成功");
//    // }
//    @Override
//    public boolean isEnableDomainView() {
//        return false;
//    }
//
//    /**
//     * 删除服务器
//     *
//     * @param context
//     */
//    @Func(PermissionConstant.GLOBAL_SERVER_POOL_SET)
//    public void doDeleteServer(Context context) {
//        // Integer serverid = this.getInt("serverid");
//        // if (serverid == null) {
//        // throw new IllegalArgumentException("serverid can not be null");
//        // }
//        //
//        // ServerPoolCriteria query = new ServerPoolCriteria();
//        // query.createCriteria().andSpIdEqualTo(serverid);
//        //
//        // ServerPool server = new ServerPool();
//        // server.setDeleteFlag(ManageUtils.DELETE);
//        //
//        // this.getServerPoolDAO().updateByExampleSelective(server, query);
//        updateServerPool(context, true);
//    }
//
//    /**
//     * @param context
//     */
//    @Func(PermissionConstant.GLOBAL_SERVER_POOL_SET)
//    public void doRecoveryServer(Context context) {
//        updateServerPool(context, false);
//    }
//
////    /**
////     * 通过ip地址取host，或者通过host名取ip地址
////     *
////     * @param context
////     */
////    @Func(PermissionConstant.GLOBAL_SERVER_POOL_SET)
////    public void doFetchAddressInfo(Context context) throws Exception {
////        String ipAddress = this.getString("ip");
////        String host = this.getString("host");
////        boolean receiveIp = StringUtils.isNotBlank(ipAddress);
////        if (!receiveIp && StringUtils.isBlank(host)) {
////            return;
////        }
////        InetAddress address = InetAddress.getByName(receiveIp ? ipAddress : host);
////        if (StringUtils.equals(address.getHostName(), address.getHostAddress())) {
////            return;
////        }
////        getResponse().getWriter().print(receiveIp ? address.getHostName() : address.getHostAddress());
////    }
//
////    private void updateServerPool(Context context, boolean delete) {
////        Integer serverid = this.getInt("serverid");
////        if (serverid == null) {
////            throw new IllegalArgumentException("serverid can not be null");
////        }
////        ServerPoolCriteria query = new ServerPoolCriteria();
////        query.createCriteria().andSpIdEqualTo(serverid);
////        ServerPool server = new ServerPool();
////        server.setDeleteFlag(delete ? ManageUtils.DELETE : ManageUtils.UN_DELETE);
////        this.getServerPoolDAO().updateByExampleSelective(server, query);
////    }
//}
