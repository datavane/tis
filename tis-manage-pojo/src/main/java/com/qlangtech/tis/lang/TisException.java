/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.lang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.runtime.module.misc.BasicRundata;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.web.start.TisAppLaunch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 底层运行时异常运行时可直达web，届时可添加一些格式化处理
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-23 18:56
 */
public class TisException extends RuntimeException {
    private final Optional<ErrorValue> errCode;

    private static class RemoveDataxWorkerForward implements Function<BasicRundata, String[]> {
        @Override
        public String[] apply(BasicRundata rundata) {
            String targetName = rundata.getStringParam(IFullBuildContext.KEY_TARGET_NAME);
            if (StringUtils.isEmpty(targetName)) {
                throw new IllegalArgumentException("request param "
                        + IFullBuildContext.KEY_TARGET_NAME + " can not be null");
            }
            return new String[]{"coredefine", "datax_action", "remove_datax_worker"};
        }
    }

    /**
     * TIS会有专门的错误提示及异常处理流程
     */
    public enum ErrorCode {
        /**
         * @see com.qlangtech.tis.manage.common.ConfigFileContext#processContent 中使用
         */
        HTTP_CONNECT_FAILD((rundata) -> {
            throw new UnsupportedOperationException();
        }),
        // 证书失效
        LICENSE_INVALID((rundata) -> {
            return null;
        }),
        FLINK_INSTANCE_LOSS_OF_CONTACT((rundata) -> {
            // example: coredefine:datax_action:save_datax_worker
            // event_submit_do_incr_delete core_action
            String appName = rundata.getStringParam(IFullBuildContext.KEY_APP_NAME);
            if (StringUtils.isEmpty(appName)) {
                throw new IllegalArgumentException("request param "
                        + IFullBuildContext.KEY_APP_NAME + " can not be null");
            }
            return new String[]{"coredefine", "core_action", "incr_delete"};
        }),
        FLINK_SESSION_CLUSTER_LOSS_OF_CONTACT(new RemoveDataxWorkerForward()),
        /**
         * TIS 刚打开时候还没有阅读新人指南
         */
        TIS_FRESHMAN_README_HAVE_NOT_READ((rundata) -> {
            return new String[]{"coredefine", "core_action", "incr_delete"};
        }),
        POWER_JOB_CLUSTER_LOSS_OF_CONTACT(new RemoveDataxWorkerForward() {
            @Override
            public String[] apply(BasicRundata rundata) {
                String[] forwardParams = super.apply(rundata);
                if (!TargetResName.K8S_DATAX_INSTANCE_NAME.equalWithName(rundata.getStringParam(IFullBuildContext.KEY_TARGET_NAME))) {
                    throw new IllegalArgumentException("request param:" + IFullBuildContext.KEY_TARGET_NAME
                            + " relevant val must be equal to:" + TargetResName.K8S_DATAX_INSTANCE_NAME.getName());
                }
                return forwardParams;
            }
        });
        private final Function<BasicRundata, String[]> forward;

        private ErrorCode(Function<BasicRundata, String[]> forward) {
            this.forward = forward;
        }

        public void execForward(BasicRundata rundata) {
            String[] forwardParams = forward.apply(rundata);
            BasicRundata.forward(rundata, forwardParams);
        }
    }

    public static ErrorCode parse(String val) {

        ErrorCode errCode = ErrorCode.valueOf(val);
        return errCode;
    }

    public static ErrMsg getErrMsg(Throwable throwable) {
        TisException except = find(throwable);
        if (except == null) {
            Throwable cause = throwable.getCause();
            return new ErrMsg(org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(throwable)
                    , cause != null ? cause : throwable, Optional.empty());
        } else {
            return new ErrMsg(except.getMessage(), except, except.errCode);
        }
    }

    private static TisException find(Throwable throwable) {
        final Throwable[] throwables = ExceptionUtils.getThrowables(throwable);
        TisException last = null;
        for (Throwable ex : throwables) {
            if (TisException.class.isAssignableFrom(ex.getClass())) {
                // 需要找到最后一个
                last = (TisException) ex;
            }
        }
        return last;
    }

    private TisException(Optional<ErrorValue> errorCode, String message) {
        super(message);
        this.errCode = errorCode;
    }

    private TisException(Optional<ErrorValue> errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errorCode;
    }

    private TisException(String message) {
        super(message);
        this.errCode = Optional.empty();
    }

    public static TisException create(String message, Throwable cause) {
        return create(null, message, cause);
    }

    public static TisException create(ErrorValue errorCode, String message) {
        return new TisException(Optional.ofNullable(errorCode), message);
    }

    public static TisException create(ErrorValue errorCode, String message, Throwable cause) {
        if (cause instanceof TisException) {
            return (TisException) cause;
        } else {
            return new TisException(Optional.ofNullable(errorCode), message, cause);
        }
    }

