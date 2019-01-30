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
package org.apache.solr.cloud.bak;

import java.io.Closeable;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.solr.cloud.CloudDescriptor;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.ZkCoreNodeProps;
import org.apache.solr.common.cloud.ZkNodeProps;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;
import org.apache.solr.logging.MDCLoggingContext;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.update.TisUpdateLog;
import org.apache.solr.update.UpdateLog;
import org.apache.solr.update.UpdateLog.RecoveryInfo;
import org.apache.solr.util.RefCounted;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 需要修改补增量的逻辑所以需要修改这个类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RecoveryStrategy extends Thread implements Closeable {

	private static final int WAIT_FOR_UPDATES_WITH_STALE_STATE_PAUSE = Integer
			.getInteger("solr.cloud.wait-for-updates-with-stale-state-pause", 7000);

	private static final int MAX_RETRIES = 500;

	private static final int STARTING_RECOVERY_DELAY = 5000;

	// private static final String REPLICATION_HANDLER = "/replication";
	private static Logger log = LoggerFactory.getLogger(RecoveryStrategy.class);

	public static interface RecoveryListener {

		public void recovered();

		public void failed();
	}

	private volatile boolean close = false;

	private RecoveryListener recoveryListener;

	private ZkController zkController;

	private String baseUrl;

	private String coreZkNodeName;

	private ZkStateReader zkStateReader;

	private volatile String coreName;

	private int retries;

	private boolean recoveringAfterStartup;

	/* 百岁添加 */
	private boolean forceRecoveryIgnoreWetherIAmLeader;

	private CoreContainer cc;

	// this should only be used from SolrCoreState
	public RecoveryStrategy(CoreContainer cc, CoreDescriptor cd, RecoveryListener recoveryListener) {
		this.cc = cc;
		this.coreName = cd.getName();
		this.recoveryListener = recoveryListener;
		setName("RecoveryThread-" + this.coreName);
		zkController = cc.getZkController();
		zkStateReader = zkController.getZkStateReader();
		baseUrl = zkController.getBaseUrl();
		coreZkNodeName = cd.getCloudDescriptor().getCoreNodeName();
	}

	public void setRecoveringAfterStartup(boolean recoveringAfterStartup) {
		this.recoveringAfterStartup = recoveringAfterStartup;
	}

	// make sure any threads stop retrying
	@Override
	public void close() {
		close = true;
		try {
			// prevSendPreRecoveryHttpUriRequest.abort();
		} catch (NullPointerException e) {
			// okay
		}
		log.warn("Stopping recovery for core={} coreNodeName={}", coreName, coreZkNodeName);
	}

	private void recoveryFailed(final SolrCore core, final ZkController zkController, final String baseUrl,
			final String shardZkNodeName, final CoreDescriptor cd) throws Exception {
		SolrException.log(log, "Recovery failed - I give up.");
		try {
			zkController.publish(cd, Replica.State.RECOVERY_FAILED);
		} finally {
			close();
			recoveryListener.failed();
		}
	}

	// private void replicate(String nodeName, SolrCore core,
	// ZkNodeProps leaderprops) throws SolrServerException, IOException {
	//
	// ZkCoreNodeProps leaderCNodeProps = new ZkCoreNodeProps(leaderprops);
	// String leaderUrl = leaderCNodeProps.getCoreUrl();
	//
	// log.info("Attempting to replicate from " + leaderUrl + ".");
	//
	// // send commit
	// commitOnLeader(leaderUrl);
	//
	// // use rep handler directly, so we can do this sync rather than async
	// SolrRequestHandler handler = core
	// .getRequestHandler(REPLICATION_HANDLER);
	// ReplicationHandler replicationHandler = (ReplicationHandler) handler;
	//
	// if (replicationHandler == null) {
	// throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE,
	// "Skipping recovery, no " + REPLICATION_HANDLER
	// + " handler found");
	// }
	//
	// ModifiableSolrParams solrParams = new ModifiableSolrParams();
	// solrParams.set(ReplicationHandler.MASTER_URL, leaderUrl);
	//
	// if (isClosed())
	// return; // we check closed on return
	// boolean success = replicationHandler.doFetch(solrParams, false);
	//
	// if (!success) {
	// throw new SolrException(ErrorCode.SERVER_ERROR,
	// "Replication for recovery failed.");
	// }
	//
	// // solrcloud_debug
	// debugRecoverySummary(core, leaderUrl);
	//
	// }
	// /**
	// * 百岁 refactor
	// *
	// * @param core
	// * @param leaderUrl
	// */
	// private void debugRecoverySummary(SolrCore core, String leaderUrl) {
	// if (log.isDebugEnabled()) {
	// try {
	// RefCounted<SolrIndexSearcher> searchHolder = core
	// .getNewestSearcher(false);
	// SolrIndexSearcher searcher = searchHolder.get();
	// Directory dir = core.getDirectoryFactory()
	// .get(core.getIndexDir(), DirContext.META_DATA, null);
	// try {
	// log.debug(core.getCoreDescriptor().getCoreContainer()
	// .getZkController().getNodeName()
	// + " replicated "
	// + searcher.search(new MatchAllDocsQuery(),
	// 1).totalHits
	// + " from " + leaderUrl + " gen:"
	// + core.getDeletionPolicy().getLatestCommit() != null
	// ? "null"
	// : core.getDeletionPolicy().getLatestCommit()
	// .getGeneration() + " data:"
	// + core.getDataDir() + " index:"
	// + core.getIndexDir() + " newIndex:"
	// + core.getNewIndexDir() + " files:"
	// + Arrays.asList(dir.listAll()));
	// } finally {
	// core.getDirectoryFactory().release(dir);
	// searchHolder.decref();
	// }
	// } catch (Exception e) {
	// log.debug("Error in solrcloud_debug block", e);
	// }
	// }
	// }
	// private void commitOnLeader(String leaderUrl)
	// throws SolrServerException, IOException {
	// try (HttpSolrClient client = new HttpSolrClient(leaderUrl)) {
	// client.setConnectionTimeout(30000);
	// UpdateRequest ureq = new UpdateRequest();
	// ureq.setParams(new ModifiableSolrParams());
	// ureq.getParams().set(DistributedUpdateProcessor.COMMIT_END_POINT,
	// true);
	// ureq.getParams().set(UpdateParams.OPEN_SEARCHER, false);
	// ureq.setAction(AbstractUpdateRequest.ACTION.COMMIT, false, true)
	// .process(client);
	// }
	// }
	@Override
	public void run() {
		// set request info for logging
		try (SolrCore core = cc.getCore(coreName)) {
			if (core == null) {
				SolrException.log(log, "SolrCore not found - cannot recover:" + coreName);
				return;
			}
			MDCLoggingContext.setCore(core);
			log.info("Starting recovery process. recoveringAfterStartup=" + recoveringAfterStartup);
			try {
				doRecovery(core);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				SolrException.log(log, "", e);
				throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
			} catch (Exception e) {
				log.error("", e);
				throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "", e);
			}
		} finally {
			MDCLoggingContext.clear();
		}
	}

	// TODO: perhaps make this grab a new core each time through the loop to
	// handle core reloads?
	public void doRecovery(SolrCore core) throws KeeperException, InterruptedException {
		boolean replayed = false;
		boolean successfulRecovery = false;
		UpdateLog ulog;
		ulog = core.getUpdateHandler().getUpdateLog();
		try {
			if (ulog == null) {
				SolrException.log(log, "No UpdateLog found - cannot recover.");
				recoveryFailed(core, zkController, baseUrl, coreZkNodeName, core.getCoreDescriptor());
				return;
			}
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		boolean firstTime = true;
		// List<Long> recentVersions;
		// UpdateLog.RecentUpdates recentUpdates = null;
		// try {
		// recentUpdates = ulog.getRecentUpdates();
		// recentVersions = recentUpdates
		// .getVersions(ulog.getNumRecordsToKeep());
		// } catch (Exception e) {
		// SolrException.log(log, "Corrupt tlog - ignoring.", e);
		// recentVersions = new ArrayList<>(0);
		// } finally {
		// if (recentUpdates != null) {
		// recentUpdates.close();
		// }
		// }
		// List<Long> startingVersions = ulog.getStartingVersions();
		// if (startingVersions != null && recoveringAfterStartup) {
		// try {
		// int oldIdx = 0; // index of the start of the old list in the
		// // current
		// // list
		// long firstStartingVersion = startingVersions.size() > 0
		// ? startingVersions.get(0) : 0;
		//
		// for (; oldIdx < recentVersions.size(); oldIdx++) {
		// if (recentVersions.get(oldIdx) == firstStartingVersion)
		// break;
		// }
		//
		// if (oldIdx > 0) {
		// log.info(
		// "####### Found new versions added after startup: num="
		// + oldIdx);
		// log.info("###### currentVersions=" + recentVersions);
		// }
		//
		// log.info("###### startupVersions=" + startingVersions);
		// } catch (Exception e) {
		// SolrException.log(log, "Error getting recent versions.", e);
		// recentVersions = new ArrayList<>(0);
		// }
		// }
		// if (recoveringAfterStartup) {
		// // if we're recovering after startup (i.e. we have been down), then
		// // we need to know what the last versions were
		// // when we went down. We may have received updates since then.
		// recentVersions = startingVersions;
		// try {
		// if ((ulog.getStartingOperation() & UpdateLog.FLAG_GAP) != 0) {
		// // last operation at the time of startup had the GAP flag
		// // set...
		// // this means we were previously doing a full index
		// // replication
		// // that probably didn't complete and buffering updates in
		// // the
		// // meantime.
		// log.info(
		// "Looks like a previous replication recovery did not complete -
		// skipping peer sync.");
		// firstTime = false; // skip peersync
		// }
		// } catch (Exception e) {
		// SolrException.log(log,
		// "Error trying to get ulog starting operation.", e);
		// firstTime = false; // skip peersync
		// }
		// }
		Future<RecoveryInfo> replayFuture = null;
		while (!successfulRecovery && !isInterrupted() && !isClosed()) {
			// though
			try {
				CloudDescriptor cloudDesc = core.getCoreDescriptor().getCloudDescriptor();
				ZkNodeProps leaderprops = zkStateReader.getLeaderRetry(cloudDesc.getCollectionName(),
						cloudDesc.getShardId());
				final String leaderBaseUrl = leaderprops.getStr(ZkStateReader.BASE_URL_PROP);
				final String leaderCoreName = leaderprops.getStr(ZkStateReader.CORE_NAME_PROP);
				String leaderUrl = ZkCoreNodeProps.getCoreUrl(leaderBaseUrl, leaderCoreName);
				String ourUrl = ZkCoreNodeProps.getCoreUrl(baseUrl, coreName);
				boolean isLeader = leaderUrl.equals(ourUrl);
				if (isLeader && !cloudDesc.isLeader()) {
					throw new SolrException(ErrorCode.SERVER_ERROR, "Cloud state still says we are leader.");
				}
				// baisui modify
				if (!this.isForceRecoveryIgnoreWetherIAmLeader() && cloudDesc.isLeader()) {
					// we are now the leader - no one else must have been
					// suitable
					log.warn("We have not yet recovered - but we are now the leader!");
					log.info("Finished recovery process.");
					zkController.publish(core.getCoreDescriptor(), Replica.State.ACTIVE);
					return;
				}
				log.info("Publishing state of core " + core.getName() + " as recovering, leader is " + leaderUrl
						+ " and I am " + ourUrl);
				zkController.publish(core.getCoreDescriptor(), Replica.State.RECOVERING);
				if (isClosed()) {
					log.info("Recovery was cancelled");
					break;
				}
				if (!cloudDesc.isLeader()) {
					// baisui comment 20160323
					// sendPrepRecoveryCmd(leaderBaseUrl, leaderCoreName,
					// slice);
				}
				if (isClosed()) {
					log.info("Recovery was cancelled");
					break;
				}
				// discussion around current value)
				try {
					Thread.sleep(WAIT_FOR_UPDATES_WITH_STALE_STATE_PAUSE);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if (isClosed()) {
					log.info("Recovery was cancelled");
					break;
				}
				log.info("Starting Replication Recovery.");
				log.info("Begin buffering updates.");
				ulog.bufferUpdates();
				replayed = false;
				try {
					if (isClosed()) {
						log.info("Recovery was cancelled");
						break;
					}
					// baisui modify 20151019
					replayFuture = replay(core);
					replayed = true;
					if (isClosed()) {
						log.info("Recovery was cancelled");
						break;
					}
					log.info("Replication Recovery was successful - registering as Active.");
					// if there are pending recovery requests, don't advert as
					// active
					zkController.publish(core.getCoreDescriptor(), Replica.State.ACTIVE);
					close = true;
					successfulRecovery = true;
					recoveryListener.recovered();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("Recovery was interrupted", e);
					close = true;
				} catch (Exception e) {
					SolrException.log(log, "Error while trying to recover", e);
				} finally {
					if (!replayed) {
						try {
							ulog.dropBufferedUpdates();
						} catch (Exception e) {
							SolrException.log(log, "", e);
						}
					}
				}
			} catch (Exception e) {
				SolrException.log(log, "Error while trying to recover.", e);
			}
			if (!successfulRecovery) {
				// Or do a fall off retry...
				try {
					if (isClosed()) {
						break;
					}
					log.error("Recovery failed - trying again... (" + retries + ")");
					retries++;
					if (retries >= MAX_RETRIES) {
						SolrException.log(log, "Recovery failed - max retries exceeded (" + retries + ").");
						try {
							recoveryFailed(core, zkController, baseUrl, coreZkNodeName, core.getCoreDescriptor());
						} catch (Exception e) {
							SolrException.log(log, "Could not publish that recovery failed", e);
						}
						break;
					}
				} catch (Exception e) {
					SolrException.log(log, "", e);
				}
				try {
					// start at 1 sec and work up to a min
					double loopCount = Math.min(Math.pow(2, retries), 60);
					log.info("Wait {} seconds before trying to recover again ({})", loopCount, retries);
					for (int i = 0; i < loopCount; i++) {
						if (isClosed())
							// check if someone closed us
							break;
						Thread.sleep(STARTING_RECOVERY_DELAY);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("Recovery was interrupted.", e);
					close = true;
				}
			}
		}
		// then we still need to update version bucket seeds after recovery
		if (successfulRecovery && replayFuture == null) {
			log.info("Updating version bucket highest from index after successful recovery.");
			core.seedVersionBuckets();
		}
		log.info("Finished recovery process.");
	}

	/**
	 * 百岁修改
	 *
	 * @param core
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Future<RecoveryInfo> replay(SolrCore core) throws InterruptedException, ExecutionException {
		UpdateLog ulog = core.getUpdateHandler().getUpdateLog();
		if (!(ulog instanceof TisUpdateLog)) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Replay failed,ulog must be type of 'TisUpdateLog'");
		}
		TisUpdateLog tisUpdateLog = (TisUpdateLog) ulog;
		File indexDir = new File(core.getIndexDir());
		long fulldumptimePoint = 0;
		try {
			fulldumptimePoint = Long.parseLong(StringUtils.substringAfter(indexDir.getName(), "index"));
		} catch (Throwable e) {
			// log.error(e.getMessage(), e);
		}
		Future<RecoveryInfo> future = null;
		// 这里修改了
		if (fulldumptimePoint > 0 && core.isReloaded()) {
			// 重新加载
			// TODO 是否可以使用searcher的last commitinfo中的时间戳来截断
			future = tisUpdateLog.applyBufferedUpdates(fulldumptimePoint);
		} else {
			// 重新启动
			future = tisUpdateLog.applyBufferedUpdates();
		}
		// .applyBufferedUpdates();
		if (future == null) {
			// no replay needed\
			log.info("No replay needed.");
		} else {
			log.info("Replaying buffered documents.");
			// wait for replay
			RecoveryInfo report = future.get();
			if (report.failed) {
				SolrException.log(log, "Replay failed");
				throw new SolrException(ErrorCode.SERVER_ERROR, "Replay failed");
			}
		}
		// solrcloud_debug
		if (log.isInfoEnabled()) {
			try {
				RefCounted<SolrIndexSearcher> searchHolder = core.getNewestSearcher(false);
				SolrIndexSearcher searcher = searchHolder.get();
				try {
					// log.info(core.getCoreDescriptor().getCoreContainer().getZkController().getNodeName()
					// + " replayed " + searcher.search(new MatchAllDocsQuery(),
					// 1).totalHits);
					log.info(cc.getZkController().getNodeName() + " replayed "
							+ searcher.search(new MatchAllDocsQuery(), 1).totalHits);

				} finally {
					searchHolder.decref();
				}
			} catch (Exception e) {
				log.debug("Error in solrcloud_debug block", e);
			}
		}
		return future;
	}

	// @Override
	public boolean isClosed() {
		return close;
	}

	// baisui commented for
	// private void sendPrepRecoveryCmd(String leaderBaseUrl,
	// String leaderCoreName, Slice slice) throws SolrServerException,
	// IOException, InterruptedException, ExecutionException {
	//
	// try (HttpSolrClient client = new HttpSolrClient(leaderBaseUrl)) {
	// client.setConnectionTimeout(30000);
	// WaitForState prepCmd = new WaitForState();
	// prepCmd.setCoreName(leaderCoreName);
	// prepCmd.setNodeName(zkController.getNodeName());
	// prepCmd.setCoreNodeName(coreZkNodeName);
	// prepCmd.setState(Replica.State.RECOVERING);
	// prepCmd.setCheckLive(true);
	// prepCmd.setOnlyIfLeader(true);
	// final Slice.State state = slice.getState();
	// if (state != Slice.State.CONSTRUCTION
	// && state != Slice.State.RECOVERY) {
	// prepCmd.setOnlyIfLeaderActive(true);
	// }
	// HttpUriRequestResponse mrr = client.httpUriRequest(prepCmd);
	// prevSendPreRecoveryHttpUriRequest = mrr.httpUriRequest;
	//
	// log.info("Sending prep recovery command to {}; {}", leaderBaseUrl,
	// prepCmd.toString());
	//
	// mrr.future.get();
	// }
	// }
	public boolean isForceRecoveryIgnoreWetherIAmLeader() {
		return forceRecoveryIgnoreWetherIAmLeader;
	}

	public void setForceRecoveryIgnoreWetherIAmLeader(boolean forceRecoveryIgnoreWetherIAmLeader) {
		this.forceRecoveryIgnoreWetherIAmLeader = forceRecoveryIgnoreWetherIAmLeader;
	}
}
