/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.workflow.pojo;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * DAG 文件管理器
 * 负责 DAG 定义文件的读写和版本管理
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class WorkflowDAGFileManager {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDAGFileManager.class);

    /**
     * DAG 定义文件名
     */
    public static final String DAG_SPEC_FILENAME = "dag-spec.json";

    /**
     * 工作流基础目录
     */
    private final File workflowBaseDir;

    /**
     * 是否启用 Git 版本管理
     */
    private final boolean enableGitVersion;

    //    /**
    //     * 构造函数（使用默认配置）
    //     */
    //    public WorkflowDAGFileManager() {
    //        this(getDefaultWorkflowDir(), false);
    //    }

    /**
     * 构造函数
     *
     * @param workflowBaseDir  工作流基础目录
     * @param enableGitVersion 是否启用 Git 版本管理
     */
    public WorkflowDAGFileManager(File workflowBaseDir, boolean enableGitVersion) {
        this.workflowBaseDir = Objects.requireNonNull(workflowBaseDir, "workflowBaseDir cannot be null");
        this.enableGitVersion = enableGitVersion;

        // 确保基础目录存在
        if (!workflowBaseDir.exists()) {
            boolean created = workflowBaseDir.mkdirs();
            if (!created) {
                throw new IllegalStateException("Failed to create workflow base directory: " + workflowBaseDir.getAbsolutePath());
            }
        }
    }

    /**
     * 保存 DAG 定义到文件系统
     *
     * @param workflowName 工作流名称
     * @param dag          DAG 定义
     * @return DAG 文件相对路径
     */
    public synchronized File saveDagSpec(String workflowName, PEWorkflowDAG dag) {
        if (StringUtils.isEmpty(workflowName)) {
            throw new IllegalArgumentException("workflowName cannot be empty");
        }
        if (dag == null) {
            throw new IllegalArgumentException("dag cannot be null");
        }

        try {
            // 创建工作流目录
            File workflowDir = workflowBaseDir;// new File(workflowBaseDir, workflowName);
            if (!workflowDir.exists()) {
                //                boolean created = workflowDir.mkdirs();
                //                if (!created) {
                //                    throw new IllegalStateException("Failed to create workflow directory: " +
                //                    workflowDir.getAbsolutePath());
                //                }
                logger.info("Created workflow directory: {}", workflowDir.getAbsolutePath());
            }

            // 序列化 DAG 为 JSON
            String jsonContent = JSON.toJSONString(dag, true);

            // 原子写入：先写临时文件，再重命名
            File dagFile = new File(workflowDir, DAG_SPEC_FILENAME);
            File tempFile = new File(workflowDir, DAG_SPEC_FILENAME + ".tmp");

            FileUtils.write(tempFile, jsonContent, StandardCharsets.UTF_8);

            // 重命名为正式文件
            if (dagFile.exists()) {
                boolean deleted = dagFile.delete();
                if (!deleted) {
                    logger.warn("Failed to delete old DAG file: {}", dagFile.getAbsolutePath());
                }
            }

            boolean renamed = tempFile.renameTo(dagFile);
            if (!renamed) {
                throw new IOException("Failed to rename temp file to DAG file");
            }

            logger.info("Saved DAG spec: workflow={}, path={}", workflowName, dagFile.getAbsolutePath());

            // Git 版本管理（可选）
            if (enableGitVersion) {
                commitDagSpec(workflowName, "Update DAG spec");
            }

            // 返回相对路径
            return dagFile;// workflowName + "/" + DAG_SPEC_FILENAME;

        } catch (IOException e) {
            logger.error("Failed to save DAG spec: workflowName={}", workflowName, e);
            throw new IllegalStateException("Failed to save DAG spec file", e);
        }
    }

    /**
     * 从文件系统加载 DAG 定义
     *
     * @param dagSpecPath 相对路径（如：workflow-name/dag-spec.json）
     * @return DAG 定义
     */
    public PEWorkflowDAG loadDagSpec(String dagSpecPath) {
        if (StringUtils.isEmpty(dagSpecPath)) {
            throw new IllegalArgumentException("dagSpecPath cannot be empty");
        }

        try {
            File dagFile = new File(workflowBaseDir, dagSpecPath);

            if (!dagFile.exists()) {
                throw new IllegalStateException("DAG spec file not found: " + dagFile.getAbsolutePath());
            }

            String jsonContent = FileUtils.readFileToString(dagFile, StandardCharsets.UTF_8);

            PEWorkflowDAG dag = JSON.parseObject(jsonContent, PEWorkflowDAG.class);

            logger.debug("Loaded DAG spec: path={}", dagSpecPath);

            return dag;

        } catch (IOException e) {
            logger.error("Failed to load DAG spec: dagSpecPath={}", dagSpecPath, e);
            throw new IllegalStateException("Failed to load DAG spec file", e);
        }
    }

    /**
     * 获取工作流目录
     *
     * @param workflowName 工作流名称
     * @return 工作流目录
     */
    public File getWorkflowDir(String workflowName) {
        if (StringUtils.isEmpty(workflowName)) {
            throw new IllegalArgumentException("workflowName cannot be empty");
        }
        return new File(workflowBaseDir, workflowName);
    }

    /**
     * 检查 DAG 文件是否存在
     *
     * @param dagSpecPath 相对路径
     * @return 是否存在
     */
    public boolean exists(String dagSpecPath) {
        if (StringUtils.isEmpty(dagSpecPath)) {
            return false;
        }
        File dagFile = new File(workflowBaseDir, dagSpecPath);
        return dagFile.exists();
    }

    /**
     * 删除 DAG 文件
     *
     * @param dagSpecPath 相对路径
     */
    public void deleteDagSpec(String dagSpecPath) {
        if (StringUtils.isEmpty(dagSpecPath)) {
            return;
        }

        try {
            File dagFile = new File(workflowBaseDir, dagSpecPath);
            if (dagFile.exists()) {
                boolean deleted = dagFile.delete();
                if (deleted) {
                    logger.info("Deleted DAG spec file: {}", dagFile.getAbsolutePath());
                } else {
                    logger.warn("Failed to delete DAG spec file: {}", dagFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete DAG spec file: {}", dagSpecPath, e);
            throw new IllegalStateException("Failed to delete DAG spec file", e);
        }
    }

    /**
     * 提交 DAG 变更到 Git 仓库（可选功能）
     *
     * @param workflowName  工作流名称
     * @param commitMessage 提交信息
     */
    public void commitDagSpec(String workflowName, String commitMessage) {
        if (!enableGitVersion) {
            logger.debug("Git version control is disabled, skip commit");
            return;
        }

        // TODO: 实现 Git 提交功能
        // 可以使用 JGit 库或者执行 git 命令
        logger.info("Git commit: workflow={}, message={}", workflowName, commitMessage);
    }

    //    /**
    //     * 获取默认工作流目录
    //     *
    //     * @return 工作流目录
    //     */
    //    private static File getDefaultWorkflowDir() {
    //        File dataDir = Config.getDataDir();// TIS.get().getDataDir();
    //        return new File(dataDir, "workflow");
    //    }

    /**
     * 获取工作流基础目录
     *
     * @return 工作流基础目录
     */
    public File getWorkflowBaseDir() {
        return workflowBaseDir;
    }

    /**
     * 是否启用 Git 版本管理
     *
     * @return 是否启用
     */
    public boolean isEnableGitVersion() {
        return enableGitVersion;
    }
}
