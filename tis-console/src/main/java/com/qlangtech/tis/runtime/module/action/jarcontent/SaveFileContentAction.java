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
package com.qlangtech.tis.runtime.module.action.jarcontent;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.opensymphony.xwork2.ModelDriven;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.Savefilecontent;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SchemaFileInvalidException;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.servlet.LoadSolrCoreConfigByAppNameServlet;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.openapi.impl.AppKey;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.misc.MessageHandler;
import com.qlangtech.tis.runtime.module.screen.jarcontent.BasicContentScreen;
import com.qlangtech.tis.runtime.pojo.ResSynManager;
import com.qlangtech.tis.runtime.pojo.ResSynManager.RuntimeResSynManager;
import junit.framework.Assert;

/*
 * 用户修改文件，并且保存文件
 * 保存文件内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SaveFileContentAction extends BasicModule implements ModelDriven<Savefilecontent> {

	private static final long serialVersionUID = 1L;

	// private ISnapshotApplyDAO snapshotApplyDAO;
	public final Savefilecontent content = new Savefilecontent();

	@Override
	public Savefilecontent getModel() {
		return content;
	}

	/**
	 * 将日常的文件同步到线上<br>
	 * 这段逻辑原先应该是在线上执行的，现在要放在日常上执行，日常连线上肯定是可以执行的
	 *
	 * @param context
	 */
	@Func(PermissionConstant.CONFIG_SYNCHRONIZE_FROM_DAILY)
	public void doSyncDailyConfig(Context context) throws Exception {
		AppDomainInfo app = this.getAppDomain();
		Assert.assertEquals(this.getInt("appid"), app.getAppid());
		// 配置文件同步控制器
		final SynManagerWorker synManager = SynManagerWorker.create(app.getAppName(), this.getContext(), this);
		if (!synManager.shallSynchronize()) {
			this.addErrorMessage(context, "DAILY环境配置文件已经同步到线上，不需要再同步了");
			return;
		}
		// final Snapshot snapshot =
		if (synManager.synchronizedOnlineSnapshot()) {
			this.addActionMessage(context, "已经将日常的配置成功发布到线上生产环境");
		}
		// 创建新SNAPSHOT
		// this.addActionMessage(
		// context,
		// "同步文件成功,最新snapshot:"
		// + createNewSnapshot(snapshot, "synchronize from daily",
		// this, new Long(this.getUserId()),
		// this.getLoginUserName()));
	}

	public static class SynManagerWorker {

		private final List<RuntimeResSynManager> resSynManagers;

		private final Context context;

		private final BasicModule module;

		private SnapshotDomain dailyResDomain;

		private SynManagerWorker(final Context context, final BasicModule module, SnapshotDomain dailyResDomain) {
			this.resSynManagers = Lists.newArrayList();
			this.context = context;
			this.module = module;
			this.dailyResDomain = dailyResDomain;
		}

		public List<RuntimeResSynManager> getResSynManagers() {
			return this.resSynManagers;
		}

		public boolean isOnlineInitialized() {
			for (RuntimeResSynManager manager : resSynManagers) {
				if (manager.getSysManager().getOnlineResDomain() != null) {
					return true;
				}
			}
			return false;
		}

		public SnapshotDomain getDailyRes() {
			return this.dailyResDomain;
		}

		public boolean shallSynchronize() {
			for (RuntimeResSynManager m : resSynManagers) {
				if (m.sysManager.shallSynchronize()) {
					return true;
				}
			}
			return false;
		}

		public boolean synchronizedOnlineSnapshot() throws Exception {
			boolean success = true;
			for (RuntimeResSynManager m : resSynManagers) {
				// final Context context, final BasicModule module
				if (!m.sysManager.synchronizedOnlineSnapshot(context, module, m.runtime)) {
					success = false;
				}
			}
			return success;
		}

		public static SynManagerWorker create(String appName, final Context context, final BasicModule module)
				throws Exception {
			final AppKey appKey = new AppKey(appName, (short) 0, RunEnvironment.DAILY, true);
			// 取得内容从数据库中拿
			appKey.setFromCache(false);
			// 日常向线上推送的文件
			SnapshotDomain dailyResDomain = LoadSolrCoreConfigByAppNameServlet
					.getSnapshotDomain(ConfigFileReader.getConfigList(), appKey, module);
			if (dailyResDomain == null) {
				throw new IllegalStateException(
						"appKey:" + appKey.getAppName() + " relevant SnapshotDomain can not be null");
			}
			SynManagerWorker worker = new SynManagerWorker(context, module, dailyResDomain);
			EnumSet<RunEnvironment> all = EnumSet.allOf(RunEnvironment.class);
			for (RunEnvironment env : all) {
				if (env == RunEnvironment.DAILY) {
					continue;
				}
				worker.resSynManagers
						.add(ResSynManager.createSynManagerOnlineFromDaily(appName, module, env, dailyResDomain));
			}
			return worker;
		}
	}

	// private void saveApplySchemaFile(Savefilecontent content, Context
	// context)
	// throws UnsupportedEncodingException {
	// Integer appid = this.getInt("appid");
	//
	// SnapshotApply snapshotApply = snapshotApplyDAO.loadByAppid(appid);
	// snapshotApply.setCreateTime(new Date());
	// snapshotApply.setUpdateTime(new Date());
	//
	// // Context context,
	// // final byte[] uploadContent, final String md5,
	// // PropteryGetter fileGetter
	//
	// final byte[] uploadContent = content.getContent()
	// .getBytes(Charset.forName(getEncode()));
	//
	// try {
	//
	// snapshotApply
	// .setResSchemaId(
	// ResSynManager
	// .createNewResource(context, uploadContent,
	// ConfigFileReader.md5file(
	// uploadContent),
	// ConfigFileReader.FILE_SCHEMA, this, this)
	// .longValue());
	//
	// } catch (SchemaFileInvalidException e) {
	//
	// return;
	// }
	// try {
	// snapshotApply.setCreateUserId(Long.parseLong(this.getUserId()));
	// } catch (Throwable e) {
	// }
	// snapshotApply.setCreateUserName(this.getLoginUserName());
	// snapshotApply.setPreSnId(snapshotApply.getSnId());
	// snapshotApply.setSnId(null);
	// snapshotApplyDAO.insertSelective(snapshotApply);
	//
	// }
	/*
	 * 与doSaveContent的区别在于，源参数是页面传递， doSaveContentByContext的参数是有http接口传入
	 */
	public void doSaveContentByContext(Context context, Savefilecontent contentOut)
			throws UnsupportedEncodingException {
		Integer snapshotid = (Integer) context.get("selectedSnapshotid");
		// content.getFilename();
		String fileName = String.valueOf(context.get("filename"));
		if ("edit_schema_apply".equals(fileName)) {
			// saveApplySchemaFile(contentOut, context);
			this.addActionMessage(context, "已经成功更新了Schema文件");
			return;
		}
		PropteryGetter propertyGetter = createConfigFileGetter(fileName);
		Long userid = 999l;
		try {
			userid = new Long(this.getUserId());
		} catch (Throwable e) {
		}
		CreateSnapshotResult createResult = createNewSnapshot(context, this.getSnapshotViewDAO().getView(snapshotid),
				propertyGetter, contentOut.getContentBytes(), this, this, "drds更新", userid, this.getLoginUserName());
		if (!createResult.isSuccess()) {
			forward("edit_" + BasicContentScreen.getResourceName(propertyGetter));
			return;
		}
		// this.getDefaultGroup().getField("snapshotid").setValue(newId);
		this.addActionMessage(context, "保存文件成功,最新snapshot:" + createResult.getNewId());
	}

	/**
	 * 保存文本内容
	 */
	@Func(PermissionConstant.CONFIG_EDIT)
	public void doSaveContent(// Navigator nav,
			Context context) throws Exception {
		if (!RunEnvironment.isDevelopMode()) {
			this.addErrorMessage(context, "请先更新日常环境中的配置文件，然后同步到线上环境!");
			return;
		}
		Integer snapshotid = this.getInt("snapshot");
		// content.getFilename();
		String fileName = this.getString("filename");
		if ("edit_schema_apply".equals(fileName)) {
			// saveApplySchemaFile(content, context);
			this.addActionMessage(context, "已经成功更新了Schema文件");
			return;
		}
		PropteryGetter propertyGetter = createConfigFileGetter(fileName);
		Long userid = 999l;
		try {
			userid = new Long(this.getUserId());
		} catch (Throwable e) {
		}
		CreateSnapshotResult createResult = createNewSnapshot(context, this.getSnapshotViewDAO().getView(snapshotid),
				propertyGetter, content.getContentBytes(), this, this, this.getString("memo"), userid,
				this.getLoginUserName());
		if (!createResult.isSuccess()) {
			forward("edit_" + BasicContentScreen.getResourceName(propertyGetter));
			return;
		}
		// this.getDefaultGroup().getField("snapshotid").setValue(newId);
		this.addActionMessage(context, "保存文件成功,最新snapshot:" + createResult.getNewId());
	}

	/**
	 * @param context
	 * @param domain
	 *            原有对象
	 * @param fileGetter
	 * @param uploadContent
	 * @param runContext
	 * @param messageHandler
	 * @param memo
	 * @param userId
	 * @param userName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static CreateSnapshotResult createNewSnapshot(Context context, final SnapshotDomain domain,
			PropteryGetter fileGetter, byte[] uploadContent, RunContext runContext, MessageHandler messageHandler,
			String memo, Long userId, String userName) throws UnsupportedEncodingException {
		return createNewSnapshot(context, domain, fileGetter, uploadContent, runContext, messageHandler, memo, userId,
				userName, true);
	}

	public static CreateSnapshotResult createNewSnapshot(Context context, final SnapshotDomain domain,
			PropteryGetter fileGetter, byte[] uploadContent, RunContext runContext, MessageHandler messageHandler,
			String memo, Long userId, String userName, boolean createNewSnapshot) throws UnsupportedEncodingException {
		CreateSnapshotResult createResult = new CreateSnapshotResult();
		try {
			final String md5 = ConfigFileReader.md5file(uploadContent);
			if (StringUtils.equals(md5, fileGetter.getMd5CodeValue(domain))) {
				saveHasNotModifyMessage(context, messageHandler, domain.getSnapshot().getSnId());
				return createResult;
			}
			// 创建一条资源记录
			try {
				Integer newResId = ResSynManager.createNewResource(context, uploadContent, md5, fileGetter,
						messageHandler, runContext);
				final Snapshot snapshot = fileGetter.createNewSnapshot(newResId, domain.getSnapshot());
				if (createNewSnapshot) {
					snapshot.setMemo(memo);
					createResult.setNewSnapshotId(createNewSnapshot(snapshot, memo, runContext, userId, userName));
					snapshot.setSnId(createResult.getNewId());
				}
				context.put("snapshot", snapshot);
			} catch (SchemaFileInvalidException e) {
				return createResult;
			}
		} finally {
			// try {
			// reader.close();
			// } catch (Throwable e) {
			// }
		}
		createResult.setSuccess(true);
		return createResult;
	}

	public static class CreateSnapshotResult {

		private Integer newSnapshotId;

		private boolean success = false;

		public Integer getNewId() {
			return newSnapshotId;
		}

		public void setNewSnapshotId(Integer newId) {
			this.newSnapshotId = newId;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}
	}

	public static Integer createNewSnapshot(final Snapshot snapshot, final String memo, // BasicModule
			RunContext runContext, // BasicModule
			Long userid, // BasicModule
			String userName) // module
	{
		Integer newId;
		snapshot.setSnId(null);
		snapshot.setUpdateTime(new Date());
		snapshot.setCreateTime(new Date());
		try {
			snapshot.setCreateUserId(userid);
		} catch (Throwable e) {
			snapshot.setCreateUserId(0l);
		}
		snapshot.setCreateUserName(userName);
		// final String memo = this.getString("memo");
		if (StringUtils.isNotEmpty(memo)) {
			snapshot.setMemo(memo);
		}
		// 插入一条新纪录
		newId = runContext.getSnapshotDAO().insert(snapshot);
		if (newId == null) {
			throw new IllegalArgumentException(" have not create a new snapshot id");
		}
		return newId;
	}

	private PropteryGetter createConfigFileGetter(String fileName) {

		if (StringUtils.endsWith(fileName, "schema")) {
			return ConfigFileReader.FILE_SCHEMA;
		} else if (StringUtils.endsWith(fileName, "solrconfig")) {
			return ConfigFileReader.FILE_SOLOR;
		} else {
			throw new IllegalStateException("fileName:" + fileName + "can not match any process");
		}
	}

	// public ISnapshotApplyDAO getSnapshotApplyDAO() {
	// return snapshotApplyDAO;
	// }
	//
	// @Autowired
	// public void setSnapshotApplyDAO(ISnapshotApplyDAO snapshotApplyDAO) {
	// this.snapshotApplyDAO = snapshotApplyDAO;
	// }
	private static void saveHasNotModifyMessage(Context context, MessageHandler messageHandler, Integer snapshotid) {
		messageHandler.addActionMessage(context, "文件没有变更，保持当前snapshot:" + snapshotid);
	}
}
