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
package com.qlangtech.tis.runtime.module.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.qlangtech.tis.runtime.module.action.IncrInitSpeAction.BuildResult;
import com.qlangtech.tis.runtime.module.action.IncrUtils.Specification;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IncrUtils {

    private IncrUtils() {
    }

    public static final Pattern PATTERN_NUMBER = Pattern.compile("[1-9]\\d*");

    public static boolean isNumber(String val) {
        Matcher m = PATTERN_NUMBER.matcher(val);
        return m.matches();
    }

    public static void main(String[] args) throws Exception {
        Specification s = Specification.parse("300");
        System.out.println(s.getUnit() + "," + s.getVal());
        // System.out.println(isNumber("123000"));
        // 
        // System.out.println(isNumber("0123000"));
        // BuildStatus status = readLastBuildRecordStatus("search4totalpay");
        // System.out.println(status.getBuildName());
        // rollbackIncrDeploy("search4totalpay");
        IncrSpec spec = new IncrSpec();
        // spec.setGitAddress("git@git.2dfire-inc.com:dfire-searcher/tis-mars.git");
        // spec.setGitRef("develop");
        // 
        // Specification s = new Specification();
        // s.setVal(100);
        // s.setUnit("c");
        // spec.setCpuLimit(s);
        // 
        // s = new Specification();
        // s.setVal(200);
        // s.setUnit("cores");
        // spec.setCpuRequest(s);
        // 
        // s = new Specification();
        // s.setVal(2);
        // s.setUnit("G");
        // spec.setMemoryLimit(s);
        // 
        // s = new Specification();
        // s.setVal(200);
        // s.setUnit("M");
        // spec.setMemoryRequest(s);
        // 
        // saveIncrSpec("search4totalpay", spec);
        spec = readIncrSpec("search4totalpay");
    // System.out.println(spec.getGitAddress());
    }

    /**
     * 保存增量通道配置规格
     *
     * @param spec
     */
    public static void saveIncrSpec(String indexName, IncrSpec spec) throws IOException {
        File f = getIncrSpecFile(indexName);
        FileUtils.write(f, JSON.toJSONString(spec, true), Charset.forName("utf8"), false);
    }

    /**
     * 读取索引规格
     *
     * @param indexName
     * @return
     * @throws IOException
     */
    public static IncrSpec readIncrSpec(String indexName) {
        File f = getIncrSpecFile(indexName);
        if (!f.exists()) {
            return null;
        }
        try {
            return JSON.parseObject(FileUtils.readFileToString(f, Charset.forName("utf8")), IncrSpec.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 添加一条部署寫入
     *
     * @param indexName
     * @param status
     * @param version
     */
    public static DeploymentStatus appendDeploymentRecord(String indexName, Status status, int version, int replica, boolean shallSeralize, int preLatestVersionNumber) {
        synchronized (IncrUtils.class) {
            DeploymentStatus s = new DeploymentStatus(indexName);
            s.setStatus(status);
            s.setVersion(version);
            s.setPreVersion(preLatestVersionNumber);
            s.setReplica(replica);
            if (shallSeralize) {
                File f = getDeploymentHistoryFile(indexName);
                String line = version + "," + preLatestVersionNumber + "," + status.val + "," + replica + "\n";
                appendLine(f, line);
            }
            return s;
        }
    }

    public static void appendIndeterminacyDeploymentRecord(String indexName, int preLatestVersionNumber) {
        synchronized (IncrUtils.class) {
            appendDeploymentRecord(indexName, Status.CREATED, -1, -1, true, preLatestVersionNumber);
        }
    }

    /**
     * 读最后一条部署记录
     *
     * @param indexName
     * @param status
     * @param version
     * @return
     */
    public static DeploymentStatus readLastDeploymentRecord(String indexName) {
        synchronized (IncrUtils.class) {
            DeploymentStatus s = new DeploymentStatus(indexName);
            s.setStatus(Status.NONE);
            File f = getDeploymentHistoryFile(indexName);
            // File monitorFile, int n, IProcessLine lineProcess
            readLastNLine(f, 1, new IProcessLine() {

                @Override
                public void print(String line) {
                    String[] ary = StringUtils.split(line, ",");
                    s.setVersion(Integer.parseInt(ary[0]));
                    s.setPreVersion(Integer.parseInt(ary[1]));
                    s.setStatus(Status.parse(ary[2]));
                    s.setReplica(Integer.parseInt(ary[3]));
                }
            });
            return s;
        }
    }

    /**
     * 取消增量构建，需要建已經写入的deploy记录回滚
     */
    public static void rollbackIncrDeploy(String indexName) {
        synchronized (IncrUtils.class) {
            final List<DeploymentStatus> history = Lists.newArrayList();
            File f = getDeploymentHistoryFile(indexName);
            // File monitorFile, int n, IProcessLine lineProcess
            readLastNLine(f, 2, new IProcessLine() {

                @Override
                public void print(String line) {
                    DeploymentStatus s = new DeploymentStatus(indexName);
                    String[] ary = StringUtils.split(line, ",");
                    s.setVersion(Integer.parseInt(ary[0]));
                    s.setPreVersion(Integer.parseInt(ary[1]));
                    s.setStatus(Status.parse(ary[2]));
                    s.setReplica(Integer.parseInt(ary[3]));
                    history.add(s);
                }
            });
            if (history.size() == 1) {
                appendDeploymentRecord(indexName, Status.NONE, -1, -1, true, -1);
            } else if (history.size() == 2) {
                DeploymentStatus s = history.get(0);
                appendDeploymentRecord(indexName, s.getStatus(), s.getVersion(), s.getReplica(), true, s.getPreVersion());
            }
        }
    }

    /**
     * 追加一条成功构建记录
     *
     * @param indexName
     * @param buildName
     */
    public static void appendBuildRecord(String indexName, BuildStatus status) {
        synchronized (IncrUtils.class) {
            String buildName = status.getBuildName();
            Status s = status.getStatus();
            BuildResult buildResult = status.getBuildResult();
            File f = getIncrBuildFile(indexName);
            String line = buildName + "," + s.val + "," + buildResult.getPhase() + "," + buildResult.getStartTime() + "," + buildResult.getDuration() + "\n";
            appendLine(f, line);
        }
    }

    private static void appendLine(File f, String line) {
        try {
            try (FileOutputStream out = FileUtils.openOutputStream(f, true)) {
                out.write((line).getBytes());
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getIncrBuildFile(String indexName) {
        String dataDir = System.getProperty("data.dir", ".");
        if (StringUtils.isEmpty(dataDir)) {
            throw new IllegalArgumentException("system dir 'data.dir' can not be empty");
        }
        File f = new File(dataDir, "/" + indexName + "/incr-build/history.log");
        return f;
    }

    private static File getDeploymentHistoryFile(String indexName) {
        String dataDir = System.getProperty("data.dir", ".");
        if (StringUtils.isEmpty(dataDir)) {
            throw new IllegalArgumentException("system dir 'data.dir' can not be empty");
        }
        return new File(dataDir, "/" + indexName + "/incr-deployment/history.log");
    }

    private static File getIncrSpecFile(String indexName) {
        String dataDir = System.getProperty("data.dir", ".");
        if (StringUtils.isEmpty(dataDir)) {
            throw new IllegalArgumentException("system dir 'data.dir' can not be empty");
        }
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("indexName can not be empty");
        }
        return new File(dataDir, "/incr-spec/" + indexName + ".text");
    }

    public static BuildStatus readLastBuildRecordStatus(String IndexName) {
        synchronized (IncrUtils.class) {
            BuildStatus status = new BuildStatus(IndexName);
            status.setStatus(Status.NONE);
            readLastNLine(getIncrBuildFile(IndexName), 1, new IProcessLine() {

                @Override
                public void print(String line) {
                    String[] args = StringUtils.split(line, ",");
                    status.setBuildName(args[0]);
                    status.setStatus(Status.parse(args[1]));
                    BuildResult bresult = new BuildResult();
                    try {
                        bresult.setDuration(Long.parseLong(args[4]));
                    } catch (Throwable e) {
                    }
                    bresult.setPhase(args[2]);
                    bresult.setStartTime(args[3]);
                    status.setBuildResult(bresult);
                }
            });
            return status;
        }
    }

    public static class DeploymentStatus {

        private final String collection;

        private final String type = "deploy";

        private int version;

        private int preVersion;

        private Status status;

        private int replica;

        public String getType() {
            return type;
        }

        public String getReplicationControllerName() {
            return this.collection + "-" + this.version;
        }

        public int getPreVersion() {
            return preVersion;
        }

        public void setPreVersion(int preVersion) {
            this.preVersion = preVersion;
        }

        public int getReplica() {
            return replica;
        }

        public void setReplica(int replica) {
            this.replica = replica;
        }

        /**
         * 是否是不确定的，因為重新build之後會自動觸發一次deploy，先记录在日志中了
         *
         * @return
         */
        public boolean isIndeterminacy() {
            return this.version < 0;
        }

        public DeploymentStatus(String deploymentName) {
            super();
            this.collection = deploymentName;
        }

        public String getCollection() {
            return collection;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getStatusLiterial() {
            switch(this.status) {
                case CREATED:
                    return "部署中";
                case SUCCESS:
                    return "运行中";
                case FAILD:
                    return "部署失败";
                default:
                    return "尚未部署";
            }
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    public static class BuildStatus {

        private final String indexName;

        private final String type = "build";

        private String buildName;

        private Status status;

        private BuildResult buildResult;

        public String getType() {
            return this.type;
        }

        public BuildStatus(String indexName) {
            super();
            this.indexName = indexName;
        }

        public BuildResult getBuildResult() {
            return this.buildResult;
        }

        public void setBuildResult(BuildResult buildResult) {
            this.buildResult = buildResult;
        }

        public String getBuildName() {
            return buildName;
        }

        public void setBuildName(String buildName) {
            this.buildName = buildName;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getIndexName() {
            return indexName;
        }
    }

    /**
     * 打印文件的最后n行内容
     *
     * @param monitorFile
     * @param n
     * @param lineProcess
     */
    private static void readLastNLine(File monitorFile, int n, IProcessLine lineProcess) {
        if (!monitorFile.exists()) {
            return;
        }
        RandomAccessFile randomAccess = null;
        try {
            randomAccess = new RandomAccessFile(monitorFile, "r");
            // boolean eol = false;
            // int c = -1;
            long fileLength = randomAccess.length();
            long size = 1;
            boolean hasEncountReturn = false;
            ww: while (true) {
                long offset = fileLength - (size++);
                if (offset < 0) {
                    randomAccess.seek(offset + 1);
                    break ww;
                }
                randomAccess.seek(offset);
                switch(// c =
                randomAccess.read()) {
                    case '\n':
                    case '\r':
                        if (!hasEncountReturn && (n--) <= 0) {
                            randomAccess.seek(offset + 1);
                            break ww;
                        }
                        hasEncountReturn = true;
                        continue;
                    default:
                        hasEncountReturn = false;
                }
            }
            String line = null;
            while ((line = randomAccess.readLine()) != null) {
                // listener.handle(line);
                lineProcess.print(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(randomAccess);
        }
    }

    private interface IProcessLine {

        void print(String line);
    }

    public static enum Status {

        CREATED("created"), SUCCESS("success"), FAILD("faild"), NONE("none"), CANCELLED("cancelled");

        private String val;

        private Status(String val) {
            this.val = val;
        }

        public String getVal() {
            return this.val;
        }

        public static Status parse(String val) {
            if (CREATED.val.equals(val)) {
                return CREATED;
            } else if (SUCCESS.val.equals(val)) {
                return SUCCESS;
            } else if (FAILD.val.equals(val)) {
                return FAILD;
            } else if (NONE.val.equals(val)) {
                return NONE;
            } else if (CANCELLED.val.equals(val)) {
                return CANCELLED;
            }
            throw new IllegalStateException("illegal val:" + val);
        }
    }

    public static class IncrSpec {

        private String gitAddress;

        // 分支
        private String gitRef;

        private Specification cpuRequest;

        private Specification cpuLimit;

        private Specification memoryRequest;

        private Specification memoryLimit;

        public String getGitAddress() {
            return gitAddress;
        }

        public boolean isSpecificationsDiff(IncrSpec s) {
            return isNotEqula(this.getCpuLimit(), s.getCpuLimit()) || isNotEqula(this.getCpuRequest(), s.getCpuRequest()) || isNotEqula(this.getMemoryLimit(), s.getMemoryLimit()) || isNotEqula(this.getMemoryRequest(), s.getMemoryRequest());
        }

        public boolean isGitSourceDiff(IncrSpec s) {
            return !StringUtils.equals(this.getGitRef(), s.getGitRef()) || !StringUtils.equals(this.getGitAddress(), s.getGitAddress());
        }

        private boolean isNotEqula(Specification s1, Specification s2) {
            return s1.getVal() != s2.getVal() || !StringUtils.equals(s1.getUnit(), s2.getUnit());
        }

        public void setGitAddress(String gitAddress) {
            this.gitAddress = gitAddress;
        }

        public String getGitRef() {
            return gitRef;
        }

        public void setGitRef(String gitRef) {
            this.gitRef = gitRef;
        }

        public Specification getCpuRequest() {
            return cpuRequest;
        }

        public void setCpuRequest(Specification cpuRequest) {
            this.cpuRequest = cpuRequest;
        }

        public Specification getCpuLimit() {
            return cpuLimit;
        }

        public void setCpuLimit(Specification cpuLimit) {
            this.cpuLimit = cpuLimit;
        }

        public Specification getMemoryRequest() {
            return memoryRequest;
        }

        public void setMemoryRequest(Specification memoryRequest) {
            this.memoryRequest = memoryRequest;
        }

        public Specification getMemoryLimit() {
            return memoryLimit;
        }

        public void setMemoryLimit(Specification memoryLimit) {
            this.memoryLimit = memoryLimit;
        }
    }

    /**
     * 规格
     */
    public static class Specification {

        private static final Pattern p = Pattern.compile("(\\d+)(\\w*)");

        public static Specification parse(String val) {
            Matcher m = p.matcher(val);
            if (!m.matches()) {
                throw new IllegalArgumentException("val:" + val + " is not match the pattern:" + p);
            }
            Specification s = new Specification();
            s.setVal(Integer.parseInt(m.group(1)));
            s.setUnit(m.group(2));
            return s;
        }

        private int val;

        private String unit;

        public int getVal() {
            return val;
        }

        public boolean isUnitEmpty() {
            return StringUtils.isEmpty(this.unit);
        }

        public void setVal(int val) {
            this.val = val;
        }

        public int normalizeMemory() {
            int result = 0;
            if ("M".equals(this.getUnit())) {
                result = this.getVal();
            } else if ("G".equals(this.getUnit())) {
                result = this.getVal() * 1024;
            }
            return result;
        }

        public int normalizeCPU() {
            // d.setCpuRequest(Specification.parse("300m"));
            // d.setCpuLimit(Specification.parse("2"));
            int result = 0;
            if ("m".equals(this.getUnit())) {
                result = this.getVal();
            } else if (this.isUnitEmpty()) {
                result = this.getVal() * 1024;
            }
            return result;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String toString() {
            return this.val + this.unit;
        }
    }
}
