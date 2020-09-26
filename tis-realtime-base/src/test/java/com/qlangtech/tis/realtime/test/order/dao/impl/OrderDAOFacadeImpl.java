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
package com.qlangtech.tis.realtime.test.order.dao.impl;

import com.qlangtech.tis.realtime.test.order.dao.ICustomerOrderRelationDAO;
import com.qlangtech.tis.realtime.test.order.dao.IDiscountDetailDAO;
import com.qlangtech.tis.realtime.test.order.dao.IGlobalcodeorderDAO;
import com.qlangtech.tis.realtime.test.order.dao.IGridFieldDAO;
import com.qlangtech.tis.realtime.test.order.dao.IInstanceAssetDAO;
import com.qlangtech.tis.realtime.test.order.dao.IInstancedetailDAO;
import com.qlangtech.tis.realtime.test.order.dao.IOrderBillDAO;
import com.qlangtech.tis.realtime.test.order.dao.IOrderDAOFacade;
import com.qlangtech.tis.realtime.test.order.dao.IOrderPromotionDAO;
import com.qlangtech.tis.realtime.test.order.dao.IOrderRefundDAO;
import com.qlangtech.tis.realtime.test.order.dao.IOrderSnapshotDAO;
import com.qlangtech.tis.realtime.test.order.dao.IOrderTagDAO;
import com.qlangtech.tis.realtime.test.order.dao.IOrderdetailDAO;
import com.qlangtech.tis.realtime.test.order.dao.IPaydetailDAO;
import com.qlangtech.tis.realtime.test.order.dao.IPayinfoDAO;
import com.qlangtech.tis.realtime.test.order.dao.IPresellOrderExtraDAO;
import com.qlangtech.tis.realtime.test.order.dao.IPromotionDAO;
import com.qlangtech.tis.realtime.test.order.dao.IQueueopDAO;
import com.qlangtech.tis.realtime.test.order.dao.IQueuestatusDAO;
import com.qlangtech.tis.realtime.test.order.dao.IRefundPayItemDAO;
import com.qlangtech.tis.realtime.test.order.dao.IServicebillinfoDAO;
import com.qlangtech.tis.realtime.test.order.dao.ISimplecodeorderDAO;
import com.qlangtech.tis.realtime.test.order.dao.ISpecialfeeDAO;
import com.qlangtech.tis.realtime.test.order.dao.ITakeoutOrderExtraDAO;
import com.qlangtech.tis.realtime.test.order.dao.ITotalpayinfoDAO;
import com.qlangtech.tis.realtime.test.order.dao.IUserDAO;
import com.qlangtech.tis.realtime.test.order.dao.IWaitingPayDAO;
import com.qlangtech.tis.realtime.test.order.dao.IWaitinginstanceinfoDAO;
import com.qlangtech.tis.realtime.test.order.dao.IWaitingordercridDAO;
import com.qlangtech.tis.realtime.test.order.dao.IWaitingorderdetailDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderDAOFacadeImpl implements IOrderDAOFacade {

    private final IWaitinginstanceinfoDAO waitinginstanceinfoDAO;

    private final IOrderRefundDAO orderRefundDAO;

    private final IInstanceAssetDAO instanceAssetDAO;

    private final IOrderTagDAO orderTagDAO;

    private final IPaydetailDAO paydetailDAO;

    private final IPayinfoDAO payinfoDAO;

    private final IWaitingordercridDAO waitingordercridDAO;

    private final IWaitingPayDAO waitingPayDAO;

    private final IDiscountDetailDAO discountDetailDAO;

    private final IOrderPromotionDAO orderPromotionDAO;

    private final IQueuestatusDAO queuestatusDAO;

    private final IOrderBillDAO orderBillDAO;

    private final IOrderSnapshotDAO orderSnapshotDAO;

    private final IInstancedetailDAO instancedetailDAO;

    private final ICustomerOrderRelationDAO customerOrderRelationDAO;

    private final ITotalpayinfoDAO totalpayinfoDAO;

    private final IPresellOrderExtraDAO presellOrderExtraDAO;

    private final ISpecialfeeDAO specialfeeDAO;

    private final IRefundPayItemDAO refundPayItemDAO;

    private final ITakeoutOrderExtraDAO takeoutOrderExtraDAO;

    private final IWaitingorderdetailDAO waitingorderdetailDAO;

    private final IGridFieldDAO gridFieldDAO;

    private final IServicebillinfoDAO servicebillinfoDAO;

    private final ISimplecodeorderDAO simplecodeorderDAO;

    private final IUserDAO userDAO;

    private final IGlobalcodeorderDAO globalcodeorderDAO;

    private final IOrderdetailDAO orderdetailDAO;

    private final IPromotionDAO promotionDAO;

    private final IQueueopDAO queueopDAO;

    public IWaitinginstanceinfoDAO getWaitinginstanceinfoDAO() {
        return this.waitinginstanceinfoDAO;
    }

    public IOrderRefundDAO getOrderRefundDAO() {
        return this.orderRefundDAO;
    }

    public IInstanceAssetDAO getInstanceAssetDAO() {
        return this.instanceAssetDAO;
    }

    public IOrderTagDAO getOrderTagDAO() {
        return this.orderTagDAO;
    }

    public IPaydetailDAO getPaydetailDAO() {
        return this.paydetailDAO;
    }

    public IPayinfoDAO getPayinfoDAO() {
        return this.payinfoDAO;
    }

    public IWaitingordercridDAO getWaitingordercridDAO() {
        return this.waitingordercridDAO;
    }

    public IWaitingPayDAO getWaitingPayDAO() {
        return this.waitingPayDAO;
    }

    public IDiscountDetailDAO getDiscountDetailDAO() {
        return this.discountDetailDAO;
    }

    public IOrderPromotionDAO getOrderPromotionDAO() {
        return this.orderPromotionDAO;
    }

    public IQueuestatusDAO getQueuestatusDAO() {
        return this.queuestatusDAO;
    }

    public IOrderBillDAO getOrderBillDAO() {
        return this.orderBillDAO;
    }

    public IOrderSnapshotDAO getOrderSnapshotDAO() {
        return this.orderSnapshotDAO;
    }

    public IInstancedetailDAO getInstancedetailDAO() {
        return this.instancedetailDAO;
    }

    public ICustomerOrderRelationDAO getCustomerOrderRelationDAO() {
        return this.customerOrderRelationDAO;
    }

    public ITotalpayinfoDAO getTotalpayinfoDAO() {
        return this.totalpayinfoDAO;
    }

    public IPresellOrderExtraDAO getPresellOrderExtraDAO() {
        return this.presellOrderExtraDAO;
    }

    public ISpecialfeeDAO getSpecialfeeDAO() {
        return this.specialfeeDAO;
    }

    public IRefundPayItemDAO getRefundPayItemDAO() {
        return this.refundPayItemDAO;
    }

    public ITakeoutOrderExtraDAO getTakeoutOrderExtraDAO() {
        return this.takeoutOrderExtraDAO;
    }

    public IWaitingorderdetailDAO getWaitingorderdetailDAO() {
        return this.waitingorderdetailDAO;
    }

    public IGridFieldDAO getGridFieldDAO() {
        return this.gridFieldDAO;
    }

    public IServicebillinfoDAO getServicebillinfoDAO() {
        return this.servicebillinfoDAO;
    }

    public ISimplecodeorderDAO getSimplecodeorderDAO() {
        return this.simplecodeorderDAO;
    }

    public IUserDAO getUserDAO() {
        return this.userDAO;
    }

    public IGlobalcodeorderDAO getGlobalcodeorderDAO() {
        return this.globalcodeorderDAO;
    }

    public IOrderdetailDAO getOrderdetailDAO() {
        return this.orderdetailDAO;
    }

    public IPromotionDAO getPromotionDAO() {
        return this.promotionDAO;
    }

    public IQueueopDAO getQueueopDAO() {
        return this.queueopDAO;
    }

    public OrderDAOFacadeImpl(IWaitinginstanceinfoDAO waitinginstanceinfoDAO, IOrderRefundDAO orderRefundDAO, IInstanceAssetDAO instanceAssetDAO, IOrderTagDAO orderTagDAO, IPaydetailDAO paydetailDAO, IPayinfoDAO payinfoDAO, IWaitingordercridDAO waitingordercridDAO, IWaitingPayDAO waitingPayDAO, IDiscountDetailDAO discountDetailDAO, IOrderPromotionDAO orderPromotionDAO, IQueuestatusDAO queuestatusDAO, IOrderBillDAO orderBillDAO, IOrderSnapshotDAO orderSnapshotDAO, IInstancedetailDAO instancedetailDAO, ICustomerOrderRelationDAO customerOrderRelationDAO, ITotalpayinfoDAO totalpayinfoDAO, IPresellOrderExtraDAO presellOrderExtraDAO, ISpecialfeeDAO specialfeeDAO, IRefundPayItemDAO refundPayItemDAO, ITakeoutOrderExtraDAO takeoutOrderExtraDAO, IWaitingorderdetailDAO waitingorderdetailDAO, IGridFieldDAO gridFieldDAO, IServicebillinfoDAO servicebillinfoDAO, ISimplecodeorderDAO simplecodeorderDAO, IUserDAO userDAO, IGlobalcodeorderDAO globalcodeorderDAO, IOrderdetailDAO orderdetailDAO, IPromotionDAO promotionDAO, IQueueopDAO queueopDAO) {
        this.waitinginstanceinfoDAO = waitinginstanceinfoDAO;
        this.orderRefundDAO = orderRefundDAO;
        this.instanceAssetDAO = instanceAssetDAO;
        this.orderTagDAO = orderTagDAO;
        this.paydetailDAO = paydetailDAO;
        this.payinfoDAO = payinfoDAO;
        this.waitingordercridDAO = waitingordercridDAO;
        this.waitingPayDAO = waitingPayDAO;
        this.discountDetailDAO = discountDetailDAO;
        this.orderPromotionDAO = orderPromotionDAO;
        this.queuestatusDAO = queuestatusDAO;
        this.orderBillDAO = orderBillDAO;
        this.orderSnapshotDAO = orderSnapshotDAO;
        this.instancedetailDAO = instancedetailDAO;
        this.customerOrderRelationDAO = customerOrderRelationDAO;
        this.totalpayinfoDAO = totalpayinfoDAO;
        this.presellOrderExtraDAO = presellOrderExtraDAO;
        this.specialfeeDAO = specialfeeDAO;
        this.refundPayItemDAO = refundPayItemDAO;
        this.takeoutOrderExtraDAO = takeoutOrderExtraDAO;
        this.waitingorderdetailDAO = waitingorderdetailDAO;
        this.gridFieldDAO = gridFieldDAO;
        this.servicebillinfoDAO = servicebillinfoDAO;
        this.simplecodeorderDAO = simplecodeorderDAO;
        this.userDAO = userDAO;
        this.globalcodeorderDAO = globalcodeorderDAO;
        this.orderdetailDAO = orderdetailDAO;
        this.promotionDAO = promotionDAO;
        this.queueopDAO = queueopDAO;
    }
}