    public static TisException create(String message) {
        return new TisException(message);
    }


    public static class ErrMsg {

        static final ThreadLocal<SimpleDateFormat> formatOfyyyyMMddHHmmssMMM = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(IParamContext.yyyyMMddHHmmssMMMPattern);
            }
        };

        static final AtomicReference<ErrMsg> preErr = new AtomicReference<>();

        private final String message;
        @JSONField(serialize = false)
        private final Throwable ex;
        private long logFileName;
        // 异常摘要
        private String abstractInfo;
        private final Optional<ErrorValue> errCode;

        public ErrMsg(String message, Throwable ex, Optional<ErrorValue> errCode) {
            this.message = message;
            this.ex = ex;
            this.errCode = Objects.requireNonNull(errCode, "errCode can not be null");
        }

        @JSONField(serialize = false)
        public Throwable getEx() {
            return ex;
        }

        public ErrorValue getErrCode() {
            return this.errCode.orElse(null);
        }

        public String getLogFileName() {
            return String.valueOf(this.logFileName);
        }

        public String getAbstractInfo() {
            return this.abstractInfo;
        }

        public void setAbstractInfo(String abstractInfo) {
            this.abstractInfo = abstractInfo;
        }

        public long getCreateTime() {
//            return LocalDateTime.parse(String.valueOf(this.logFileName), IParamContext.yyyyMMddHHmmssMMM)
//                    .toEpochSecond(ZoneOffset.UTC);
            try {
                return formatOfyyyyMMddHHmmssMMM.get().parse(String.valueOf(this.logFileName)).getTime();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public String getMessage() {
            return this.message;
        }

        @Override
        public String toString() {
            return logFileName + "/" + message;
        }

        public ErrMsg writeLogErr() {
            Objects.requireNonNull(ex, "exception can not be null");
            this.logFileName = Long.parseLong(IParamContext.getCurrentMillisecTimeStamp());
            final ErrMsg currError = this;
            // 将相同的异常合并成一条，以免屏幕上相同的异常显示n条
            return preErr.updateAndGet((pre) -> {
                try {
                    if (pre != null && StringUtils.equals(pre.getMessage(), currError.getMessage())) {
                        return pre;
                    }
                    File errLog = getErrLogFile(String.valueOf(this.logFileName));
                    StringWriter errWriter = new StringWriter();
                    try (PrintWriter print = new PrintWriter(errWriter)) {
                        ex.printStackTrace(print);
                    } catch (Exception e) {
                        throw new RuntimeException(errLog.getAbsolutePath(), e);
                    }
                    final String detail = errWriter.toString();
                    JSONObject err = new JSONObject();
                    err.put(KEY_ABSTRACT, ex.getMessage());
                    err.put(KEY_DETAIL, detail);
                    FileUtils.write(errLog, JsonUtil.toString(err), TisUTF8.get());
                    return currError;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static final String KEY_DETAIL = "detail";
    private static final String KEY_ABSTRACT = "abstract";

    private static final Pattern p = Pattern.compile("\\d{" + IParamContext.yyyyMMddHHmmssMMMPattern.length() + "}");

    public static List<ErrMsg> getErrorLogs() {
        File errLogDir = getErrLogDir();
        String[] logs = errLogDir.list();
        if (logs == null) {
            return Collections.emptyList();
        }
        List<ErrMsg> result = Lists.newArrayList(Arrays.stream(logs).filter((l) ->
                p.matcher(l).matches()
        ).map((l) -> {
            ErrMsg errMsg = new ErrMsg(null, null, Optional.empty());
            errMsg.logFileName = Long.parseLong(l);
            return errMsg;
        }).iterator());
        Collections.sort(result, ((a, b) -> (a.logFileName >= b.logFileName) ? -1 : 1));
        return result;
    }

    private static File getErrLogFile(String logFileName) {
        return new File(getErrLogDir(), logFileName);
    }

    private static File getErrLogDir() {
        return new File(TisAppLaunch.getLogDir(), "syserrs");
    }

    public static ILogErrorDetail getLogError(String logFileName) {
        if (StringUtils.isEmpty(logFileName)) {
            throw new IllegalArgumentException("param logFileName can not be null");
        }
        File errLogFile = getErrLogFile(logFileName);

        AtomicReference<JSONObject> error = new AtomicReference<>();

        return new ILogErrorDetail() {
            @Override
            public String getDetail() {
                return getPersisObj().getString(KEY_DETAIL);
            }

            private JSONObject getPersisObj() {
                return error.updateAndGet((pre) -> {
                    try {
                        if (pre == null) {
                            pre = JSON.parseObject(FileUtils.readFileToString(errLogFile, TisUTF8.get()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return pre;
                });
            }

            @Override
            public String getAbstractInfo() {
                return getPersisObj().getString(KEY_ABSTRACT);
            }
        };
    }


}
