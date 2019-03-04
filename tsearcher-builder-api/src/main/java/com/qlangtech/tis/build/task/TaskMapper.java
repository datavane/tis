package com.qlangtech.tis.build.task;

import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;

public interface TaskMapper {
	public TaskReturn map(TaskContext context);
}
