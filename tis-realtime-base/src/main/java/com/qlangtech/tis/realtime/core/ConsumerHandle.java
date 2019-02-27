package com.qlangtech.tis.realtime.core;

/**
 * 消息处理Handle Created by binggun on 2015/6/8 INFO:这个方法使用于 ONS
 */
public abstract class ConsumerHandle {

	// 支持 || 关系
	private String subExpression;

	private String topic;

	public String getTopic() {
		return this.topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	/**
	 * 处理消息Handle，业务逻辑在此处理
	 *
	 * @param message
	 * @return true:处理成功;false:处理失败，重新投递
	 */
	public abstract boolean consume(AsyncMsg message);

	public String getSubExpression() {
		return subExpression;
	}

	public void setSubExpression(String subExpression) {
		this.subExpression = subExpression;
	}
}
