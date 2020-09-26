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
package com.qlangtech.tis.rpc.server;

import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.grpc.Empty;
import com.qlangtech.tis.grpc.IncrStatusGrpc;
import com.qlangtech.tis.grpc.LaunchReportInfoEntry;
import com.qlangtech.tis.grpc.TableSingleDataIndexStatus;
import com.qlangtech.tis.realtime.yarn.rpc.*;
import com.qlangtech.tis.rpc.grpc.log.common.TableDumpStatus;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-05-01 11:50
 */
public class IncrStatusClient implements IncrStatusUmbilicalProtocol {

    private static final Logger logger = LoggerFactory.getLogger(IncrStatusClient.class);

    private final IncrStatusGrpc.IncrStatusBlockingStub blockingStub;

    private final IncrStatusGrpc.IncrStatusStub asyncStub;

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    public IncrStatusClient(Channel channel) {
        blockingStub = IncrStatusGrpc.newBlockingStub(channel);
        asyncStub = IncrStatusGrpc.newStub(channel);
    }

    // PingResult ping = blockingStub.ping(Empty.newBuilder().build());
    // System.out.println(ping.getValue());
    @Override
    public PingResult ping() {
        com.qlangtech.tis.grpc.PingResult ping = blockingStub.ping(Empty.newBuilder().build());
        PingResult result = new PingResult();
        result.setValue(ping.getValue());
        return result;
    }

    @Override
    public MasterJob reportStatus(UpdateCounterMap upateCounter) {
        com.qlangtech.tis.grpc.UpdateCounterMap.Builder builder = com.qlangtech.tis.grpc.UpdateCounterMap.newBuilder();
        upateCounter.getData().entrySet().forEach((e) -> {
            TableSingleDataIndexStatus.Builder sbuilder = TableSingleDataIndexStatus.newBuilder();
            com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus value = e.getValue();
            value.getTableConsumeData().entrySet().forEach((ce) -> {
                sbuilder.putTableConsumeData(ce.getKey(), ce.getValue());
            });
            sbuilder.setBufferQueueRemainingCapacity(value.getBufferQueueRemainingCapacity());
            sbuilder.setBufferQueueUsedSize(value.getBufferQueueUsedSize());
            sbuilder.setConsumeErrorCount(value.getConsumeErrorCount());
            sbuilder.setIgnoreRowsCount(value.getIgnoreRowsCount());
            sbuilder.setTis30SAvgRT(value.getTis30sAvgRT());
            sbuilder.setUuid(value.getUUID());
            sbuilder.setIncrProcessPaused(value.isIncrProcessPaused());
            com.qlangtech.tis.grpc.TableSingleDataIndexStatus s = sbuilder.build();
            builder.putData(e.getKey(), s);
        });
        builder.setFrom(upateCounter.getFrom());
        builder.setGcCounter(upateCounter.getGcCounter());
        builder.setUpdateTime(upateCounter.getUpdateTime());
        com.qlangtech.tis.grpc.UpdateCounterMap updateCounterMap = builder.build();
        com.qlangtech.tis.grpc.MasterJob masterJob = blockingStub.reportStatus(updateCounterMap);
        if (masterJob.getJobType() == com.qlangtech.tis.grpc.MasterJob.JobType.None) {
            return null;
        }
        // JobType jobType, String indexName, String uuid
        MasterJob job = new MasterJob(JobType.parseJobType(masterJob.getJobTypeValue()), masterJob.getIndexName(), masterJob.getUuid());
        job.setStop(masterJob.getStop());
        job.setCreateTime(masterJob.getCreateTime());
        return job;
    }

    @Override
    public void nodeLaunchReport(LaunchReportInfo launchReportInfo) {
        com.qlangtech.tis.grpc.LaunchReportInfo.Builder builder = com.qlangtech.tis.grpc.LaunchReportInfo.newBuilder();
        launchReportInfo.getCollectionFocusTopicInfo().entrySet().forEach((e) -> {
            com.qlangtech.tis.grpc.TopicInfo.Builder topicinfoBuilder = com.qlangtech.tis.grpc.TopicInfo.newBuilder();
            TopicInfo tinfo = e.getValue();
            tinfo.getTopicWithTags().entrySet().forEach((i) -> {
                LaunchReportInfoEntry.Builder ebuilder = LaunchReportInfoEntry.newBuilder();
                ebuilder.setTopicName(i.getKey());
                ebuilder.addAllTagName(i.getValue());
                topicinfoBuilder.addTopicWithTags(ebuilder.build());
            });
            builder.putCollectionFocusTopicInfo(e.getKey(), /*collection name*/
            topicinfoBuilder.build());
        });
        blockingStub.nodeLaunchReport(builder.build());
    }

    @Override
    public void reportDumpTableStatus(DumpPhaseStatus.TableDumpStatus tableDumpStatus) {
        TableDumpStatus.Builder builder = TableDumpStatus.newBuilder();
        builder.setAllRows(tableDumpStatus.getAllRows());
        builder.setComplete(tableDumpStatus.isComplete());
        builder.setFaild(tableDumpStatus.isFaild());
        builder.setReadRows(tableDumpStatus.getReadRows());
        builder.setTableName(tableDumpStatus.getName());
        builder.setTaskid(tableDumpStatus.getTaskid());
        builder.setWaiting(tableDumpStatus.isWaiting());
        blockingStub.reportDumpTableStatus(builder.build());
    }

    @Override
    public void reportBuildIndexStatus(BuildSharedPhaseStatus buildStatus) {
        com.qlangtech.tis.rpc.grpc.log.common.BuildSharedPhaseStatus.Builder builder = com.qlangtech.tis.rpc.grpc.log.common.BuildSharedPhaseStatus.newBuilder();
        builder.setAllBuildSize(buildStatus.getAllBuildSize());
        builder.setBuildReaded(buildStatus.getBuildReaded());
        builder.setComplete(buildStatus.isComplete());
        builder.setFaild(buildStatus.isFaild());
        builder.setSharedName(buildStatus.getSharedName());
        builder.setTaskid(buildStatus.getTaskid());
        builder.setWaiting(buildStatus.isWaiting());
        blockingStub.reportBuildIndexStatus(builder.build());
    }

    /**
     * Issues several different requests and then exits.
     */
    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:8980";
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [target]");
                System.err.println("");
                System.err.println("  target  The server to connect to. Defaults to " + target);
                System.exit(1);
            }
            target = args[0];
        }
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        try {
            IncrStatusClient client = new IncrStatusClient(channel);
            // Looking for a valid feature
            while (true) {
                client.ping();
                Thread.sleep(1000);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
