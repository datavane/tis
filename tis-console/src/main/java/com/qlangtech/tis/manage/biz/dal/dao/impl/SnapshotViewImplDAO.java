/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.BasicDAO;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.common.TisUTF8;
import junit.framework.Assert;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * snapshot 的视图查询dao
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-3-27
 */
public class SnapshotViewImplDAO extends BasicDAO<SnapshotDomain, SnapshotCriteria> implements ISnapshotViewDAO {

  public static final ThreadLocal<MergeData> mergeDataContext = new ThreadLocal<MergeData>() {
    //    @Override
//    public MergeData get() {
//      return super.get();
//    }
    @Override
    protected MergeData initialValue() {
//      System.out.println("initialValue");
      MergeData initMergeData = new MergeData();//super.initialValue();
      initMergeData.put(KEY_MIN_GRAM_SIZE, 2);
      initMergeData.put(KEY_MAX_GRAM_SIZE, 7);
      return initMergeData;
    }
  };

  private ISnapshotDAO snapshotDAO;

  private IUploadResourceDAO uploadResourceDao;

  private IResourceParametersDAO resourceParametersDAO;

  @Override
  public String getEntityName() {
    return "snapshot_view";
  }

  @Override
  public SnapshotDomain getView(Integer snId, boolean mergeContextParams) {
    // 实现懒加载
    Assert.assertNotNull("param snId ", snId);
    final Snapshot snapshot = snapshotDAO.loadFromWriteDB(snId);
    if (snapshot == null) {
      throw new IllegalArgumentException("snid:" + snId + " relevant record is not exist");
    }
    SnapshotDomain domain = new SnapshotDomain() {

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

      @Override
      public UploadResource getSolrConfig() {
        try {
          if (this.solrConfig == null && snapshot.getResSolrId() != null) {
            solrConfig = uploadResourceDao.loadFromWriteDB(snapshot.getResSolrId());
            if (mergeContextParams) {
              mergeSystemParameter(solrConfig);
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
          if (mergeContextParams) {
            mergeSystemParameter(schema);
          }
        }
        return schema;
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

  private VelocityContext createContext() {
    VelocityContext velocityContext = new VelocityContext();
    DynaBean cfg = new LazyDynaBean();
    MergeData mergeData = mergeDataContext.get();
    for (Map.Entry<String, Object> entry : mergeData.entrySet()) {
      cfg.set(entry.getKey(), entry.getValue());
    }
    velocityContext.put("cfg", cfg);
//        ResourceParametersCriteria criteria = new ResourceParametersCriteria();
//        List<ResourceParameters> params = resourceParametersDAO.selectByExample(criteria, 1, 100);
//        for (ResourceParameters param : params) {
//            velocityContext.put(param.getKeyName(), GlobalConfigServlet.getParameterValue(param, runtime));
//        }
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
  private void mergeSystemParameter(UploadResource resource) {
    OutputStreamWriter writer = null;
    try {
      VelocityContext context = createContext();
      byte[] content = resource.getContent();
      ByteArrayOutputStream converted = new ByteArrayOutputStream();
      // StringWriter writer = new StringWriter(converted);
      writer = new OutputStreamWriter(converted, TisUTF8.get());
      // Velocity.evaluate(context, writer, resource.getResourceType(),
      // new String(content, encode));
      velocityEngine.evaluate(context, writer, resource.getResourceType(), new String(content, TisUTF8.get()));
      writer.flush();
      resource.setContent(converted.toByteArray());
      resource.setMd5Code(ConfigFileReader.md5file(resource.getContent()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  public static void main(String[] args) {
    SnapshotViewImplDAO dao = new SnapshotViewImplDAO();
    UploadResource resource = new UploadResource();
    resource.setResourceType("test");
    String content = "${cfg.minGramSize}";
    resource.setContent(content.getBytes(TisUTF8.get()));
    dao.mergeSystemParameter(resource);
    System.out.println(new String(resource.getContent(), TisUTF8.get()));
  }

  public void setUploadResourceDao(IUploadResourceDAO uploadResourceDao) {
    this.uploadResourceDao = uploadResourceDao;
  }

  // @Autowired
  public void setSnapshotDAO(ISnapshotDAO snapshotDAO) {
    this.snapshotDAO = snapshotDAO;
  }

  public static class MergeData extends HashMap<String, Object> {

  }
}
