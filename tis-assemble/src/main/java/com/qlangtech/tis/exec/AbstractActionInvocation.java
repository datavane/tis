/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.exec;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.exec.impl.DumpInterceptor;
import com.qlangtech.tis.exec.impl.IndexBackFlowInterceptor;
import com.qlangtech.tis.exec.impl.IndexBuildInterceptor;
import com.qlangtech.tis.exec.impl.IndexBuildWithHdfsPathInterceptor;
import com.qlangtech.tis.exec.impl.TableJoinInterceptor;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AbstractActionInvocation implements ActionInvocation {

    public static final String COMMAND_KEY_DIRECTBUILD = "directbuild";

    private static final Logger logger = LoggerFactory.getLogger(AbstractActionInvocation.class);

    // 原生的全流程構建，從dump開始到最後索引回流
    private static final IExecuteInterceptor[] fullints = new IExecuteInterceptor[] { // new BindTablePartitionInterceptor(),
    new DumpInterceptor(), // //
    new TableJoinInterceptor(), // ////////
    new IndexBuildInterceptor(), new IndexBackFlowInterceptor() };

    // 由数据中心触发的直接進入索引build階段
    private static final IExecuteInterceptor[] directBuild = new IExecuteInterceptor[] { // /////
    new IndexBuildWithHdfsPathInterceptor(), new IndexBackFlowInterceptor() };

    /**
     * 创建执行链
     *
     * @param chainContext
     * @return
     */
    public static ActionInvocation createExecChain(IExecChainContext chainContext) {
        final Map<String, Integer> /* index */
        componentOrders = new HashMap<String, Integer>();
        IExecuteInterceptor[] ints = null;
        if ("true".equalsIgnoreCase(chainContext.getString(COMMAND_KEY_DIRECTBUILD))) {
            ints = directBuild;
        } else {
            ints = fullints;
        }
        AbstractActionInvocation preInvocation = new AbstractActionInvocation();
        preInvocation.setContext(chainContext);
        preInvocation.setComponentOrders(componentOrders);
        AbstractActionInvocation invocation = null;
        for (int i = (ints.length - 1); i >= 0; i--) {
            componentOrders.put(ints[i].getName(), i);
            invocation = new AbstractActionInvocation();
            invocation.setComponentOrders(componentOrders);
            invocation.setContext(chainContext);
            invocation.setInterceptor(ints[i]);
            invocation.setSuccessor(preInvocation);
            preInvocation = invocation;
        }
        logger.info("component description:");
        for (Map.Entry<String, Integer> componentEntry : componentOrders.entrySet()) {
            logger.info(componentEntry.getKey() + ":" + componentEntry.getValue());
        }
        logger.info("=====================description end");
        return preInvocation;
    }

    // = new HashMap<String,
    private Map<String, Integer> componentOrders;

    // Integer>();
    @Override
    public ExecuteResult invoke() throws Exception {
        if (componentOrders == null) {
            throw new IllegalStateException("componentOrders can not be null");
        }
        String start = chainContext.getString(IndexSwapTaskflowLauncher.COMPONENT_START);
        String end = chainContext.getString(IndexSwapTaskflowLauncher.COMPONENT_END);
        int startIndex = Integer.MIN_VALUE;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = componentOrders.get(start);
        } catch (Throwable e) {
        }
        try {
            endIndex = componentOrders.get(end);
        } catch (Throwable e) {
        }
        if (interceptor == null) {
            // return chainContext.execute();
            return ExecuteResult.SUCCESS;
        } else {
            int current;
            try {
                current = componentOrders.get(interceptor.getName());
            } catch (Throwable e) {
                throw new IllegalStateException("component:" + interceptor.getName() + " can not find value in componentOrders," + componentOrders.toString());
            }
            if (current >= startIndex && current <= endIndex) {
                logger.info("execute " + interceptor.getName() + ":" + current + "[" + startIndex + "," + endIndex + "]");
                return interceptor.intercept(successor);
            } else {
                // 直接跳过
                return successor.invoke();
            }
        }
    }

    public Map<String, Integer> getComponentOrders() {
        return componentOrders;
    }

    public void setComponentOrders(Map<String, Integer> componentOrders) {
        this.componentOrders = componentOrders;
    }

    // public static void main(String[] arg) throws Exception {
    // 
    // BasicModule action = new BasicModule() {
    // private static final long serialVersionUID = 1L;
    // 
    // @Override
    // public String execute() throws Exception {
    // System.out.println("BasicModule exec");
    // return "basicModule";
    // }
    // };
    // 
    // AbstractActionInvocation actionInvoc = new AbstractActionInvocation();
    // actionInvoc.setAction(action);
    // 
    // Interceptor inter = new Interceptor() {
    // @Override
    // public String intercept(ActionInvocation invocation)
    // throws Exception {
    // try {
    // return "interc1";
    // // return invocation.invoke();
    // } finally {
    // System.out.println("post interc1");
    // }
    // }
    // };
    // 
    // AbstractActionInvocation actionInvoc2 = new AbstractActionInvocation();
    // actionInvoc2.setAction(action);
    // actionInvoc2.setInterceptor(inter);
    // actionInvoc2.setSuccessor(actionInvoc);
    // 
    // System.out.println("forward:" + actionInvoc2.invoke());
    // // action.execute();
    // }
    @Override
    public IExecChainContext getContext() {
        return this.chainContext;
    }

    private IExecChainContext chainContext;

    private IExecuteInterceptor interceptor;

    private ActionInvocation successor;

    public void setSuccessor(ActionInvocation successor) {
        this.successor = successor;
    }

    public IExecuteInterceptor getSuccessor() {
        return interceptor;
    }

    public void setInterceptor(IExecuteInterceptor successor) {
        this.interceptor = successor;
    }

    // @Override
    // public IExecChainContext getAction() {
    // return this.chainContext;
    // }
    public void setContext(IExecChainContext action) {
        this.chainContext = action;
    }
}
