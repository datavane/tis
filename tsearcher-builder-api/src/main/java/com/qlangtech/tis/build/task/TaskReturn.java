package com.qlangtech.tis.build.task;

public class TaskReturn {

	private String msg;
	private ReturnCode returnCode;

	public TaskReturn(ReturnCode returnCode, String msg) {
		this.returnCode = returnCode;
		this.msg = msg;
	}

	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}

	public enum ReturnCode {
		SUCCESS, FAILURE
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
