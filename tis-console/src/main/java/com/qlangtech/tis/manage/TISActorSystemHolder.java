///**
// *   Licensed to the Apache Software Foundation (ASF) under one
// *   or more contributor license agreements.  See the NOTICE file
// *   distributed with this work for additional information
// *   regarding copyright ownership.  The ASF licenses this file
// *   to you under the Apache License, Version 2.0 (the
// *   "License"); you may not use this file except in compliance
// *   with the License.  You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//package com.qlangtech.tis.manage;
//
//import akka.actor.ActorRef;
//import com.qlangtech.tis.dag.TISActorSystem;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.ServletContext;
//
///**
// * TIS Actor System 持有者
// * 提供全局访问 TISActorSystem 实例的便捷方法
// *
// * 使用场景：
// * - Controller 中触发工作流执行
// * - Service 中查询工作流状态
// * - 定时任务中触发工作流
// *
// * 使用示例：
// * <pre>
// * // 获取 WorkflowInstance Sharding Region
// * ActorRef workflowRegion = TISActorSystemHolder.getWorkflowInstanceRegion(servletContext);
// *
// * // 发送 StartWorkflow 消息
// * StartWorkflow msg = new StartWorkflow(instanceId, dataXName);
// * workflowRegion.tell(msg, ActorRef.noSender());
// * </pre>
// *
// * @author 百岁（baisui@qlangtech.com）
// * @date 2026-01-30
//// */
//public class TISActorSystemHolder {
//
//    private static final Logger logger = LoggerFactory.getLogger(TISActorSystemHolder.class);
//
//   /**
//     * 获取 TISActorSystem 实例
//     *
//     * @param servletContext ServletContext
//     * @return TISActorSystem 实例，如果未初始化则返回 null
//     */
//    public static TISActorSystem getTISActorSystem(ServletContext servletContext) {
//        if (servletContext == null) {
//            logger.warn("ServletContext is null, cannot get TISActorSystem");
//            return null;
//        }
//
//        Object attr = servletContext.getAttribute(TISActorSystem.ATTR_TIS_ACTOR_SYSTEM);
//        if (attr instanceof TISActorSystem) {
//            return (TISActorSystem) attr;
//        }
//
//        logger.warn("TISActorSystem not found in ServletContext");
//        return null;
//    }
//
//  /**
//     * 获取 DAGMonitorActor
//     *
//     * @param servletContext ServletContext
//     * @return DAGMonitorActor 引用，如果未初始化则返回 null
//     */
//    public static ActorRef getDagMonitorActor(ServletContext servletContext) {
//        TISActorSystem actorSystem = getTISActorSystem(servletContext);
//        if (actorSystem != null) {
//            return actorSystem.getDagMonitorActor();
//        }
//        return null;
//    }
//
//    /**
//     * 获取 ClusterManagerActor
//     *
//     * @param servletContext ServletContext
//     * @return ClusterManagerActor 引用，如果未初始化则返回 null
//     */
//    public static ActorRef getClusterManagerActor(ServletContext servletContext) {
//        TISActorSystem actorSystem = getTISActorSystem(servletContext);
//        if (actorSystem != null) {
//            return actorSystem.getClusterManagerActor();
//        }
//        return null;
//    }
//
//    /**
//     * 获取 NodeDispatcherActor
//     *
//     * @param servletContext ServletContext
//     * @return NodeDispatcherActor 引用，如果未初始化则返回 null
//     */
//    public static ActorRef getNodeDispatcherActor(ServletContext servletContext) {
//        TISActorSystem actorSystem = getTISActorSystem(servletContext);
//        if (actorSystem != null) {
//            return actorSystem.getNodeDispatcherActor();
//        }
//        return null;
//    }
//
//    /**
//     * 检查 TISActorSystem 是否已初始化
//     *
//     * @param servletContext ServletContext
//     * @return true 如果已初始化
//     */
//    public static boolean isInitialized(ServletContext servletContext) {
//        TISActorSystem actorSystem = getTISActorSystem(servletContext);
//        return actorSystem != null && actorSystem.isInitialized();
//    }
//
//    /**
//     * 检查 TISActorSystem 是否正在运行
//     *
//     * @param servletContext ServletContext
//     * @return true 如果正在运行
//     */
//    public static boolean isRunning(ServletContext servletContext) {
//        TISActorSystem actorSystem = getTISActorSystem(servletContext);
//        return actorSystem != null && actorSystem.isRunning();
//    }
//
//    /**
//     * 获取 WorkflowInstance Sharding Region
//     * 用于直接向WorkflowInstanceActor发送消息
//     *
//     * @param servletContext ServletContext
//     * @return WorkflowInstance Sharding Region 引用，如果未初始化则返回 null
//     */
//    public static ActorRef getWorkflowInstanceRegion(ServletContext servletContext) {
//        TISActorSystem actorSystem = getTISActorSystem(servletContext);
//        if (actorSystem != null) {
//            return actorSystem.getWorkflowInstanceRegion();
//        }
//        return null;
//    }
//}
