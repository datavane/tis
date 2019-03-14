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
package com.qlangtech.tis.manage.yarn;

import java.util.Date;

import com.qlangtech.tis.manage.common.ManageUtils;

public class ApplicationReportStatus {

	// "id": "application_1552283947750_0013",
	// "user": "spring-boot",
	// "name": "search4shop-0-indexbuild",
	// "queue": "default",
	// "state": "FINISHED",
	// "finalStatus": "SUCCEEDED",
	// "progress": 100.0,
	// "trackingUI": "History",
	// "trackingUrl":
	// "http://yarn-master:8088/proxy/application_1552283947750_0013/",
	// "diagnostics": "",
	// "clusterId": 1552283947750,
	// "applicationType": "YARN",
	// "applicationTags": "",
	// "startedTime": 1552299351786,
	// "finishedTime": 1552299379629,
	// "elapsedTime": 27843,
	// "amContainerLogs":
	// "http://yarn-1:8042/node/containerlogs/container_1552283947750_0013_01_000001/spring-boot",
	// "amHostHttpAddress": "yarn-1:8042",
	// "allocatedMB": -1,
	// "allocatedVCores": -1,
	// "runningContainers": -1,
	// "memorySeconds": 361005,
	// "vcoreSeconds": 88,
	// "preemptedResourceMB": 0,
	// "preemptedResourceVCores": 0,
	// "numNonAMContainerPreempted": 0,
	// "numAMContainerPreempted": 0

	private String id;// : "application_1552283947750_0013",
	private String user; // "user": "spring-boot",
	private String name;// "name": "search4shop-0-indexbuild",
	private String queue; // "queue": "default",
	private String state;// "FINISHED",
	private String finalStatus;// "SUCCEEDED",
	private float progress;// 100.0,
	private String trackingUI;// ": "History",
	private String trackingUrl;// :
								// "http://yarn-master:8088/proxy/application_1552283947750_0013/",
	private String diagnostics;// ": "",
	private long clusterId;// ": 1552283947750,
	private String applicationType;// : "YARN",
	// private String "applicationTags": "",
	private long startedTime;// : 1552299351786,
	private long finishedTime;// ": 1552299379629,
	private int elapsedTime; // ": 27843,
	private String amContainerLogs;// ":
									// "http://yarn-1:8042/node/containerlogs/container_1552283947750_0013_01_000001/spring-boot",
	private String amHostHttpAddress;// ": "yarn-1:8042",
	private int allocatedMB;// ": -1,
	private int allocatedVCores;// ": -1,
	private int runningContainers;// ": -1,
	private int memorySeconds;// ": 361005,
	private int vcoreSeconds;// ": 88,
	private int preemptedResourceMB;// ": 0,
	private int preemptedResourceVCores;// ": 0,
	// "numNonAMContainerPreempted": 0,
	// "numAMContainerPreempted": 0

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public String getTrackingUI() {
		return trackingUI;
	}

	public void setTrackingUI(String trackingUI) {
		this.trackingUI = trackingUI;
	}

	public String getTrackingUrl() {
		return trackingUrl;
	}

	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}

	public String getDiagnostics() {
		return diagnostics;
	}

	public void setDiagnostics(String diagnostics) {
		this.diagnostics = diagnostics;
	}

	public long getClusterId() {
		return clusterId;
	}

	public void setClusterId(long clusterId) {
		this.clusterId = clusterId;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public long getStartedTime() {
		return startedTime;
	}

	public void setStartedTime(long startedTime) {
		this.startedTime = startedTime;
	}

	public long getFinishedTime() {
		return finishedTime;
	}

	public void setFinishedTime(long finishedTime) {
		this.finishedTime = finishedTime;
	}

	public int getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(int elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public String getAmContainerLogs() {
		return amContainerLogs;
	}

	public void setAmContainerLogs(String amContainerLogs) {
		this.amContainerLogs = amContainerLogs;
	}

	public String getAmHostHttpAddress() {
		return amHostHttpAddress;
	}

	public void setAmHostHttpAddress(String amHostHttpAddress) {
		this.amHostHttpAddress = amHostHttpAddress;
	}

	public int getAllocatedMB() {
		return allocatedMB;
	}

	public void setAllocatedMB(int allocatedMB) {
		this.allocatedMB = allocatedMB;
	}

	public int getAllocatedVCores() {
		return allocatedVCores;
	}

	public void setAllocatedVCores(int allocatedVCores) {
		this.allocatedVCores = allocatedVCores;
	}

	public int getRunningContainers() {
		return runningContainers;
	}

	public void setRunningContainers(int runningContainers) {
		this.runningContainers = runningContainers;
	}

	public int getMemorySeconds() {
		return memorySeconds;
	}

	public void setMemorySeconds(int memorySeconds) {
		this.memorySeconds = memorySeconds;
	}

	public int getVcoreSeconds() {
		return vcoreSeconds;
	}

	public void setVcoreSeconds(int vcoreSeconds) {
		this.vcoreSeconds = vcoreSeconds;
	}

	public int getPreemptedResourceMB() {
		return preemptedResourceMB;
	}

	public void setPreemptedResourceMB(int preemptedResourceMB) {
		this.preemptedResourceMB = preemptedResourceMB;
	}

	public int getPreemptedResourceVCores() {
		return preemptedResourceVCores;
	}

	public void setPreemptedResourceVCores(int preemptedResourceVCores) {
		this.preemptedResourceVCores = preemptedResourceVCores;
	}

	// private final ApplicationReport app;

	// private String name;
	// private
	//
	// public ApplicationReportStatus( //ApplicationReport app
	// ) {
	// super();
	// //this.app = app;
	// }
	//
	// public String getName() {
	// return this.app.getName();
	// }
	//
	// public Resource getNeededResource() {
	// return app.getApplicationResourceUsageReport().getNeededResources();
	// }
	//
	// public Resource getReservedResource() {
	// return app.getApplicationResourceUsageReport().getReservedResources();
	// }
	//
	// public Resource getUsedResource() {
	// return app.getApplicationResourceUsageReport().getUsedResources();
	// }
	//
	// public int getApplicationId() {
	// return app.getApplicationId().getId();
	// }
	//
	public final String getStartTime() {
		return ManageUtils.formatDateYYYYMMdd(new Date(this.startedTime));
	}

	//
	public final String getEndTime() {
		return ManageUtils.formatDateYYYYMMdd(new Date(this.finishedTime));
	}
	//
	// public String getAppState() {
	// return String.valueOf(app.getYarnApplicationState());
	//
	// }
	//
	// public String getFinalAppState() {
	// return String.valueOf(app.getFinalApplicationStatus());
	// }
	//
	// public final String getProgress() {
	// return String.valueOf((int) (app.getProgress() * 100)) + "%";
	// }
	//
	// // 诊断信息
	// public String getDiagnostics() {
	// return app.getDiagnostics();
	// }

}
