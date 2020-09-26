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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.servlet.GlobalConfigServlet;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

/**
 * snapshot 的视图查询dao
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-27
 */
public class SnapshotViewImplDAO extends BasicDAO<SnapshotDomain, SnapshotCriteria> implements ISnapshotViewDAO {

    private ISnapshotDAO snapshotDAO;

    private IUploadResourceDAO uploadResourceDao;

    private IResourceParametersDAO resourceParametersDAO;

    // static{
    // Velocity.in
    // }
    @Override
    public String getEntityName() {
        return "snapshot_view";
    }

    private static String encode = "utf8";

    @Override
    public SnapshotDomain getView(Integer snId) {
        return getView(snId, null);
    }

    @Override
    public SnapshotDomain getView(Integer snId, final RunEnvironment runtime) {
        // 实现懒加载
        // Assert.assertNotNull("param runtime", runtime);
        Assert.assertNotNull("param snId ", snId);
        // Assert.assertNotNull("obj runtime can not be null ", runtime);
        final Snapshot snapshot = snapshotDAO.loadFromWriteDB(snId);
        if (snapshot == null) {
            throw new IllegalArgumentException("snid:" + snId + " relevant record is not exist");
        }
        SnapshotDomain domain = new SnapshotDomain() {

            UploadResource springConfig;

            UploadResource coreProp;

            UploadResource datasource;

            UploadResource jar;

            UploadResource solrConfig;

            UploadResource schema;

            @Override
            public Snapshot getSnapshot() {
                return snapshot;
            }

            @Override
            public Integer getAppId() {
                return snapshot.getAppId();
            }

            // @Override
            // public Integer getPackId() {
            // return snapshot.getPid();
            // }
            @Override
            public UploadResource getApplication() {
                if (springConfig == null && snapshot.getResApplicationId() != null) {
                    springConfig = uploadResourceDao.loadFromWriteDB(snapshot.getResApplicationId());
                }
                return springConfig;
            }

            @Override
            public UploadResource getCoreProp() {
                if (coreProp == null && snapshot.getResCorePropId() != null) {
                    coreProp = uploadResourceDao.loadFromWriteDB(snapshot.getResCorePropId());
                }
                return coreProp;
            // return uploadResourceDao.loadFromWriteDB(snapshot
            // .getResCorePropId());
            }

            @Override
            public UploadResource getDatasource() {
                if (this.datasource == null && snapshot.getResDsId() != null) {
                    datasource = uploadResourceDao.loadFromWriteDB(snapshot.getResDsId());
                }
                return datasource;
            // return
            // uploadResourceDao.loadFromWriteDB(snapshot.getResDsId());
            }

            @Override
            public UploadResource getJarFile() {
                if (this.jar == null && snapshot.getResJarId() != null) {
                    jar = uploadResourceDao.loadFromWriteDB(snapshot.getResJarId());
                }
                return jar;
            // return uploadResourceDao
            // .loadFromWriteDB(snapshot.getResJarId());
            }

            @Override
            public UploadResource getSolrConfig() {
                try {
                    if (this.solrConfig == null && snapshot.getResSolrId() != null) {
                        solrConfig = uploadResourceDao.loadFromWriteDB(snapshot.getResSolrId());
                        if (runtime != null) {
                            mergeSystemParameter(solrConfig, runtime);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return solrConfig;
            }

            @Override
            public UploadResource getSolrSchema() {
                if (this.schema == null && snapshot.getResSchemaId() != null) {
                    schema = uploadResourceDao.loadFromWriteDB(snapshot.getResSchemaId());
                // mergeSystemParameter(schema, runtime);
                }
                return schema;
            // return uploadResourceDao.loadFromWriteDB(snapshot
            // .getResSchemaId());
            }
        };
        return domain;
    }

    // @Override
    // public SnapshotDomain getView(Integer snId, RunEnvironment runtime) {
    // return getView(snId, runtime, true);
    // }
    public IResourceParametersDAO getResourceParametersDAO() {
        return resourceParametersDAO;
    }

    public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
        this.resourceParametersDAO = resourceParametersDAO;
    }

    private VelocityContext createContext(RunEnvironment runtime) {
        VelocityContext velocityContext = new VelocityContext();
        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
        List<ResourceParameters> params = resourceParametersDAO.selectByExample(criteria, 1, 100);
        for (ResourceParameters param : params) {
            velocityContext.put(param.getKeyName(), GlobalConfigServlet.getParameterValue(param, runtime));
        }
        return velocityContext;
    }

    private static final VelocityEngine velocityEngine;

    static {
        try {
            velocityEngine = new VelocityEngine();
            Properties prop = new Properties();
            prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
            velocityEngine.init(prop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将文档内容和系统参数合并
     *
     * @param resource
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void mergeSystemParameter(UploadResource resource, RunEnvironment runtime) {
        OutputStreamWriter writer = null;
        try {
            VelocityContext context = createContext(runtime);
            byte[] content = resource.getContent();
            ByteArrayOutputStream converted = new ByteArrayOutputStream();
            // StringWriter writer = new StringWriter(converted);
            writer = new OutputStreamWriter(converted, encode);
            // Velocity.evaluate(context, writer, resource.getResourceType(),
            // new String(content, encode));
            velocityEngine.evaluate(context, writer, resource.getResourceType(), new String(content, encode));
            writer.flush();
            resource.setContent(converted.toByteArray());
            resource.setMd5Code(ConfigFileReader.md5file(resource.getContent()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    // @Override
    // public SnapshotDomain getView(Integer snId) {
    // SnapshotDomain domain = new SnapshotDomain();
    // // Snapshot snapshot = new Snapshot();
    // // snapshot.setSnId(snId);
    // domain.getSnapshot().setSnId(snId);
    // 
    // return this.load("snapshot_view.ibatorgenerated_selectById", domain);
    // }
    // @Autowired
    public void setUploadResourceDao(IUploadResourceDAO uploadResourceDao) {
        this.uploadResourceDao = uploadResourceDao;
    }

    // @Autowired
    public void setSnapshotDAO(ISnapshotDAO snapshotDAO) {
        this.snapshotDAO = snapshotDAO;
    }
}
