package com.qlangtech.tis.datax.impl;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.datax.IDataxGlobalCfg;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.biz.dal.pojo.AppType;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;

import java.util.Objects;

/**
 * @author: baisui 百岁
 * @create: 2021-04-21 09:09
 **/
public class DefaultDataxProcessor extends DataxProcessor {

  public static final String KEY_FIELD_NAME = "globalCfg";

  @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
  public String name;

  @FormField(ordinal = 1, type = FormFieldType.SELECTABLE, validate = {Validator.require})
  public String globalCfg;

  @FormField(ordinal = 2, type = FormFieldType.ENUM, validate = {Validator.require})
  public int dptId;
  @FormField(ordinal = 3, validate = {Validator.require})
  public String recept;

  public Application buildApp() {
    Application app = new Application();
    app.setProjectName(this.name);
    app.setDptId(this.dptId);
    app.setRecept(this.recept);
    app.setAppType(AppType.DataXPipe.getType());
    return app;
  }


  public IDataxGlobalCfg getDataXGlobalCfg() {
    IDataxGlobalCfg globalCfg = ParamsConfig.getItem(this.globalCfg, IDataxGlobalCfg.class);
    Objects.requireNonNull(globalCfg, "dataX Global config can not be null");
    return globalCfg;
  }

  @TISExtension()
  public static class DescriptorImpl extends Descriptor<IAppSource> {

    public DescriptorImpl() {
      super();
      this.registerSelectOptions(KEY_FIELD_NAME, () -> ParamsConfig.getItems(IDataxGlobalCfg.class));
    }

    public boolean validateName(IFieldErrorHandler msgHandler, Context context, String fieldName, String value) {
      return msgHandler.validateBizLogic(IFieldErrorHandler.BizLogic.APP_NAME_DUPLICATE, context, fieldName, value);
    }

    @Override
    public String getDisplayName() {
      return "DataxProcessor";
    }
  }


}
