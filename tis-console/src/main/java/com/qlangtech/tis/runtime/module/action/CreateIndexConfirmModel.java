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
package com.qlangtech.tis.runtime.module.action;

import com.qlangtech.tis.coredefine.module.control.SelectableServer;
import com.qlangtech.tis.coredefine.module.control.SelectableServer.CoreNode;
import com.qlangtech.tis.fullbuild.indexbuild.LuceneVersion;
import com.qlangtech.tis.runtime.module.action.AddAppAction.ExtendApp;
import com.qlangtech.tis.runtime.module.action.SchemaAction.UploadSchemaWithRawContentForm;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年5月19日
 */
public class CreateIndexConfirmModel {

  private String dataxName;
  private boolean expertModel;

  // 模板索引Appid
  private int tplAppId;

  private ExtendApp appform;

  private ExpertEditorModel expert;

  // 创建索引服务器的候选节点
  private SelectableServer.ServerNodeTopology coreNode;

  // 傻瓜模式类型对象
  private StupidModel stupid;

  public SelectableServer.ServerNodeTopology getCoreNode() {
    return coreNode;
  }

  public void setCoreNode(SelectableServer.ServerNodeTopology coreNode) {
    this.coreNode = coreNode;
  }

  public String getDataxName() {
    return dataxName;
  }

  public void setDataxName(String dataxName) {
    this.dataxName = dataxName;
  }

  /**
   * 取得服务器的候选地址
   *
   * @return
   */
  public String[] getCoreNodeCandidate() {
    if (this.coreNode == null) {
      throw new IllegalStateException("prop coreNode can not be null");
    }
    String[] result = new String[this.coreNode.getHosts().length];
    int i = 0;
    for (CoreNode node : this.coreNode.getHosts()) {
      result[i++] = node.getHostName();
    }
    return result;
    // if (coreNode == null || StringUtils.isBlank(coreNode.getNodeName())) {
    // throw new IllegalStateException("coreNode can not be null");
    // }
    // // return new String[] {
    // // StringUtils.substringBefore(coreNode.getNodeName(), "_") };
    // return new String[]{coreNode.getNodeName()};
  }

  public int getTplAppId() {
    if (this.tplAppId < 1) {
      throw new IllegalStateException("prop tplAppId can not small than 1");
    }
    return this.tplAppId;
  }

  public void setTplAppId(int tplAppId) {
    this.tplAppId = tplAppId;
  }

  public boolean isExpertModel() {
    return this.expertModel;
  }

  public ExtendApp getAppform() {
    return this.appform;
  }

  public LuceneVersion parseTplVersion() {
    if (this.appform == null) {
      throw new IllegalStateException("appform can not be null");
    }
    return LuceneVersion.LUCENE_7;
    // return LuceneVersion.parse(this.appform.getTisTpl());
  }

  public void setAppform(ExtendApp appform) {
    this.appform = appform;
  }

  public void setExpertModel(boolean expertModel) {
    this.expertModel = expertModel;
  }

  public ExpertEditorModel getExpert() {
    return expert;
  }

  public void setExpert(ExpertEditorModel expert) {
    this.expert = expert;
  }

  public StupidModel getStupid() {
    if (stupid == null) {
      throw new IllegalStateException("stupid obj can not be null");
    }
    return stupid;
  }

  public void setStupid(StupidModel stupid) {
    this.stupid = stupid;
  }

  public static class ExpertEditorModel {

    private String xml;

    public String getXml() {
      if (StringUtils.isBlank(this.xml)) {
        throw new IllegalStateException("xml content can not be null");
      }
      return this.xml;
    }

    public void setXml(String xml) {
      this.xml = xml;
    }
  }

  public static class StupidModel {

    private UploadSchemaWithRawContentForm model;

    public UploadSchemaWithRawContentForm getModel() {
      return model;
    }

    public void setModel(UploadSchemaWithRawContentForm model) {
      this.model = model;
    }
  }
}
