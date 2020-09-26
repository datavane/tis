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
package com.qlangtech.tis.realtime.test.order.dao;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IOrderDAOFacade {

    IWaitinginstanceinfoDAO getWaitinginstanceinfoDAO();

    IOrderRefundDAO getOrderRefundDAO();

    IInstanceAssetDAO getInstanceAssetDAO();

    IOrderTagDAO getOrderTagDAO();

    IPaydetailDAO getPaydetailDAO();

    IPayinfoDAO getPayinfoDAO();

    IWaitingordercridDAO getWaitingordercridDAO();

    IWaitingPayDAO getWaitingPayDAO();

    IDiscountDetailDAO getDiscountDetailDAO();

    IOrderPromotionDAO getOrderPromotionDAO();

    IQueuestatusDAO getQueuestatusDAO();

    IOrderBillDAO getOrderBillDAO();

    IOrderSnapshotDAO getOrderSnapshotDAO();

    IInstancedetailDAO getInstancedetailDAO();

    ICustomerOrderRelationDAO getCustomerOrderRelationDAO();

    ITotalpayinfoDAO getTotalpayinfoDAO();

    IPresellOrderExtraDAO getPresellOrderExtraDAO();

    ISpecialfeeDAO getSpecialfeeDAO();

    IRefundPayItemDAO getRefundPayItemDAO();

    ITakeoutOrderExtraDAO getTakeoutOrderExtraDAO();

    IWaitingorderdetailDAO getWaitingorderdetailDAO();

    IGridFieldDAO getGridFieldDAO();

    IServicebillinfoDAO getServicebillinfoDAO();

    ISimplecodeorderDAO getSimplecodeorderDAO();

    IUserDAO getUserDAO();

    IGlobalcodeorderDAO getGlobalcodeorderDAO();

    IOrderdetailDAO getOrderdetailDAO();

    IPromotionDAO getPromotionDAO();

    IQueueopDAO getQueueopDAO();
}
