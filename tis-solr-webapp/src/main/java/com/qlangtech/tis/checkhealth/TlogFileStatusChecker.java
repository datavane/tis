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
package com.qlangtech.tis.checkhealth;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.health.check.IStatusChecker;
import com.qlangtech.tis.health.check.Mode;
import com.qlangtech.tis.health.check.StatusLevel;
import com.qlangtech.tis.health.check.StatusModel;
import com.qlangtech.tis.manage.common.SendSMSUtils;

/*
 * 用于校验每个collection tlog文件夹中的最后两个文件是否正常，<br>
 * tlog会在每次硬提交之后生成一个新的tlog文件，但是在某种异常状下，不会生成一个新的文件，始终只向一个老的文件中写入
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TlogFileStatusChecker implements IStatusChecker {

    private static final int CHECK_ORDER = 10;

    private static final String COLLECTION_PREFIX = "search4";

    private static final Pattern PATTERN_FILE_INDEX = Pattern.compile("[1-9]\\d*|0$");

    // private static final Pattern PATTERN_REPLIC = Pattern
    // .compile("search4(.+?)_shard\\d+_replica\\d+");
    private static final Logger logger = LoggerFactory.getLogger("check_health");

    // 十分钟
    private static final long MAX_TLOG_TIME_GAP = 360 * 60 * 1000;

    private long lastProcess = System.currentTimeMillis();

    private File solrHome;

    @Override
    public void init() {
        final Path solrHome = SolrResourceLoader.locateSolrHome();
        if (solrHome == null) {
            throw new IllegalStateException(" 'solrHome' can not be null");
        }
        this.solrHome = solrHome.toFile();
    }

    @Override
    public int order() {
        return CHECK_ORDER;
    }

    @Override
    public Mode mode() {
        return Mode.PUB;
    }

    @Override
    public StatusModel check() {
        // / opt/data/solrhome/search4menu_new_shard1_replica1/data/tlog
        // 扫描home目录下的所有tlog文件
        StatusModel stateModel = new StatusModel();
        stateModel.level = StatusLevel.OK;
        if ((lastProcess + MAX_TLOG_TIME_GAP * 2) > System.currentTimeMillis()) {
            return stateModel;
        }
        final String[] indexDirs = solrHome.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return StringUtils.startsWith(name, COLLECTION_PREFIX);
            }
        });
        LastestTlog latestTlogs = null;
        StringBuffer summary = new StringBuffer("tlog illegal ");
        boolean illegal = false;
        Matcher matcher = null;
        for (String indexDir : indexDirs) {
            File idxDir = new File(solrHome, indexDir + "/data/tlog");
            if (!idxDir.exists()) {
                continue;
            }
            latestTlogs = new LastestTlog(idxDir);
            String[] tlogs = idxDir.list();
            for (String tlog : tlogs) {
                matcher = PATTERN_FILE_INDEX.matcher(tlog);
                if (matcher.find()) {
                    latestTlogs.addTlog(Integer.parseInt(matcher.group()), tlog);
                }
            }
            // 增量文件是否正常
            if (!latestTlogs.isValid()) {
                summary.append(indexDir).append(",");
                illegal = true;
            // Matcher m = PATTERN_REPLIC.matcher(indexDir);
            // if (m.matches()) {
            // illegal = true;
            // summary.append(m.group(1)).append(",");
            // } else {
            // throw new IllegalStateException(indexDir + " is not match " +
            // PATTERN_REPLIC);
            // }
            }
        }
        try {
            if (illegal) {
                // stateModel.level = StatusLevel.FAIL;
                // stateModel.message = summary.toString();
                InetAddress local = InetAddress.getLocalHost();
                SendSMSUtils.send("tlog falid " + local.getHostName() + "," + local.getHostAddress() + " " + summary.toString());
                logger.info("tlog status falid " + summary.toString());
                return stateModel;
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        logger.info("tlog status OK,check:" + Arrays.toString(indexDirs));
        lastProcess = System.currentTimeMillis();
        return stateModel;
    }

    private static class LastestTlog {

        IndexFile lastestTlog = null;

        IndexFile secLatestTlog = null;

        private final File tlogDir;

        public LastestTlog(File tlogDir) {
            super();
            this.tlogDir = tlogDir;
        }

        void addTlog(int index, String fileName) {
            if (secLatestTlog == null || secLatestTlog.index < index) {
                IndexFile newIndexFile = new IndexFile(index, new File(tlogDir, fileName));
                if (lastestTlog == null || lastestTlog.index < index) {
                    // 堆内大小交换
                    secLatestTlog = lastestTlog;
                    lastestTlog = newIndexFile;
                } else {
                    // 只替换小的
                    secLatestTlog = newIndexFile;
                }
            }
        }

        boolean isValid() {
            if (lastestTlog == null || secLatestTlog == null) {
                return true;
            }
            int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            // 凌晨 这个时间段时间不需要care
            int coefficient = (hourOfDay >= 0 && hourOfDay < 10) ? 5 : 1;
            // 倒数第二个最新的文件如果比最新的文件lastmodify时间如果相差10分钟，说明增量执行文件有问题了
            if (lastestTlog.lastModified() > (secLatestTlog.lastModified() + (MAX_TLOG_TIME_GAP * coefficient))) {
                return false;
            }
            return true;
        }
    }

    private static class IndexFile {

        private final int index;

        private final File tlog;

        public long lastModified() {
            return tlog.lastModified();
        }

        /**
         * @param index
         * @param tlog
         */
        public IndexFile(int index, File tlog) {
            super();
            this.index = index;
            this.tlog = tlog;
        }
    }

    public static void main(String[] args) throws Exception {
        InetAddress local = InetAddress.getLocalHost();
        System.out.println(local.getHostName());
    // Matcher m = PATTERN_FILE_INDEX.matcher("tlog.0000000000100001");
    // 
    // if (m.find()) {
    // System.out.println(m.group());
    // } else {
    // System.out.println("not find");
    // }
    }
}
