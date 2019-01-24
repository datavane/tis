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
package com.qlangtech.tis.runtime.module.screen.jarcontent;

import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicContentScreen extends BasicModule {

    // implements
    // PropteryGetter
    /**
     */
    private static final long serialVersionUID = 1L;

    public static final String KEY_FILE_CONTENT = "filecontent";

    public BasicContentScreen() {
        super("savefilecontent");
    }

    // @Func(PermissionConstant.CONFIG_EDIT)
    public // , Navigator nav
    void execute(// , Navigator nav
    Context context) throws Exception {
        this.disableNavigationBar(context);
        context.put("uneditable", this.isEditModel() || "false".equalsIgnoreCase(this.getString("editable")));
        processContent(context);
    }

    public final String getResourceName() {
        return getResourceName(this.getSolrDependency());
    }

    public static String getResourceName(PropteryGetter propGetter) {
        return StringUtils.substringBefore(propGetter.getFileName(), ".");
    }

    protected String getContent(Context context) throws UnsupportedEncodingException {
        String configContent = (String) context.get(KEY_FILE_CONTENT);
        SnapshotDomain snapshot = getSnapshot(context);
        if (configContent != null) {
            return configContent;
        }
        return new String(this.getSolrDependency().getContent(snapshot), "utf8");
    }

    protected final SnapshotDomain getSnapshot(Context context) {
        Snapshot sn = (Snapshot) context.get("snapshot");
        if (sn != null) {
            return this.getSnapshotViewDAO().getView(sn.getSnId());
        }
        // isEditModel() ? this.getInt("snapshotid") :
        Integer snapshotId = getInt("snapshot");
        // context.put("snapshotid", snapshotId);
        SnapshotDomain snapshot = this.getSnapshotViewDAO().getView(snapshotId);
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshotId:" + snapshotId + " can not find pojo in db");
        }
        context.put("snap", snapshot.getSnapshot());
        return snapshot;
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }

    protected void processContent(Context context) throws UnsupportedEncodingException {
        setConfigFileContent(context, getContent(context), this.isEditModel());
        context.put("fileName", this.getSolrDependency().getFileName());
        // nav.forwardTo();
        getRundataInstance().forwardTo(getForward());
    }

    public static void setConfigFileContent(Context context, String content, boolean editModel) {
        context.put(KEY_FILE_CONTENT, editModel ? content : StringEscapeUtils.escapeHtml(content));
    }

    public static final String VIEW_OF_EDITFILE = "jarcontent/editfile.vm";

    /**
     * @return
     */
    protected String getForward() {
        if (!isEditModel()) {
            return "jarcontent/filecontent.vm";
        } else {
            return VIEW_OF_EDITFILE;
        }
    }

    protected boolean isEditModel() {
        // 如果日常能编辑
        return ManageUtils.isDevelopMode();
    }

    protected abstract PropteryGetter getSolrDependency();
    // @Override
    // public byte[] getContent(SnapshotDomain pubInfo) {
    // 
    // }
    // 
    // @Override
    // public final Snapshot createNewSnapshot(Integer newResourceId,
    // Snapshot snapshot) {
    // throw new UnsupportedOperationException();
    // }
    // public abstract String getFileName();
    // public abstract String getMd5CodeValue(Snapshot snapshot);
    // public abstract Long getFileSufix(Snapshot snapshot);
}
