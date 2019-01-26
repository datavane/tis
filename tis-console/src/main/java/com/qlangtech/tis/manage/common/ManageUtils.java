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
package com.qlangtech.tis.manage.common;

import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_APPLICATION;
import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_CORE_PROPERTIES;
import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_DATA_SOURCE;
import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_SCHEMA;
import static com.qlangtech.tis.manage.common.ConfigFileReader.FILE_SOLOR;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;

import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ManageUtils {

    public static final int DELETE = 1;

    public static final int UN_DELETE = 0;

    private RunContext daoContext;

    // private IServerPoolDAO serverPoolDAO;
    private HttpServletRequest request;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final SimpleDateFormat TIME_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd");

    private static final Pattern SERVLET_PATH = Pattern.compile("/([^\\.^/]+?)\\.[^\\.]+$");

    // 是否是开发模式
    public static boolean isDevelopMode() {
        // return RunEnvironment.isDevelopMode();
        return RunEnvironment.DAILY == RunEnvironment.getSysRuntime();
    }

    public RunEnvironment getRuntime() {
        // return RunEnvironment.isDevelopMode();
        return RunEnvironment.getSysRuntime();
    }

    public boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    public String defaultIfEmpty(String value, String defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    public static boolean isInPauseState(TriggerCrontab crontab) {
        return isInPauseState(crontab.getFjobId() != null, (crontab.isFstop()), crontab.getIjobId() != null, (crontab.isIstop()));
    }

    // private final static DecimalFormat decimalFormat = new
    // DecimalFormat("0.0");
    // public String getZKDumpPath(String appname) {
    // try {
    // return URLEncoder.encode(Zklockview.ZK_DUMP_LOCK_PATH_PREFIX
    // + appname, BasicModule.getEncode());
    // } catch (UnsupportedEncodingException e) {
    // throw new RuntimeException(e);
    // }
    // }
    public static boolean isTrue(String key) {
        return "true".equals(DefaultFilter.getReqeust().getParameter(key));
    }

    /**
     * 单位是byte
     *
     * @param volume
     * @return
     */
    public static String formatVolume(long volume) {
        return FileUtils.byteCountToDisplaySize(volume);
    // volume = volume / 1024;
    // 
    // if (volume < 1) {
    // return "1K";
    // }
    // 
    // if ((volume / 1024) < 1) {
    // return String.valueOf(volume) + "K";
    // }
    // 
    // if (volume > (1024 * 1024)) {
    // return decimalFormat.format(new Float(volume) / (1024l * 1024l)) +
    // "G";
    // }
    // 
    // return String.valueOf((volume / 1024)) + "M";
    }

    public String getServerIp() throws UnknownHostException {
        return InetAddress.getLocalHost().toString();
    }

    public static boolean isInPauseState(boolean hasfulldump, boolean fulljobStop, boolean hasincrdump, boolean incrjobStop) {
        if (hasfulldump) {
            return fulljobStop;
        // if (fulljobStop) {
        // return true;
        // } else {
        // return false;
        // }
        } else if (hasincrdump) {
            return incrjobStop;
        // if (incrjobStop) {
        // return true;
        // } else {
        // return false;
        // }
        }
        // 默认为停止状态
        return true;
    }

    public boolean isNullable(Object o) {
        return o instanceof Nullable;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Autowired
    public void setDaoContext(RunContext daoContext) {
        this.daoContext = daoContext;
    }

    public static String formatDateYYYYMMdd(Date time) {
        if (time == null) {
            return StringUtils.EMPTY;
        }
        return TIME_FORMAT.format(time);
    }

    public String formatDateYYYYMMdd(long time) {
        return TIME_FORMAT.format(new Date(time));
    }

    public String formatTodayYYYYMMdd() {
        return TIME_FORMAT2.format(new Date());
    }

    public String formatTodayYYYYMMdd(int offset) {
        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.DAY_OF_YEAR, offset);
        return TIME_FORMAT2.format(calender.getTime());
    }

    public static String getfileEditName(String servletPath) {
        Matcher m = SERVLET_PATH.matcher(servletPath);
        if (m.find()) {
            return (m.group(1));
        }
        throw new IllegalArgumentException("servletPath:" + servletPath + " is invalid");
    }

    // this.href
    private static final MessageFormat openDialogScript = new MessageFormat("opendialog(null,{3},$(window).width()-150,($(document).height()>$(window).height()?$(document).height():$(window).height())-15,{2});");

    // opendialog('$caption',this.href,
    private static final MessageFormat urlformat = new MessageFormat("&nbsp;<a target=''_blank'' href=''{0}'' {4}  onclick=\"" + openDialogScript.toPattern() + " return false;\">{1}</a>");

    public String openMaximizeDialog(String href) {
        return openDialogScript.format(new Object[] { StringUtils.EMPTY, StringUtils.EMPTY, "function(){}", href });
    }

    public String getConfigViewLink(com.qlangtech.tis.manage.common.Module broker, Integer snapshotid) {
        return getConfigViewLink(broker, snapshotid, true);
    }

    public String getConfigViewLink(com.qlangtech.tis.manage.common.Module broker, Integer snapshotid, boolean refeshWhenDialogClose) {
        return getConfigViewLink(broker, daoContext.getSnapshotDAO().loadFromWriteDB(snapshotid), refeshWhenDialogClose);
    }

    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d{1,})\\*(\\d{1,})");

    public static String md5(String value) {
        return DigestUtils.md5Hex(value);
    }

    public int getPopLength(String size, boolean w) {
        if (StringUtils.isEmpty(size)) {
            return w ? 500 : 400;
        }
        Matcher m = SIZE_PATTERN.matcher(size);
        if (m.matches()) {
            return Integer.parseInt(w ? m.group(1) : m.group(2));
        }
        return 0;
    }

    public String getConfigViewLink(com.qlangtech.tis.manage.common.Module broker, Snapshot snapshot) {
        return this.getConfigViewLink(broker, snapshot, true);
    }

    /**
     * @param broker
     * @param snapshot
     * @param refeshWhenDialogClose
     * @param linkEditable
     *            展示的链接点进去内容是否可以直接编辑
     * @return
     */
    public String getConfigViewLink(com.qlangtech.tis.manage.common.Module broker, Snapshot snapshot, boolean refeshWhenDialogClose, boolean linkEditable) {
        StringBuilder url = new StringBuilder();
        Integer snapshotid = snapshot.getSnId();
        url.append("<strong>Ver:</strong>").append(snapshotid);
        url.append("<a href='#' title='" + snapshot.getCreateUserName() + ":" + StringUtils.trimToEmpty(snapshot.getMemo()) + "' ><img src='" + broker.setTarget("imgs/note.jpg") + "' border='0' /></a>");
        final String dialogCloseFunction = linkEditable ? "function(){fetchLatestSnapshot();}" : "function(){}";
//        if (snapshot.getResApplicationId() != null) {
//            url.append(dialog(broker.setTarget("jarcontent/application_context") + "?snapshot=" + snapshotid, "[applicationContext.xml]", refeshWhenDialogClose, linkEditable, dialogCloseFunction));
//        }
//        if (snapshot.getResDsId() != null) {
//            url.append(dialog(broker.setTarget("jarcontent/ds") + "?snapshot=" + snapshotid, "[ds.xml]", refeshWhenDialogClose, linkEditable));
//        }
        if (snapshot.getResSchemaId() != null) {
            // if (snapshot.getResSchemaVirtualId() == null) {
            url.append(dialog(broker.setTarget("jarcontent/schema") + "?snapshot=" + snapshotid, "[schema.xml]", refeshWhenDialogClose, linkEditable, dialogCloseFunction));
        // } else {
        // url.append(dialog(broker.setTarget("schema_view") + "?shmid=" + snapshot.getResSchemaVirtualId()
        // + "&snapshot=" + snapshotid, "[schema.xml]", refeshWhenDialogClose, linkEditable));
        // }
        }
        if (snapshot.getResSolrId() != null) {
            url.append(dialog(broker.setTarget("jarcontent/solrconfig") + "?snapshot=" + snapshotid, "[solr.xml]", refeshWhenDialogClose, linkEditable, dialogCloseFunction));
        }
//        if (snapshot.getResCorePropId() != null) {
//            url.append(dialog(broker.setTarget("jarcontent/core") + "?snapshot=" + snapshotid, "[core.properties]", refeshWhenDialogClose, linkEditable, dialogCloseFunction));
//        }
//        final String uploadlink = "<a href='" + broker.setTarget("jarcontent/uploadjar") + "?snapshot=" + snapshotid + "' onclick=\"opendialog('上传Jar文件',this.href,400,300);return false;\" >uploadjar</a>";
//        if (snapshot.getResJarId() != null) {
//            url.append("[<a href=\"" + broker.setTarget("jarcontent/jar.action") + "?snapshot=" + snapshotid + "\" />jar</a>," + uploadlink + "]");
//        } else {
//            url.append(uploadlink);
//        }
        return url.toString();
    }

    public String getConfigViewLink(com.qlangtech.tis.manage.common.Module broker, Snapshot snapshot, boolean refeshWhenDialogClose) {
        return getConfigViewLink(broker, snapshot, refeshWhenDialogClose, true);
    }

    public String getServerConfigViewLink(Module broker, String appName, short group, String serverIp, int port) {
        StringBuilder url = new StringBuilder();
        PropteryGetter[] getAry = new PropteryGetter[] { FILE_APPLICATION, FILE_SCHEMA, FILE_SOLOR, FILE_DATA_SOURCE, FILE_CORE_PROPERTIES };
        for (PropteryGetter g : getAry) {
            appendLink(broker, url, appName, group, serverIp, port, g.getFileName());
        }
        return url.toString();
    }

    private void appendLink(// TurbineURIBroker broker,
    Module broker, StringBuilder url, String appName, short group, String serverIp, int port, String resName) {
        try {
            url.append(dialog(broker.setTarget("solr/" + StringUtils.substringBefore(resName, ".")) + "?ip=" + URLEncoder.encode(serverIp, "utf8") + "&port=" + port + "&group=" + group + "&appname=" + appName + "&resname=" + URLEncoder.encode(resName, "utf8"), "[" + resName + "]", true, true));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String noescape(String value) {
        return value;
    }

    public String recordstatus(int value) {
        return recordstatus(Byte.valueOf((byte) value));
    }

    public String recordstatus(Byte value) {
        switch(value) {
            case 0:
                return "正常";
            case 1:
                return "已删除";
            default:
                throw new IllegalStateException("record value:" + value + " is not illegal");
        }
    }

    // public String environment(Short runtEnvironment) {
    // return RunEnvironment.getEnum(runtEnvironment).getDescribe();
    // }
    public String dialog(String url, String content) {
        return this.dialog(url, content, false, true, false);
    }

    public String dialog(String url, String content, boolean btnStyle) {
        return this.dialog(url, content, false, true, btnStyle);
    }

    public String dialog(String url, String content, boolean notRefeshWhenDialogClose, boolean linkEditable, boolean btnStyle) {
        return this.dialog(url, content, notRefeshWhenDialogClose, linkEditable, "function(){}", btnStyle);
    }

    public String dialog(String url, String content, boolean notRefeshWhenDialogClose, boolean linkEditable) {
        return this.dialog(url, content, notRefeshWhenDialogClose, linkEditable, "function(){}", false);
    }

    private String dialog(String url, String content, boolean notRefeshWhenDialogClose, boolean linkEditable, String callbackFunction) {
        return this.dialog(url, content, notRefeshWhenDialogClose, linkEditable, callbackFunction, false);
    }

    private String dialog(String url, String content, boolean notRefeshWhenDialogClose, boolean linkEditable, String callbackFunction, boolean btnStyle) {
        return urlformat.format(new Object[] { // /////// 0
        url + (linkEditable ? StringUtils.EMPTY : (StringUtils.contains(url, "?") ? '&' : '?') + "editable=false"), // 1
        content, // 2
        notRefeshWhenDialogClose ? callbackFunction : "function(){window.location.reload();}", // 3
        "this.href", btnStyle ? "class='btn btn-default'" : StringUtils.EMPTY });
    }

    public final IUser getLoginUser() {
        // return SecurityContextHolder.getContext().getUser();
        return UserUtils.getUser(this.request, daoContext);
    }

    public String urlencode(String value) {
        try {
            return URLEncoder.encode(value, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDailyHost() {
        return Config.getPublishHook().get(RunEnvironment.DAILY);
    }

    public static boolean isValidAppDomain(HttpServletRequest request) {
        AppDomainInfo domain = getAppDomain(request);
        if (domain == null) {
            return false;
        }
        return !(domain instanceof Nullable);
    }

    public static AppDomainInfo getAppDomain(HttpServletRequest request) {
        AppDomainInfo domain = (AppDomainInfo) request.getAttribute(BasicModule.REQUEST_DOMAIN_KEY);
        return domain;
    // return CheckAppDomainExistValve.getAppDomain(request, context);
    }

    public String defotVal(String value, String defotVal) {
        return StringUtils.defaultIfEmpty(value, defotVal);
    }

    public boolean isShowServerRelateInfo(Boolean showServerRelateInfo, String coreName, Integer coreIndex) {
        return // Boolean.TRUE.equals(context.get("showServerRelateInfo"))
        showServerRelateInfo || StringUtils.equals(coreName, this.getAppDomain().getAppName() + "-" + coreIndex);
    }

    public static void main(String[] arg) {
        // System.out.println(Math.pow(2, 3));
        System.out.println(md5("123456"));
    }

    public AppDomainInfo getAppDomain() {
        return getAppDomain(this.request);
    }

    /**
     * 是否是daily環境的應用
     *
     * @return
     */
    public boolean isDaily() {
        return isDaily(this.request);
    }

    public static boolean isDaily(HttpServletRequest request) {
        return isValidAppDomain(request) && (getAppDomain(request).getRunEnvironment() == RunEnvironment.DAILY);
    }

    public boolean isRuntimeEqual(String runtime) {
        return isValidAppDomain(this.request) && (getAppDomain(this.request).getRunEnvironment() == RunEnvironment.getEnum(runtime));
    }

    public String getPublishHook(String envir) {
        return Config.getPublishHook().get(RunEnvironment.getEnum(envir));
    }

    /**
     * 取得当先的pubhook地址
     *
     * @return
     */
    public String getPublishHook() {
        return Config.getPublishHook().get(this.getAppDomain().getRunEnvironment());
    }

    public String getHsfMonitorHost() {
        return Config.getHsfMonotorHost(this.getAppDomain().getRunEnvironment());
    }

    private NavigationBar navigatebar = null;

    public String createNavigateBar() {
        if (navigatebar == null) {
            navigatebar = new NavigationBar(this.request);
            navigatebar.setRunContextGetter(new RunContextGetter(this.daoContext));
        }
        return navigatebar.loadMenuFromDB();
    }

    // public void writeOrglist(HttpServletResponse response,
    // List<OrganizationDO> orglist) throws IOException {
    // FuncAuthority.xstream.toXML(orglist, response.getWriter());
    // }
    static final String img = "<img border='0' src='/runtime/imgs/delte_icon.jpg'/>";

    static final Pattern pattern = Pattern.compile("(\\d{1,})-[^,]+");

    public String formatAuthortityScript(String orign, String perminssioncode, int appid) {
        Matcher m = pattern.matcher(orign);
        if (m.find()) {
            return (m.replaceAll("<span id='" + perminssioncode + '_' + appid + '_' + "$1'>$0<a href='#' onclick=\"deltClick('" + perminssioncode + "'," + appid + " ,$1)\">" + img + "</a></span>"));
        } else {
            return orign;
        }
    }

    public boolean isNotNull(Object o) {
        return o != null;
    }

    // @Autowired
    // public void setServerPoolDAO(IServerPoolDAO serverPoolDAO) {
    // this.serverPoolDAO = serverPoolDAO;
    // }
    public String getShortDepartmentName(String departmentName) {
        // departmentName.
        String[] dpts = StringUtils.split(departmentName, "-");
        if (dpts.length > 2) {
            return dpts[dpts.length - 2] + "-" + dpts[dpts.length - 1];
        } else {
            return departmentName;
        }
    }

    // public String getApplyState(ApplicationApply apply) {
    // return ApplyAction.ApplyStatus.getState(apply.getStatus()).getDesc();
    // }
    // 
    // public String getApplyTestingState() {
    // return ApplyAction.ApplyStatus.CREATED_TESTING.getDesc();
    // }
    // 
    // public String getPublishApprovalState() {
    // return ApplyAction.ApplyStatus.PUBLISH_APPROVE_WAITING.getDesc();
    // }
    /**
     * 是否在测试状态
     *
     * @return
     */
    // public static boolean isTest() {
    // return "true".equalsIgnoreCase(System.getProperty("test"));
    // 
    // }
    // /**
    // * 成功发布
    // *
    // * @return
    // */
    // public String getSuccessPublishState() {
    // return ApplyAction.ApplyStatus.PUBLISH_SUCCESSFUL.getDesc();
    // }
    public int getGroups(int val) {
        return (int) Math.pow(2, val);
    }

    /**
     * @param apply
     * @param user
     * @param applyState
     * @return
     */
    public boolean showApplyButton(ApplicationApply apply, int... applyState) {
        Assert.assertNotNull("apply can not be null", apply);
        IUser user = this.getLoginUser();
        Assert.assertNotNull("user can not be null", user);
        if (!user.hasGrantAuthority(PermissionConstant.APP_APPLY_STATE_SET)) {
            return false;
        }
        for (int state : applyState) {
            if ((apply.getStatus()) == state) {
                return true;
            }
        }
        return false;
    }

    public void wait4me() {
        try {
            Thread.sleep(1000 * 6);
        } catch (InterruptedException e) {
        }
    }
    // public static void main(String[] arg) {
    // // System.out.println(urlformat.format(new Object[] { "www.taobao.com",
    // // "aaaa" }));
    // 
    // ManageUtils util = new ManageUtils();
    // // System.out.println();
    // 
    // util.buildNewEcrmData();
    // }
    /**
     * 判断用户是否拥有业务方管理员角色
     */
}
