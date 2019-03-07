/**
 * 
 */
package com.qlangtech.tis.manage.common.trigger;

import com.alibaba.fastjson.JSON;

/**
 * @author 百岁（baisui@taobao.com）
 * @date 2014年10月30日下午12:02:48
 */
public class TriggerTaskConfig extends SourceType {

	/**
	 * 序列化
	 * 
	 * @param config
	 * @return
	 */
	public static String serialize(TriggerTaskConfig config) {
		JSON json = (JSON) JSON.toJSON(config);
		return json.toJSONString();
	}

	/**
	 * 解析一个对象
	 * 
	 * @param value
	 * @return
	 */
	public static TriggerTaskConfig parse(String value) {
		SourceType type = JSON.parseObject(value, SourceType.class);
		return JSON.parseObject(value, TriggerTaskConfig.class);
	}

	public final String getAppName() {
		return appName;
	}

	public final void setAppName(String appName) {
		this.appName = appName;
	}

	private int taskid;
	protected String appName;
	private Long maxDumpCount;

	/**
	 * 
	 */
	public TriggerTaskConfig() {
		super();
	}

	public int getTaskId() {
		return taskid;
	}

	public void setTaskId(int taskId) {
		this.taskid = taskId;
	}

	public Long getMaxDumpCount() {
		return maxDumpCount;
	}

	public void setMaxDumpCount(Long maxDumpCount) {
		this.maxDumpCount = maxDumpCount;
	}

}