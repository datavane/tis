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
package com.qlangtech.tis.fullbuild;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年8月20日
 */
public interface IFullBuildContext {

    // 索引分区数目
    String KEY_APP_SHARD_COUNT = "index_shard_count";

    String KEY_APP_SHARD_COUNT_SINGLE = "1";

    String KEY_APP_NAME = "appname";

    String KEY_WORKFLOW_ID = "workflow_id";

    String KEY_WORKFLOW_NAME = "workflow_name";

    String KEY_ER_RULES = "er_rules";

    // com.qlangtech.tis.assemble.FullbuildPhase
    // String COMPONENT_START = "component.start";
    // String COMPONENT_END = "component.end";
    // String KEY_WORKFLOW_DETAIL = "workflowDetail";
    // 定时或者手动？
    String KEY_TRIGGER_TYPE = "triggertype";

    String KEY_BUILD_HISTORY_TASK_ID = "history.task.id";
    String NAME_APP_DIR = "ap";
}
