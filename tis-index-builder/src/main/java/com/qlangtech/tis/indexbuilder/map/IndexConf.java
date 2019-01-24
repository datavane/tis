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
package com.qlangtech.tis.indexbuilder.map;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.management.OperatingSystemMXBean;
import com.taobao.terminator.build.jobtask.Context;
import com.qlangtech.tis.indexbuilder.index.IndexMerger.MergeMode;
import com.qlangtech.tis.indexbuilder.source.SourceType;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IndexConf extends Configuration {

    public static final Logger logger = LoggerFactory.getLogger(IndexConf.class);

    public IndexConf(boolean loadDefault) {
        super(false);
    }

    public String getBucketname() {
        return get("indexing.bucketname");
    }

    public String getFileSystemType() {
        return get("indexing.fileSystemType", "hdfs");
    }

    /*
	 * public int getMaxFileCount() { return
	 * getInt("indexing.maxlocalfilecount",10); }
	 */
    public String getFsName() {
        return getSourceFsName();
    }

    public String getSourceFsName() {
        return get("indexing.sourcefsname");
    }

    public String getWriteFileUser() {
        return get("terminator.write.file.user");
    }

    public String getSchemaName() {
        return get("indexing.schemaname", "schema.xml");
    }

    public MergeMode getMergeMode() {
        return MergeMode.valueOf(get("indexing.mergemode", "serial").toUpperCase());
    }

    public String getSourcePath() {
        return get("indexing.sourcepath");
    }

    public int getHdfsReaderBufferSize() {
        return getInt("indexing.hdfsreaderbuffersize", 1024);
    }

    public int getFlushCountThreshold() {
        return getInt("indexing.flushCountThreshold", 1000000);
    }

    public long getFlushSizeThreshold() {
        return getLong("indexing.flushSizeThreshold", 50000000);
    }

    public int getDocQueueSize() {
        return getInt("indexing.docqueuesize", 2000);
    }

    public int getDocPoolSize() {
        return getInt("indexing.docpoolsize", 1000);
    }

    public float getDocBoost() {
        return getFloat("indexing.docboost", 1.0f);
    }

    public int getMinSplitSize() {
        return getInt("indexing.minsplitsize", 128 * 1024 * 1024);
    }

    public String getServiceName() {
        return get("indexing.servicename");
    }

    public String getCoreName() {
        return get("indexing.corename");
    }

    public int getDocMakerThreadCount() {
        int count = getInt("indexing.docmakerthreadcount", -1);
        if (count == -1) {
            OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            int threadCount = (int) (os.getAvailableProcessors() * getDocMakerThreadRatio());
            return threadCount > 0 ? threadCount : 1;
        }
        return count;
    }

    public int getIndexMakerThreadCount() {
        int count = getInt("indexing.indexmakerthreadcount", -1);
        if (count == -1) {
            OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            int threadCount = (int) (os.getAvailableProcessors() * getIndexMakerThreadRatio());
            return threadCount > 0 ? threadCount : 1;
        }
        return count;
    }

    public float getDocMakerThreadRatio() {
        return getFloat("indexing.docmakerthreadratio", 0.5f);
    }

    public float getIndexMakerThreadRatio() {
        return getFloat("indexing.indexmakerthreadratio", 0.5f);
    }

    public int getRamDirQueueSize() {
        return getInt("indexing.ramdirqueuesize", 1);
    }

    public int getMaxMergeThreadCount() {
        return getInt("indexing.maxmergethreadcount", 3);
    }

    public int getMaxMergeCount() {
        return getInt("indexing.maxmergecount", 5);
    }

    public int getMaxNumSegments() {
        return getInt("indexing.maxNumSegments", 1);
    }

    public int getFileQueueSize() {
        return getInt("indexing.filequeuesize", 10);
    }

    public int getMaxFailCount() {
        return getInt("indexing.maxfailcount", 100);
    }

    public int getGroupNum() {
        return getInt("indexing.groupnum", Integer.parseInt(get("indexing.corename", "-0").substring(get("indexing.corename", "-0").lastIndexOf("-") + 1)));
    }

    public int getOldGroupNum() {
        return getInt("indexing.oldgroupnum", 0);
    }

    public String getFileSplitor() {
        return get("indexing.filesplitor");
    }

    public String getIndexUserName() {
        return get("indexing.username");
    }

    public static final String KEY_COL_DELIMITER = "indexing.delimiter";

    public String getDelimiter() {
        return get(KEY_COL_DELIMITER, "\t");
    }

    public String getMakerMergePolicy() {
        return get("indexing.maker.mergePolicy", "TieredMergePolicy");
    }

    public int getMakerMergeFacotr() {
        return getInt("indexing.maker.mergefactor", 10000000);
    }

    public int getMergeFacotr() {
        return getInt("indexing.mergefactor", 10);
    }

    public String getMergePolicy() {
        return get("indexing.mergePolicy", "TieredMergePolicy");
    }

    public boolean getUseCompoundFile() {
        return getBoolean("indexing.UseCompoundFile", true);
    }

    public int getSegmentsPerTier() {
        return getInt("indexing.SegmentsPerTier", 10000);
    }

    public int getMaxMergeAtOnce() {
        return getInt("indexing.MaxMergeAtOnce", 10000);
    }

    public boolean isRamOptimize() {
        return getBoolean("indexing.ramoptimize", true);
    }

    public int getMakerRAMBufferSizeMB() {
        return getInt("indexing.maker.RAMBufferSizeMB", 100);
    }

    public boolean getMakerUseCompoundFile() {
        return getBoolean("indexing.maker.UseCompoundFile", true);
    }

    /*
	 * public boolean isYunti() { return getBoolean("indexing.isyunti",false); }
	 */
    public int getMakerSegmentsPerTier() {
        return getInt("indexing.maker.SegmentsPerTier", 10000);
    }

    public int getMakerMaxMergeAtOnce() {
        return getInt("indexing.maker.MaxMergeAtOnce", 10000);
    }

    public String getLockFilePath() {
        return get("indexing.merger.lockfilepath", "/home/admin/terminator_builder/temp/lock");
    }

    public long getOptimizeSizeThreshold() {
        return getLong("indexing.optimze.optimizeSizeThreshold", 1000000000L);
    }

    /**
     * @return
     */
    public int getOptimizeNumThreshold() {
        // TODO Auto-generated method stub
        return getInt("indexing.optimze.optimizeNumThreshold", 10000000);
    }

    public int getFlushCheckInterval() {
        return getInt("indexing.flushcheckinterval", 32768);
    }

    public int getPrintInterval() {
        return getInt("indexing.printinterval", 131072);
    }

    public void loadFrom(Context context) {
        Map<String, String> paramMap = context.getUserParamMap();
        for (Entry<String, String> en : paramMap.entrySet()) {
            set(en.getKey(), en.getValue());
        }
    }

    /**
     * @return
     */
    public boolean isDownloadFile() {
        // TODO Auto-generated method stub
        return getBoolean("indexing.isdownloadfile", false);
    }

    public String getGroupNums() {
        // ,"0;1;2;40;41;42;80;81;82;120;121;122;160;161;162;200;201;202;240;241;242;280;281;282|3;4;5;43;44;45;83;84;85;123;124;125;");
        return get("indexing.groupnums", "0");
    }

    public String getRouteKey() {
        return get("indexing.routekey", "id");
    }

    public int getGroupCount() {
        return getInt("indexing.groupcount", 1);
    }

    public int getOldGroupCount() {
        return getInt("indexing.oldgroupcount", 1);
    }

    public int getMergeThreads() {
        return getInt("indexing.mergethreads", 2);
    }

    public String getLocalOutputPath() {
        return get("indexing.localoutputpath", "/home/admin/ecrmIndex");
    }

    /**
     * @return
     */
    public String getTargetOkFilePath() {
        // TODO Auto-generated method stub
        return get("indexing.targetokfilepath", "/user/admin/" + getServiceName() + "/all/okfile");
    }

    public int getIndexRamDirBufferSize() {
        return getInt("indexing.indexramdirbuffersize", 1024);
    }

    public int getMergeRamDirBufferSize() {
        return getInt("indexing.mergeramdirbuffersize", 10240);
    }

    public int getIndexSmallCacheSize() {
        return getInt("indexing.indexsmallcachesize", 102400);
    }

    public int getIndexLargeCacheSize() {
        return getInt("indexing.indexlargecachesize", 10240000);
    }

    public int getMergeSmallCacheSize() {
        return getInt("indexing.mergesmallcachesize", 1024 * 2);
    }

    public int getMergeLargeCacheSize() {
        return getInt("indexing.mergelargecachesize", getMergeRamDirBufferSize() * 2 + 1);
    }

    public boolean isDirectRAM() {
        return getBoolean("indexing.directram", false);
    }

    public boolean isStoreLocal() {
        return getBoolean("indexing.isstorelocal", false);
    }

    public int getRecordLimit() {
        return getInt("indexing.recordlimit", 0);
    }

    // 兼容ecrm的独特的方式
    public boolean isEcrm() {
        return getBoolean("isecrm", false);
    }

    public String getIndexDate() {
        // TODO Auto-generated method stub
        // 检索节点不传indexing.date,只能从路径中截取建索引的日期，恶心死了
        // /user/admin/search4realecrm/all/11/output/20140707131547
        String date = null;
        try {
            String path = get("indexing.outputpath");
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            path = path.substring(path.lastIndexOf("/") + 1, path.length() - 6);
            date = path;
        // return date;
        } catch (Exception e) {
            logger.warn("获取建索引日期失败！", e);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        if (get("indexing.okfileTimeZone") != null) {
            format.setTimeZone(TimeZone.getTimeZone(get("indexing.okfileTimeZone")));
        }
        return get("indexing.date", date != null ? date : format.format(new Date()));
    }

    public String[] getCores() {
        String[] cores = new String[(int) (getGroupCount() / getOldGroupCount())];
        for (int i = 0; i < cores.length; i++) {
            cores[i] = String.valueOf(getOldGroupCount() * i + getGroupNum());
        }
        return cores;
    }

    public String getOkFileName() {
        return get("indexing.okfilename", "tsearcher.ok");
    }

    public int getPayloadThreadCount() {
        return getInt("indexing.payloadthreadcount", 8);
    }

    public boolean getGenUid() {
        return getBoolean("indexing.genuid", true);
    }

    public String getHdfsjarpath() {
        return get("indexing.hdfsjarpath", "/home/admin/terminator_builder/sharelib/tsearcherhdfs3.3");
    }

    /**
     * @return
     */
    public String getYuntijarpath() {
        // TODO Auto-generated method stub
        return get("indexing.yuntijarpath", "/home/admin/terminator_builder/sharelib/yuntihdfs");
    }

    public String getHbase094jarpath() {
        // TODO Auto-generated method stub
        return get("indexing.hbase094jarpath", "/home/admin/terminator_builder/sharelib/hbase0.94");
    }

    public String getHbaseSite() {
        // TODO Auto-generated method stub
        return get("indexing.hbasesite", "/home/admin/terminator_builder/sharelib/hbase0.94/hbase-site.xml");
    }

    public String getOdpsjarpath() {
        // TODO Auto-generated method stub
        return get("indexing.odpsjarpath", "/home/admin/terminator_builder/sharelib/odps");
    }

    /**
     * @return
     */
    public String getFileNameSplitor() {
        // TODO Auto-generated method stub
        return get("indexing.filenamesplitor", "-");
    }

    // hadoop-site.xml文件位置
    public String getHadoopSite() {
        return get("indexing.hadoopsite", "/home/admin/config/hadoop-site.xml");
    }

    public int getSplitGroupCount() {
        return getInt("indexing.splitgroupcount", 0);
    }

    /**
     * @return
     */
    public boolean isOptimzeRelease() {
        // TODO Auto-generated method stub
        return getBoolean("indexing.optimizerelease", false);
    }

    /**
     * @return
     */
    public String getPayloadFields() {
        // TODO Auto-generated method stub
        return get("indexing.payloadfields");
    }

    /**
     * @return
     */
    public boolean genRangeField() {
        // TODO Auto-generated method stub
        return getBoolean("indexing.genrangefield", true);
    }

    /**
     * @return
     */
    public String getServicejarpath() {
        // TODO Auto-generated method stub
        return "/home/admin/terminator_builder/sharelib/userJar/" + getServiceName();
    }

    /**
     * @return
     */
    public RunEnvironment getRunEnvironment() {
        // TODO Auto-generated method stub
        String env = get("job.runEnvironment", "online");
        return RunEnvironment.getEnum(env);
    // if (env.equals("daily")) {
    // return RunEnvironment.DAILY;
    // //} else if (env.equals("ready")) {
    // //			return RunEnvironment.READY;
    // } else if (env.equals("online")) {
    // return RunEnvironment.ONLINE;
    // } else {
    // return RunEnvironment.ONLINE;
    // }
    }

    public SourceType getSourceType() {
        return getEnum("indexing.sourcetype", SourceType.UNKNOWN);
    }

    /**
     * @return
     */
    public String getRemoteJarHost() {
        // TODO Auto-generated method stub
        return get("indexing.remotejarhost", "http://terminator.admin.tbsite.net:9999");
    }

    /**
     * @return
     */
    public boolean isLoadJar() {
        // TODO Auto-generated method stub
        return getBoolean("indexing.isloadjar", true);
    }

    /**
     * @return
     */
    public String getSourceReaderFactory() {
        // TODO Auto-generated method stub
        if (getSourceType().equals(SourceType.YUNTI)) {
            return get("indexing.sourcereaderfactory", "com.taobao.terminator.indexbuilder.source.YuntiHDFSReaderFactory");
        } else if (getSourceType().equals(SourceType.YUNTI2)) {
            return get("indexing.sourcereaderfactory", "com.taobao.terminator.indexbuilder.source.YuntiHDFSReaderFactory2");
        } else if (getSourceType().equals(SourceType.YUNTI3)) {
            return get("indexing.sourcereaderfactory", "com.taobao.terminator.indexbuilder.source.YuntiHDFSReaderFactory3");
        } else if (getSourceType().equals(SourceType.HDFS)) {
            return get("indexing.sourcereaderfactory", "com.taobao.terminator.indexbuilder.source.impl.HDFSReaderFactory");
        } else if (getSourceType().equals(SourceType.HBASE094)) {
            return get("indexing.sourcereaderfactory", "com.taobao.terminator.indexbuilder.source.HBaseReaderFactory");
        } else if (getSourceType().equals(SourceType.ODPS)) {
            return get("indexing.sourcereaderfactory", "com.taobao.terminator.indexbuilder.source.OdpsReaderFactory");
        } else {
            return get("indexing.sourcereaderfactory");
        }
    }

    /**
     * @return
     */
    public String getSouceReaderRactoryJarPath() {
        // TODO Auto-generated method stub
        if (getSourceType().equals(SourceType.YUNTI) || getSourceType().equals(SourceType.YUNTI2) || getSourceType().equals(SourceType.YUNTI3)) {
            return get("indexing.sourcereaderfactoryjarpath", getYuntijarpath());
        } else if (getSourceType().equals(SourceType.HDFS)) {
            return get("indexing.sourcereaderfactoryjarpath", getHdfsjarpath());
        } else if (getSourceType().equals(SourceType.HBASE094)) {
            return get("indexing.sourcereaderfactoryjarpath", getHbase094jarpath());
        } else if (getSourceType().equals(SourceType.ODPS)) {
            return get("indexing.sourcereaderfactoryjarpath", getOdpsjarpath());
        } else {
            return get("indexing.sourcereaderfactoryjarpath");
        }
    }

    /**
     * @return
     */
    public String getFileFormat() {
        // TODO Auto-generated method stub
        return get("indexing.fileformat", "text");
    }

    public String getHbaseTable() {
        return get("indexing.hbasetable");
    }

    public int getStackSize() {
        return getInt("indexing.stacksize", 1000);
    }

    /**
     * @return
     */
    public String getRsAddr() {
        // TODO Auto-generated method stub
        return get("indexing.rsaddr");
    }

    /**
     * @return
     */
    public String getAppKey() {
        // TODO Auto-generated method stub
        return get("indexing.appkey");
    }

    /**
     * @return
     */
    public String getOdpsEndpoint() {
        // TODO Auto-generated method stub
        return get("indexing.odpsendpoint", "http://dt.odps.aliyun-inc.com");
    }

    /**
     * @return
     */
    public String getOdpsAccessId() {
        // TODO Auto-generated method stub
        return get("indexing.odpsaccessid", "");
    }

    /**
     * @return
     */
    public String getOdpsAccessKey() {
        // TODO Auto-generated method stub
        return get("indexing.odpsaccesskey", "");
    }

    /**
     * @return
     */
    public String getOdpsProject() {
        // TODO Auto-generated method stub
        return get("indexing.odpsproject", "");
    }

    /**
     * @return
     */
    public String getOdpsTable() {
        // TODO Auto-generated method stub
        return get("indexing.odpstable", "");
    }

    /**
     * @return
     */
    public String getIncrTime() {
        return get("indexing.incrtime", "");
    }

    /**
     * @return
     */
    public String getShardKey() {
        // TODO Auto-generated method stub
        return get("indexing.shardkey");
    }

    /**
     * @return
     */
    public int getTotalGroups() {
        // TODO Auto-generated method stub
        return getInt("indexing.totalgroups", 1);
    }

    /**
     * @return
     */
    public String getOdpsPartition() {
        // TODO Auto-generated method stub
        String partdesc = get("indexing.odpspartition", "");
        partdesc = partdesc.trim();
        int start = partdesc.indexOf("{");
        int end = partdesc.indexOf("}");
        if (start != -1 && end != -1) {
            SimpleDateFormat sf = new SimpleDateFormat(partdesc.substring(start + 1, end));
            return partdesc.substring(0, start) + sf.format(new Date());
        } else {
            return partdesc;
        }
    }

    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        System.out.println(format.format(new Date()));
        IndexConf conf = new IndexConf(false);
        System.out.println(conf.get("ddd") == null);
        String corename = "searchxdxx-9";
        System.out.println(corename.substring(corename.lastIndexOf("-") + 1));
    /*
		 * String partdesc = " date{yyyyMMdd}"; int start =
		 * partdesc.indexOf("{"); int end = partdesc.indexOf("}"); String p =
		 * ""; if(start!=-1 && end!=-1) { SimpleDateFormat sf=new
		 * SimpleDateFormat(partdesc.substring(start+1,end)); p=
		 * partdesc.substring(0, start)+sf.format(new Date()); } else { p=
		 * partdesc; } System.out.println(p);
		 */
    }

    /**
     * @return
     */
    public int gethbaseScanCache() {
        // TODO Auto-generated method stub
        return getInt("indexing.hbasescancache", 1000);
    }
}
