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
package com.qlangtech.tis.realtime.s4totalpay;

import com.google.common.collect.Sets;
import com.qlangtech.tis.realtime.BasicBeanGroup;
import com.qlangtech.tis.realtime.BasicIncrTestCase;
import com.qlangtech.tis.realtime.PojoCUD;
import com.qlangtech.tis.realtime.test.member.dao.IMemberDAOFacade;
import com.qlangtech.tis.realtime.test.member.pojo.Card;
import com.qlangtech.tis.realtime.test.member.pojo.Customer;
import com.qlangtech.tis.realtime.test.order.dao.IOrderDAOFacade;
import com.qlangtech.tis.realtime.test.order.pojo.*;
import com.qlangtech.tis.realtime.test.shop.dao.IShopDAOFacade;
import com.qlangtech.tis.realtime.test.shop.pojo.MallShop;
import com.qlangtech.tis.realtime.transfer.BasicRMListener;
import com.qlangtech.tis.spring.LauncherResourceUtils;

import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class AbstractTestS4totalpayIncr extends BasicIncrTestCase {

    public static final String TAB_ORDERINFO = "orderdetail";

    public static final String TAB_INSTANCE = "instancedetail";

    public static final String TAB_TOTALPAY = "totalpayinfo";

    public static final String TAB_CARD = "card";

    public static final String TAB_CUSTOMER = "customer";

    public static final String TAB_MALL_SHOP = "mall_shop";

    public static final String TAB_SPECIALFEE = "specialfee";

    public static final String TAB_ORDER_BILL = "order_bill";

    public static final String TAB_TAKEOUT_ORDER_EXTRA = "takeout_order_extra";

    public static final String TAB_PAYINFO = "payinfo";

    //  protected static BasicRMListener listenerBean = null;

    // protected ApplicationContext s4totalpayContext;

    protected IOrderDAOFacade orderDAOFacade;

    private IMemberDAOFacade memberDAOFacade;

    private IShopDAOFacade shopDAOFacade;

    private static final long wfTimestamp = 20190820171040l;

    protected static final String KEY_MODIFY_TIME = "modify_time";

    private static final Set<String> includeSpringContext;


    static {
        includeSpringContext = Sets.newHashSet();
        includeSpringContext.add("employees-dao-context.xml");
        LauncherResourceUtils.resourceFilter = (res) -> {
            return !includeSpringContext.contains(res.getFilename());
        };
    }


    public AbstractTestS4totalpayIncr(boolean shallRegisterMQ) {
//boolean shallRegisterMQ,String collectionName ,long wfTimestamp ,String... configLocations
        super(shallRegisterMQ, COLLECTION_search4totalpay, wfTimestamp
                , "/conf/order-test-dao-context.xml", "/conf/member-test-dao-context.xml", "/conf/shop-test-dao-context.xml");
        // com.qlangtech.tis.realtime.transfer.MQListenerFactory 中不会启动mq去读mq的消息
//        Config.setTest(!shallRegisterMQ);
//        AbstractTisCloudSolrClient.initHashcodeRouter();
//        try {
//            final TisIncrLauncher incrLauncher = new TisIncrLauncher(COLLECTION_search4totalpay, wfTimestamp, true);
//            incrLauncher.downloadDependencyJarsAndPlugins();
//            // 启动增量任务
//            BeanFactory incrContainer = incrLauncher.launchIncrChannel();
//            listenerBean = incrContainer.getBean(BasicRMListener.class);
//            assertNotNull(listenerBean);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        s4totalpayContext = new // /
//        ClassPathXmlApplicationContext("/conf/order-test-dao-context.xml", "/conf/member-test-dao-context.xml", "/conf/shop-test-dao-context.xml") {
//
//            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
//                DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
//                // DataSourceRegister.setApplicationContext(factory,
//                // dbMetaList);
//                DBNode.registerDependencyDbsFacadeConfig(COLLECTION_search4totalpay, wfTimestamp, factory);
//                // SpringDBRegister dbRegister = new SpringDBRegister(dbLinkMetaData.getName(), dbLinkMetaData, factory);
//                // dbRegister.visitAll();
//                // registerExtraBeanDefinition(factory);
//                super.prepareBeanFactory(beanFactory);
//            }
//        };
        orderDAOFacade = appContext.getBean("orderDAOFacade", IOrderDAOFacade.class);
        memberDAOFacade = appContext.getBean("memberDAOFacade", IMemberDAOFacade.class);
        shopDAOFacade = appContext.getBean("shopDAOFacade", IShopDAOFacade.class);
    }

    protected class BeanGroup extends BasicBeanGroup {

        protected DTO<Specialfee> specialfee;

        protected DTO<MallShop> shopPojo;

        protected DTO<Card> card;

        protected DTO<Customer> custm;

        protected DTO<Payinfo> payinfo1;

        protected DTO<Payinfo> payinfo2;

        protected DTO<TakeoutOrderExtra> takeoutOrderExtra;

        protected DTO<OrderBill> orderBill1;

        protected DTO<Instancedetail> instance1;

        protected DTO<Instancedetail> instance2;

        protected DTO<Totalpayinfo> totalpayinfo;

        protected DTO<Orderdetail> orderdetail;

        // private final IClassAwareCallback updateCallback;
        // 
        // public BeanGroup(IClassAwareCallback updateCallback) {
        // this.updateCallback = updateCallback;
        // }
        public DTO<Specialfee> getSpecialfee() {
            return specialfee;
        }

        public DTO<MallShop> getShopPojo() {
            return shopPojo;
        }

        public DTO<Card> getCard() {
            return card;
        }

        public DTO<Customer> getCustm() {
            return custm;
        }

        public DTO<Payinfo> getPayinfo1() {
            return payinfo1;
        }

        public DTO<Payinfo> getPayinfo2() {
            return payinfo2;
        }

        public DTO<TakeoutOrderExtra> getTakeoutOrderExtra() {
            return takeoutOrderExtra;
        }

        public DTO<OrderBill> getOrderBill1() {
            return orderBill1;
        }

        public DTO<Instancedetail> getInstance1() {
            return instance1;
        }

        public DTO<Instancedetail> getInstance2() {
            return instance2;
        }

        public BeanGroup(BasicRMListener listenerBean) {
            super(listenerBean);
        }

//        protected <T> DTO<T> getBean(String path, Class<T> pojoClazz, PojoCUD<T> crud) {
//            DefaultRowValueGetter junitValsExample = deserializeBean(crud.getTableName(), getTableRowProcessor(crud.getTableName()), path);
//            DefaultRowValueGetter vals = deserializeBean(crud.getTableName(), null, /*** 不需要处理 */path);
//            T pojo = DTO.pojo(vals, pojoClazz);
//            crud.initSyncWithDB(pojo);
//            return new DTO<>(junitValsExample, vals, pojoClazz, pojo, crud);
//        }

        public BeanGroup invoke() {
            this.totalpayinfo = this.getBean("totalpayinfo_1.txt", Totalpayinfo.class, new PojoCUD<Totalpayinfo>() {
                @Override
                public String getTableName() {
                    return "totalpayinfo";
                }

                @Override
                public void initSyncWithDB(Totalpayinfo pojo) {
                }

                @Override
                public void updateByExampleSelective(Totalpayinfo newPojo, Totalpayinfo oldPojo) {
                }
            });
            this.orderdetail = getBean("orderdetail_1.txt", Orderdetail.class, new PojoCUD<Orderdetail>() {
                @Override
                public String getTableName() {
                    return "orderdetail";
                }

                @Override
                public void initSyncWithDB(Orderdetail pojo) {
                }

                @Override
                public void updateByExampleSelective(Orderdetail newPojo, Orderdetail oldPojo) {
                }
            });
            specialfee = getBean("specialfee_1.txt", Specialfee.class, new PojoCUD<Specialfee>() {
                @Override
                public String getTableName() {
                    return "specialfee";
                }

                @Override
                public void initSyncWithDB(Specialfee pojo) {
                    orderDAOFacade.getSpecialfeeDAO().deleteByPrimaryKey(pojo.getSpecialfeeId());
                    orderDAOFacade.getSpecialfeeDAO().insertSelective(pojo);
                }

                @Override
                public void updateByExampleSelective(Specialfee pojo, Specialfee oldPojo) {
                    SpecialfeeCriteria c = new SpecialfeeCriteria();
                    c.createCriteria().andSpecialfeeIdEqualTo(oldPojo.getSpecialfeeId());
                    orderDAOFacade.getSpecialfeeDAO().updateByExampleSelective(pojo, c);
                }
            });
            // orderDAOFacade.getSpecialfeeDAO().updateByExampleSelective()
            shopPojo = this.getBean("mall_shop_1.txt", MallShop.class, new PojoCUD<MallShop>() {
                @Override
                public String getTableName() {
                    return "mall_shop";
                }

                @Override
                public void initSyncWithDB(MallShop pojo) {
                    shopDAOFacade.getMallShopDAO().deleteByPrimaryKey(pojo.getId());
                    shopDAOFacade.getMallShopDAO().insertSelective(pojo);
                }

                @Override
                public void updateByExampleSelective(MallShop pojo, MallShop oldPojo) {
                    throw new UnsupportedOperationException();
                }
            });
            // DefaultRowValueGetter mallShop = deserializeBean("mall_shop_1.txt");
            // MallShop shopPojo = pojo(mallShop, MallShop.class);
            card = this.getBean("card_1.txt", Card.class, new PojoCUD<Card>() {
                @Override
                public String getTableName() {
                    return "card";
                }

                @Override
                public void initSyncWithDB(Card pojo) {
                    memberDAOFacade.getCardDAO().deleteByPrimaryKey(pojo.getId());
                    memberDAOFacade.getCardDAO().insertSelective(pojo);
                }

                @Override
                public void updateByExampleSelective(Card pojo, Card old) {
                    throw new UnsupportedOperationException();
                }
            });
            // DefaultRowValueGetter card = deserializeBean("card_1.txt");
            // Card c = pojo(card, Card.class);
            custm = this.getBean("customer_1.txt", Customer.class, new PojoCUD<Customer>() {
                @Override
                public String getTableName() {
                    return "customer";
                }

                @Override
                public void initSyncWithDB(Customer pojo) {
                    memberDAOFacade.getCustomerDAO().deleteByPrimaryKey(pojo.getId());
                    memberDAOFacade.getCustomerDAO().insertSelective(pojo);
                }

                @Override
                public void updateByExampleSelective(Customer pojo, Customer oldPojo) {
                    throw new UnsupportedOperationException();
                }
            });
            // DefaultRowValueGetter customer = deserializeBean("customer_1.txt");
            // Customer custm = pojo(customer, Customer.class);
            PojoCUD<Payinfo> payinfoCUD = new PojoCUD<Payinfo>() {
                @Override
                public String getTableName() {
                    return "payinfo";
                }

                @Override
                public void initSyncWithDB(Payinfo pojo) {
                    orderDAOFacade.getPayinfoDAO().deleteByPrimaryKey(pojo.getPayId());
                    orderDAOFacade.getPayinfoDAO().insertSelective(pojo);
                }

                @Override
                public void updateByExampleSelective(Payinfo pojo, Payinfo oldPojo) {
                    assertNotNull(pojo.getCouponCost());
                    assertNotNull(pojo.getCouponFee());
                    assertNotNull(pojo.getCouponNum());
                    PayinfoCriteria c = new PayinfoCriteria();
                    c.createCriteria().andPayIdEqualTo(oldPojo.getPayId());
                    orderDAOFacade.getPayinfoDAO().updateByExampleSelective(pojo, c);
                }
            };
            payinfo1 = getBean("payinfo_1.txt", Payinfo.class, payinfoCUD);
            payinfo2 = getBean("payinfo_2.txt", Payinfo.class, payinfoCUD);
            // TestS4totalpayIncr.this.orderDAOFacade.getPayinfoDAO().insertSelective(payinfo2.pojo);
            takeoutOrderExtra = getBean("takeout_order_extra_1.txt", TakeoutOrderExtra.class, new PojoCUD<TakeoutOrderExtra>() {
                @Override
                public String getTableName() {
                    return "takeout_order_extra";
                }

                @Override
                public void initSyncWithDB(TakeoutOrderExtra pojo) {
                }

                @Override
                public void updateByExampleSelective(TakeoutOrderExtra pojo, TakeoutOrderExtra old) {
                }
            });
            orderBill1 = getBean("order_bill_1.txt", OrderBill.class, new PojoCUD<OrderBill>() {

                @Override
                public String getTableName() {
                    return "order_bill";
                }

                @Override
                public void initSyncWithDB(OrderBill pojo) {
                }

                @Override
                public void updateByExampleSelective(OrderBill pojo, OrderBill old) {
                }
            });
            PojoCUD<Instancedetail> instanceCUD = new PojoCUD<Instancedetail>() {

                @Override
                public String getTableName() {
                    return "instancedetail";
                }

                @Override
                public void initSyncWithDB(Instancedetail pojo) {
                    orderDAOFacade.getInstancedetailDAO().deleteByPrimaryKey(pojo.getInstanceId());
                    orderDAOFacade.getInstancedetailDAO().insertSelective(pojo);
                }

                @Override
                public void updateByExampleSelective(Instancedetail pojo, Instancedetail old) {
                    InstancedetailCriteria c = new InstancedetailCriteria();
                    c.createCriteria().andInstanceIdEqualTo(old.getInstanceId());
                    orderDAOFacade.getInstancedetailDAO().updateByExampleSelective(pojo, c);
                }
            };
            instance1 = getBean("instancedetail_1_1.txt", Instancedetail.class, instanceCUD);
            instance2 = getBean("instancedetail_1_2.txt", Instancedetail.class, instanceCUD);
            return this;
        }
    }
}
