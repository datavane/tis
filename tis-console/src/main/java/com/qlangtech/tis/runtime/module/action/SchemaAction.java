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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.fullbuild.indexbuild.LuceneVersion;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.Savefilecontent;
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.jarcontent.SaveFileContentAction;
import com.qlangtech.tis.runtime.module.misc.*;
import com.qlangtech.tis.solrdao.ISchema;
import com.qlangtech.tis.solrdao.ISchemaField;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.SolrFieldsParser.SolrType;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import com.qlangtech.tis.sql.parser.ColName;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import com.qlangtech.tis.workflow.pojo.WorkFlow;
import com.qlangtech.tis.workflow.pojo.WorkFlowCriteria;
import com.yushu.tis.xmodifier.XModifier;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.DocType;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年11月11日
 */
public class SchemaAction extends BasicModule {

  private static final long serialVersionUID = 1L;

  protected static final String INDEX_PREFIX = "search4";

  private static Logger log = LoggerFactory.getLogger(SchemaAction.class);

  /**
   * 创建新索引流程取得通过workflow反射Schema 生成索引
   *
   * @param context
   * @throws Exception
   */
  public void doGetTplFields(Context context) throws Exception {
    final String wfName = StringUtils.substringAfter(this.getString("wfname"), ":");
    WorkFlow workflow = getWorkflow(wfName);
    // 通过version取默认模板
    Application tplApp = getTemplateApp();
    SchemaResult tplSchema = getTemplateSchema(context, tplApp);
    if (!tplSchema.success) {
      return;
    }
    ParseResult parseResult = tplSchema.parseResult;
    SolrType strType = parseResult.getTisType(SolrType.DEFAULT_STRING_TYPE_NAME);
    SolrType longType = parseResult.getTisType("long");
    SqlTaskNodeMeta.SqlDataFlowTopology dfTopology = SqlTaskNodeMeta.getSqlDataFlowTopology(workflow.getName());
    List<ColName> cols = dfTopology.getFinalTaskNodeCols();
    for (ColName colName : cols) {
      PSchemaField f = new PSchemaField();
      f.setName(colName.getAliasName());
      f.setType(strType);
      f.setStored(true);
      f.setIndexed(false);
      f.setMltiValued(false);
      f.setDocValue(false);
      // f.setInputDisabled(true);
      parseResult.dFields.add(f);
    }
    // parseResult.setSharedKey(null);
    parseResult.setUniqueKey(null);
    PSchemaField verField = new PSchemaField();
    verField.setName("_version_");
    verField.setDocValue(true);
    verField.setStored(true);
    verField.setType(longType);
    // verField.setInputDisabled(true);
    parseResult.dFields.add(verField);
    PSchemaField textField = new PSchemaField();
    textField.setName("text");
    textField.setDocValue(false);
    textField.setMltiValued(true);
    textField.setStored(false);
    textField.setIndexed(true);
    textField.setType(strType);
    // textField.setInputDisabled(true);
    parseResult.dFields.add(textField);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(tplSchema.content);
    org.jdom2.Document document2 = saxBuilder.build(inputStream);
    final XModifier modifier = new XModifier(document2);
    modifier.addModify("/fields/field(:delete)");
    modifier.addModify("/sharedKey(:delete)");
    modifier.deleteUniqueKey();
    updateSchemaXML(parseResult, document2, modifier, Collections.emptySet());
    XMLOutputter xmlout = new XMLOutputter(xmlPrettyformat);
    xmlout.setFormat(xmlPrettyformat.setEncoding(TisUTF8.getName()));
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      try (OutputStreamWriter writer = new OutputStreamWriter(out, TisUTF8.get())) {
        xmlout.output(document2, writer);
        writer.flush();
        tplSchema.content = out.toByteArray();
      }
    }
    // writer.close();
    // 序列化结果
    this.doGetFields(context, tplSchema);
  }

  private SchemaResult getTemplateSchema(Context context, Application tplApp) throws Exception {
    UploadResource schemaRes = getAppSchema(tplApp);
    // 需要将原始fields节点去掉
    SchemaResult schemaResult = this.parseSchemaResult(context, schemaRes.getContent(), false, /* shallValidate */
      false);
    if (!schemaResult.success) {
      return schemaResult;
    }
    schemaResult.setTplAppId(tplApp.getAppId());
    ParseResult parseResult = schemaResult.parseResult;
    if (parseResult == null) {
      throw new IllegalStateException("schemaResult:'" + tplApp.getProjectName() + "'.parseResult can not be null");
    }
    parseResult.dFields.clear();
    return schemaResult;
  }

  private WorkFlow getWorkflow(String wfName) {
    WorkFlowCriteria wquery = new WorkFlowCriteria();
    wquery.createCriteria().andNameEqualTo(wfName);
    List<WorkFlow> workFlows = this.getWorkflowDAOFacade().getWorkFlowDAO().selectByExample(wquery, 1, 1);
    Optional<WorkFlow> first = workFlows.stream().findFirst();
    if (!first.isPresent()) {
      throw new IllegalStateException("workflow:" + wfName + " can not find relevant instance in db");
    }
    return first.get();
  }

  public void doGetXmlContent(Context context) throws Exception {
    Application app = new Application();
    app.setAppId(this.getAppDomain().getAppid());
    UploadResource schemaResource = getAppSchema(app);
    this.doGetXmlContent(context, schemaResource, Collections.emptyList());
  }

  private void doGetXmlContent(Context context, UploadResource schemaResource, List<SchemaField> fields) throws Exception {
    JSONObject schema = new JSONObject();
    schema.put("schema", new String(schemaResource.getContent(), getEncode()));
    JSONArray disabledInputs = new JSONArray();
    for (SchemaField f : fields) {
      // if (f.isInputDisabled()) {
      // disabledInputs.put(f.getName());
      // }
    }
    schema.put("inputDisabled", disabledInputs);
    this.setBizResult(context, schema);
  }

  /**
   * 普通视图模式下保存到缓存(点击专家模式)
   */
  @Func(value = PermissionConstant.APP_SCHEMA_UPDATE, sideEffect = false)
  public void doToggleExpertModel(Context context) throws Exception {
    UploadSchemaWithRawContentForm form = this.getFormValues();
    if (!validateStupidContent(context, form)) {
      return;
    }
    String schema = this.createSchema(form, context);
    // refreshSchemaInCache(schema);
    // this.addActionMessage(context, "swap success");
    UploadResource schemaResource = new UploadResource();
    schemaResource.setContent(schema.getBytes(Charset.forName(getEncode())));
    this.doGetXmlContent(context, schemaResource, form.getFields());
  }

  /**
   * 在傻瓜模式下保存
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.APP_SCHEMA_UPDATE, sideEffect = true)
  public void doSaveByExpertModel(Context context) throws Exception {
    VisualizingSchemaForm form = this.parseJsonPost(VisualizingSchemaForm.class);
    // UploadSchemaWithRawContentForm form = this.getFormValues();
    if (!validateStupidContent(context, form.getVisualizingForm())) {
      return;
    }

    Savefilecontent meta = form.getMeta();

    String schema = this.createSchema(form.getVisualizingForm(), context);
    SchemaAction.CreateSnapshotResult createResult //
      = createNewSnapshot(context
      , this.getSnapshotViewDAO().getView(meta.getSnapshotid()), ConfigFileReader.FILE_SCHEMA, schema.getBytes(TisUTF8.get())
      , this, this, meta.getMemo(), Long.parseLong(this.getUser().getId()), this.getLoginUserName());

    if (!createResult.isSuccess()) {
      // forward("edit_" + BasicContentScreen.getResourceName(propertyGetter));
      return;
    }
    this.setBizResult(context, createResult);
    this.addActionMessage(context, "保存文件成功,最新snapshot:" + createResult.getNewId());
  }

  private static final Pattern PATTERN_FIELD = Pattern.compile("[a-z|_][\\w|_]+");

  /**
   * 傻瓜模式下校验表单
   *
   * @param context
   * @param form
   * @return
   */
  private boolean validateStupidContent(Context context, UploadSchemaWithRawContentForm form) {
    FieldErrors fieldsErrors = new FieldErrors();
    boolean hasBlankError = false;
    boolean hasFieldTypeError = false;
    // boolean hasNamePatternError = false;
    // 是否有重复
    boolean hasDuplicateError = false;
    Set<String> duplicate = new HashSet<String>();
    for (SchemaField f : form.getFields()) {
      if (StringUtils.isBlank(f.getName())) {
        if (!hasBlankError) {
          this.addErrorMessage(context, "字段名称不能为空");
          hasBlankError = true;
        }
        FieldErrorInfo err = fieldsErrors.getFieldErrorInfo(f.getId());// new FieldErrorInfo(f.getId());
        err.setFieldNameError(true);
      } else {
        if (!duplicate.add(f.getName())) {
          // if (!hasDuplicateError) {
          this.addErrorMessage(context, "字段名‘" + f.getName() + "’不能重复");
          // }
          // 有重复
          hasDuplicateError = true;
          FieldErrorInfo err = fieldsErrors.getFieldErrorInfo(f.getId());
          err.setFieldNameError(true);
          // fieldsErrors.add(err);
        } else {
          if (!PATTERN_FIELD.matcher(f.getName()).matches() && !f.isDynamic()) {
            // if (!hasNamePatternError) {
            this.addErrorMessage(context, "字段名‘" + f.getName() + "’开通必须以[a-z]作为开头且中间不能有除数字、下划线、大小写字母以外的字符出现");
            // }
            //    hasNamePatternError = true;
            FieldErrorInfo err = fieldsErrors.getFieldErrorInfo(f.getId());
            err.setFieldNameError(true);

          } else if (StringUtils.isBlank(f.getFieldtype())) {
            this.addErrorMessage(context, "请为字段‘" + f.getName() + "’选择类型");
            hasFieldTypeError = true;
            FieldErrorInfo err = fieldsErrors.getFieldErrorInfo(f.getId());
            err.setFieldTypeError(true);
            //fieldsErrors.add(err);
          } else if ("string".equalsIgnoreCase(f.getFieldtype())  //
            && (StringUtils.isBlank(f.getTokenizerType())
            || "-1".equalsIgnoreCase(f.getTokenizerType()))) {
            // if (!hasFieldTypeError) {
            this.addErrorMessage(context, "请为字段‘" + f.getName() + "’选择分词类型");
            hasFieldTypeError = true;
            // }
            FieldErrorInfo err = fieldsErrors.getFieldErrorInfo(f.getId());
            err.setFieldTypeError(true);
            // fieldsErrors.add(err);
          }
        }

        if (!f.getSortable() && !f.isIndexed() && !f.isStored()) {
          this.addErrorMessage(context, SolrFieldsParser.getFieldPropRequiredErr(f.getName()));
          FieldErrorInfo err = fieldsErrors.getFieldErrorInfo(f.getId());
          err.setFieldPropRequiredError(true);
          // fieldsErrors.add(err);
        }

      }
    }
    if (fieldsErrors.hasErrors() || hasBlankError || hasFieldTypeError || hasDuplicateError || hasDuplicateError) {
      setBizResult(context, fieldsErrors.getAllErrs());
      return false;
    }
    return true;
  }

  /**
   * 高级视图模式下保存到缓存(点击小白模式)
   */
  @Func(value = PermissionConstant.APP_SCHEMA_UPDATE, sideEffect = false)
  public void doToggleStupidModel(Context context) throws Exception {
    // 整段xml文本
    com.alibaba.fastjson.JSONObject body = this.parseJsonPost();
    byte[] schemaContent = body.getString("content").getBytes(TisUTF8.get());
    this.getStructSchema(context, schemaContent);
  }

  /**
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.APP_SCHEMA_UPDATE, sideEffect = false)
  public void doGetStructSchema(Context context) throws Exception {
    String appName = this.getString("app");
    if (StringUtils.isEmpty(appName)) {
      throw new IllegalArgumentException("param 'app' can not be null");
    }
    Application tplApp = this.getApplicationDAO().selectByName(appName);
    if (tplApp == null) {
      throw new IllegalStateException("index:" + appName + ", relevant app can not be null");
    }
    this.getStructSchema(context, tplApp);
  }

  private boolean getStructSchema(Context context, Application app, ISchemaJsonVisitor... schemaVisitor) throws Exception {
    UploadResource schemaRes = getAppSchema(app);
    return this.getStructSchema(context, schemaRes.getContent(), schemaVisitor);
  }

  /**
   * 当一个索引创建之后又被删除了，又需要重新创建就需要需执行该流程了，过程中需要将之前的记录找回来
   *
   * @param context
   */
  public void doGetApp(Context context) throws Exception {
    final String collectionName = this.getString("name");
    if (StringUtils.isEmpty(collectionName)) {
      throw new IllegalArgumentException("param name can not be null");
    }
    AddAppAction.ExtendApp app = new AddAppAction.ExtendApp();
    Application a = this.getApplicationDAO().selectByName(collectionName);
    BeanUtils.copyProperties(app, a);
    WorkFlow df = this.getWorkflowDAOFacade().getWorkFlowDAO().selectByPrimaryKey(a.getWorkFlowId());
    if (df == null) {
      this.addErrorMessage(context, "当前索引'" + collectionName + "'还未绑定数据流实例");
      return;
    }
    app.setWorkflow(df.getId() + ":" + df.getName());
    DepartmentCriteria dc = new DepartmentCriteria();
    dc.createCriteria();
    List<Department> dpts = this.getDepartmentDAO().selectByExample(dc);
    if (dpts.size() < 1) {
      this.addErrorMessage(context, "系统还未创建部门实例");
      return;
    }
    app.setSelectableDepartment(dpts.stream().map((r) -> new Option(r.getName(), String.valueOf(r.getDptId()))).collect(Collectors.toList()));
    Map<String, Object> result = Maps.newHashMap();
    result.put("app", app);
    if (!getStructSchema(context, app, (schema) -> {
      result.put("schema", schema);
    })) {
      return;
    }
    this.setBizResult(context, result);
  }

  /**
   * 取得结构化的schema结果
   *
   * @param context
   * @param schemaContent
   * @throws Exception
   */
  private boolean getStructSchema(Context context, byte[] schemaContent, ISchemaJsonVisitor... scmVisitor) throws Exception {
    SchemaResult result = parseSchemaResult(context, schemaContent, false, /* shallValidate */
      true);
    if (!result.success) {
      return false;
    }
    ParseResult parseResult = result.parseResult;
    com.alibaba.fastjson.JSONObject schema = this.doGetFields(context, SchemaResult.create(parseResult, new String(schemaContent, TisUTF8.get())));
    for (ISchemaJsonVisitor visitor : scmVisitor) {
      visitor.process(schema);
    }
    return true;
  }

  /**
   * 解析提交的schemaxml 内容
   *
   * @param context
   * @return
   * @throws Exception
   */
  private SchemaResult parseSchemaResult(Context context, byte[] schemaContent, boolean shallValidate, boolean xmlPost) throws Exception {
    SchemaResult result = new SchemaResult(xmlPost);
    if (schemaContent == null) {
      throw new IllegalStateException("schemaContent can not be null");
    }
    ParseResult parseResult;
    try {
      // final ByteArrayInputStream read = new ByteArrayInputStream(schemaContent);
      // parseResult = fieldsParser.parseSchema(read, shallValidate);
      IIndexMetaData meta = SolrFieldsParser.parse(() -> schemaContent, shallValidate);
      parseResult = meta.getSchemaParseResult();
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      parseResult = new ParseResult(shallValidate);
      parseResult.errlist.add(e.getMessage());
    }
    if (!parseResult.isValid() || parseResult.errlist.size() > 0) {
      for (String err : parseResult.errlist) {
        this.addErrorMessage(context, err);
      }
      return result;
    }
    // new String(, getEncode());
    result.content = schemaContent;
    result.success = true;
    result.parseResult = parseResult;
    return result;
  }

  protected static class SchemaResult {

    protected boolean success = false;

    // 模板索引的id编号
    private int tplAppId;

    protected final boolean xmlPost;

    public byte[] content;

    protected ParseResult parseResult;

    private static SchemaResult create(ParseResult parseResult, String schemaContent) {
      SchemaResult schema = new SchemaResult(true);
      schema.parseResult = parseResult;
      schema.content = schemaContent.getBytes(TisUTF8.get());
      schema.success = true;
      return schema;
    }

    public int getTplAppId() {
      return tplAppId;
    }

    public void setTplAppId(int tplAppId) {
      this.tplAppId = tplAppId;
    }

    /**
     * @param xmlPost
     */
    public SchemaResult(boolean xmlPost) {
      super();
      this.xmlPost = xmlPost;
    }
  }

  /**
   * 小白模式下，取得json结构化内容，(更新流程中使用)
   *
   * @param context
   * @throws Exception
   */
  public void doGetFields(Context context) throws Exception {
    Application app = new Application();
    app.setAppId(this.getAppDomain().getAppid());
    UploadResource schemaResource = this.getAppSchema(app);
    SchemaResult schema = this.parseSchemaResult(context, schemaResource.getContent(), /* shallValidate */
      false, /* xmlPost */
      false);
    // ParseResult parseResult = schema
    this.doGetFields(context, schema);
  }

  /**
   * 通过Schema xml内容向小白模式投影
   *
   * @param context
   * @throws Exception
   */
  public void doGetFieldsBySnapshotId(Context context) throws Exception {
    SchemaResult schema = this.parseSchemaResult(context, SaveFileContentAction.getResContent(this, context), /* shallValidate */
      false, /* xmlPost */
      false);
    this.doGetFields(context, schema);
  }

  /**
   * 取得普通模式多字段
   *
   * @param context
   * @throws Exception
   */
  protected // ParseResult
  com.alibaba.fastjson.JSONObject doGetFields(// ParseResult
                                              Context context, // UploadResource schemaResource
                                              SchemaResult result) throws Exception {
    ParseResult parseResult = result.parseResult;
    final com.alibaba.fastjson.JSONObject schema = new com.alibaba.fastjson.JSONObject();
    if (result.getTplAppId() > 0) {
      schema.put("tplAppId", result.getTplAppId());
    }
    // 设置原生schema的内容
    if (result.content != null) {
      schema.put("schemaXmlContent", new String(result.content, TisUTF8.get()));
    }
    String sharedKey = StringUtils.trimToEmpty(parseResult.getSharedKey());
    String pk = StringUtils.trimToEmpty(parseResult.getUniqueKey());
    schema.put("shareKey", sharedKey);
    schema.put("uniqueKey", pk);
    com.alibaba.fastjson.JSONArray fields = new com.alibaba.fastjson.JSONArray();
    com.alibaba.fastjson.JSONObject f = null;
    int id = 0;
    String type = null;
    for (PSchemaField field : parseResult.dFields) {
      f = new com.alibaba.fastjson.JSONObject();
      // 用于标示field 頁面操作過程中不能變
      // 0 开始
      f.put("id", id++);
      // 用于表示UI上的行号
      // 1 开始
      f.put("index", id);
      // f.put("uniqueKey", id++);
      f.put("sharedKey", StringUtils.equals(field.getName(), sharedKey));
      f.put("uniqueKey", StringUtils.equals(field.getName(), pk));
      f.put("name", field.getName());
      // f.put("inputDisabled", field.inputDisabled);
      // f.put("rangequery", false);
      f.put("defaultVal", StringUtils.trimToNull(field.getDefaultValue()));
      f.put("fieldtype", field.getTisFieldType());
      if (field.getType() != null) {
        type = field.getType().getSType().getName();
        serialVisualType2Json(f, type);
      }
      f.put("docval", field.isDocValue());
      f.put("indexed", field.isIndexed());
      f.put("multiValue", field.isMltiValued());
      f.put("required", field.isRequired());
      f.put("stored", field.isStored());
      fields.add(f);
    }
    schema.put("fields", fields);
    serialTypes(schema, parseResult.getFieldTypesKey());
    this.setBizResult(context, schema);
    return schema;
  }

  interface ISchemaJsonVisitor {

    void process(com.alibaba.fastjson.JSONObject schema);
  }

  /**
   * @param schema
   * @param types
   * @throws JSONException
   */
  @SuppressWarnings("all")
  protected void serialTypes(final com.alibaba.fastjson.JSONObject schema, Collection<String> schemaTypeKeys) throws JSONException {
    com.alibaba.fastjson.JSONArray types = new com.alibaba.fastjson.JSONArray();
    com.alibaba.fastjson.JSONObject f = null;
    com.alibaba.fastjson.JSONArray tokens = null;
    com.alibaba.fastjson.JSONObject tt = null;
    // Set<String> typesSet = new HashSet<String>();
    for (Map.Entry<String, VisualType> t : TokenizerType.visualTypeMap.entrySet()) {
      f = new com.alibaba.fastjson.JSONObject();
      f.put("name", t.getKey());
      // f.put("rangeAware", t.getValue().isRanageQueryAware());
      f.put("split", t.getValue().isSplit());
      tokens = new com.alibaba.fastjson.JSONArray();
      if (t.getValue().isSplit()) {
        for (TokenizerType tokenType : t.getValue().getTokenerTypes()) {
          tt = new com.alibaba.fastjson.JSONObject();
          tt.put("key", tokenType.getKey());
          tt.put("value", tokenType.getDesc());
          tokens.add(tt);
        }
        f.put("tokensType", tokens);
      }
      types.add(f);
    }
    for (String key : schemaTypeKeys) {
      if (// && !StringUtils.startsWith(key, TokenizerType.REGULAR.getKey())
        !TokenizerType.isContain(key)) {
        f = new com.alibaba.fastjson.JSONObject();
        f.put("name", key);
        // f.put("rangeAware", false);
        types.add(f);
      }
    }
    schema.put("fieldtypes", types);
  }

  /**
   * 将xml中的solr type转换成小白模式下的可视化field type
   *
   * @param f
   * @param type
   * @param
   * @throws JSONException
   */
  private void serialVisualType2Json(com.alibaba.fastjson.JSONObject f, String type) throws JSONException {
    TokenizerType tokenizerType = TokenizerType.parse(type);
    if (tokenizerType == null) {
      // 非分词字段
      f.put("split", false);
      NumericVisualType vtype = TokenizerType.numericTypeMap.get(type);
      if (vtype != null) {
        f.put("fieldtype", vtype.getType());
//        f.put("range", vtype.isRangeEnable());
//        f.put("rangequery", vtype.isRanageQueryAware());
        return;
      }
      f.put("fieldtype", type);
    } else {
      // 分词字段
      f.put("split", true);
      f.put("fieldtype", "string");
      f.put("tokenizerType", tokenizerType.getKey());
      f.put("range", false);
    }
    // f.put("rangequery", false);
    // if (TokenizerType.tokenerTypes.contains(tokenizerType)) {
    //
    // f.put("fieldtype",
    // TokenizerType.visualTypeMap.get(tokenizerType.getKey()).type);
    // f.put("ttype", tokenizerType.getKey());
    // } else {
    //
    // }
    // 处理正则分词的情况
    // if (StringUtils.startsWith(type, TokenizerType.REGULAR.getKey())) {
    // f.put("fieldtype", TokenizerType.REGULAR.getKey());
    // f.put("regularSymbol", parseRegularSymbol(type, "_"));
    // f.put("ttype", TokenizerType.REGULAR.getKey());
    // }
  }

  // private String parseRegularSymbol(String s, String split) {
  // String[] ss = s.split(split);
  // String symbol = "";
  // for (int i = 1; i < ss.length; i++) {
  // if (StringUtils.isEmpty(ss[i])) {
  // System.out.println(s + " is splited by " + split + " has a Empty");
  // } else {
  // int iValue = Integer.parseInt(ss[i], 10);
  // char tempC = (char) iValue;// 转换为一个字符
  // symbol = symbol + tempC;
  // }
  // }
  // return symbol;
  // }

  /**
   * @param
   * @return
   * @throws UnsupportedEncodingException
   * @throws Exception
   */
  // protected ParseResult parseSchema(byte[] content, boolean shallValidate)
  // throws UnsupportedEncodingException, Exception {
  //
  // return parseResult;
  // }
  public Application getAppById() {
    // NewAppInfo appinfo = this.getAppinfoFromTair();
    Application app = new Application();
    return app;
  }

  /**
   * @return
   */
  private UploadResource getAppSchema(Application app) {
    final Integer publishSnapshotId = getPublishSnapshotId(this.getServerGroupDAO(), app);
    Snapshot snapshot = this.getSnapshotDAO().selectByPrimaryKey(publishSnapshotId);
    UploadResource uploadRes = this.getUploadResourceDAO().selectByPrimaryKey(snapshot.getResSchemaId());
    return uploadRes;
  }

  // protected static final SolrFieldsParser fieldsParser;
  //
  // static {
  // fieldsParser = new SolrFieldsParser();
  // }
  // public static final SolrFieldsParser schemaFieldParser = new SolrFieldsParser();
  private SchemaResult getPostedSchema(Context context) throws Exception, UnsupportedEncodingException {
    return this.getPostedSchema(context, true);
  }

  /**
   * 页面上传的schema 文本转成结构化schema
   *
   * @param context
   * @return
   * @throws Exception
   * @throws UnsupportedEncodingException
   */
  protected SchemaResult getPostedSchema(Context context, boolean shallvalidate) throws Exception, UnsupportedEncodingException {
    // 页面中是由xml内容直接提交的
    boolean xmlPost = true;
    String schema = null;
    if (StringUtils.isEmpty(schema = this.getString("content"))) {
      xmlPost = false;
      schema = this.createSchema(this.getFormValues(), context);
    }
    // 校验提交的内容是否合法
    SchemaResult result = parseSchemaResult(context, schema.getBytes(getEncode()), shallvalidate, /*
       * shall validate schema
       */
      xmlPost);
    return result;
  }

  public String covType(String stype) {
    if (stype.equals("tint") || stype.equals("int")) {
      return "int";
    } else if (stype.equals("tlong") || stype.equals("long")) {
      return "long";
    } else if (stype.equals("double")) {
      return "double";
    } else {
      return "string";
    }
  }

  // private String createSchema(UploadSchemaForm schemaForm,
  // final Context context) throws Exception {
  // return createSchema(schemaForm, new SchemaTemplateGet() {
  // @Override
  // public String getSchemaXml() {
  // return SchemaAction.this.getSchemaXml(context);
  // }
  // });
  // }
  // protected interface SchemaTemplateGet {
  // String getSchemaXml();
  // }
  protected String createSchema(UploadSchemaWithRawContentForm schemaForm, Context context) throws Exception {
    return this.createSchema(schemaForm, context, true);
  }

  /**
   * Schema XML模式 --> 专家模式 是一个数据结构投影，确保XML转专家模式，再专家模式转xml模式信息不会减少
   */
  protected String createSchema(UploadSchemaWithRawContentForm schemaForm, Context context, boolean shallExecuteDelete) throws Exception {
    Assert.assertNotNull(schemaForm);
    // 先解析库中已经存在的模板
    if (StringUtils.isBlank(schemaForm.getSchemaXmlContent())) {
      throw new IllegalArgumentException("schemaXmlContent can not be null");
    }
    // 原始内容
    final byte[] originContent = schemaForm.getSchemaXmlContent().getBytes(getEncode());
    ByteArrayInputStream inputStream = new ByteArrayInputStream(originContent);
    org.jdom2.Document document2 = saxBuilder.build(inputStream);
    final XModifier modifier = new XModifier(document2);
    org.w3c.dom.Document document = createDocument(originContent);
    ParseResult parseResult = SolrFieldsParser.parseDocument(document, false);
    final Set<String> intersectionKeys = new HashSet<String>();
    for (PSchemaField field : parseResult.dFields) {
      // 小白编辑模式下可能将字段删除，所以在高级模式下也要将字段删除
      if (schemaForm.containsField(field.getName())) {
        intersectionKeys.add(field.getName());
        continue;
      }
      if (shallExecuteDelete) {
        modifier.addModify("/fields/field[@name='" + field.getName() + "'](:delete)");
      }
    }
    updateSchemaXML(schemaForm, document2, modifier, intersectionKeys);
    // 将生成的元素加入文档：根元素
    // 添加docType属性
    DocType docType = new DocType("schema", "solrres://tisrepository/dtd/solrschema.dtd");
    document2.setDocType(docType);
    XMLOutputter xmlout = new XMLOutputter(xmlPrettyformat);
    ByteArrayOutputStream writer = new ByteArrayOutputStream();
    xmlout.output(document2, writer);
    return writer.toString(getEncode());
  }

  private void updateSchemaXML(ISchema schemaForm, org.jdom2.Document document2, final XModifier modifier, final Set<String> intersectionKeys) {
    for (ISchemaField field : schemaForm.getSchemaFields()) {
      modifySchemaProperty(modifier, field, "type", parseSolrFieldType(field, document2));
      modifySchemaProperty(modifier, field, "stored", field.isStored());
      modifySchemaProperty(modifier, field, "indexed", field.isIndexed());
      modifySchemaProperty(modifier, field, "docValues", field.isDocValue());
      modifySchemaProperty(modifier, field, "multiValued", field.isMultiValue());
    }
    if (StringUtils.isNotBlank(schemaForm.getUniqueKey())) {
      this.modifySchemaProperty("/uniqueKey/text()", schemaForm.getUniqueKey(), modifier);
    } else {
      modifier.deleteUniqueKey();
    }
    // }
    if (StringUtils.isNotBlank(schemaForm.getSharedKey())) {
      this.modifySchemaProperty("/sharedKey/text()", schemaForm.getSharedKey(), modifier);
    } else {
      modifier.deleteSharedKey();
    }
    modifier.modify();
  }

  /**
   * 通过提交的field信息
   *
   * @param field
   * @param document2
   * @return
   */
  protected String parseSolrFieldType(ISchemaField field, org.jdom2.Document document2) {
    if ("string".equalsIgnoreCase(field.getTisFieldType()) && StringUtils.isNotBlank(field.getTokenizerType())) {
      return field.getTokenizerType();
    }
    // TokenizerType.parseVisualType(field)
    VisualType type = null;
    for (Map.Entry<String, VisualType> entry : TokenizerType.visualTypeMap.entrySet()) {
      type = entry.getValue();
      if (!"string".equalsIgnoreCase(field.getTisFieldType()) && StringUtils.equals(field.getTisFieldType(), entry.getValue().type)) {
        //return field.isRange() ? type.getRangedFieldName() : type.getType();
        return type.getType();
      }
    }
    return field.getTisFieldType();
  }

  static final SAXBuilder saxBuilder = new SAXBuilder(new XMLReaderSAX2Factory(false));

  static {
    saxBuilder.setEntityResolver(new EntityResolver() {

      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource source = new InputSource();
        source.setCharacterStream(new StringReader(""));
        return source;
      }
    });
  }

  // private static final Format xmlPrettyformat =
  // Format.getPrettyFormat();
  private static final Format xmlPrettyformat = Format.getPrettyFormat().setEncoding(TisUTF8.getName());

  private static final Format xmlRawformat = Format.getRawFormat().setEncoding(TisUTF8.getName());

  private void modifySchemaProperty(String key, Object value, XModifier modifier) {
    modifier.addModify(key, String.valueOf(value));
  }

  /**
   * @param modifier
   * @param field
   */
  private void modifySchemaProperty(XModifier modifier, ISchemaField field, String key, Object value) {
    if (value == null) {
      return;
    }
    modifySchemaProperty(String.format("/fields/field[@name='%s']/@%s", field.getName(), key), value, modifier);
  }

  private UploadSchemaWithRawContentForm getFormValues() throws Exception {
    String postContent = IOUtils.toString(this.getRequest().getInputStream(), getEncode());
    if (StringUtils.isBlank(postContent)) {
      throw new IllegalStateException("param postContent can not be blank");
    }
    return JSON.parseObject(postContent, UploadSchemaWithRawContentForm.class);
  }

  public static class UploadSchemaWithRawContentForm extends UploadSchemaForm {

    // 原始SchemaXml 文本内容
    private String schemaXmlContent;

    public String getSchemaXmlContent() {
      return schemaXmlContent;
    }

    public void setSchemaXmlContent(String schemaXmlContent) {
      this.schemaXmlContent = schemaXmlContent;
    }
  }

  public static class VisualizingSchemaForm {
    private UploadSchemaWithRawContentForm visualizingForm;
    private Savefilecontent meta;

    public UploadSchemaWithRawContentForm getVisualizingForm() {
      Objects.requireNonNull(this.visualizingForm, "visualizingForm of type UploadSchemaWithRawContentForm can not be null");
      return visualizingForm;
    }

    public void setVisualizingForm(UploadSchemaWithRawContentForm visualizingForm) {
      this.visualizingForm = visualizingForm;
    }

    public Savefilecontent getMeta() {
      Objects.requireNonNull(this.meta, "meta of type Savefilecontent can not be null");
      return meta;
    }

    public void setMeta(Savefilecontent meta) {
      this.meta = meta;
    }
  }

  /**
   * 更新一个core schema文件 baisui
   *
   * @param context
   * @throws Exception
   */
  @SuppressWarnings("all")
  public void doModifedSchema(Context context) throws Exception {
    SchemaResult schema = getPostedSchema(context);
    if (!schema.success) {
      return;
    }
  }

  /**
   * 更新schema 只更新，不执行导数据的后续操作
   *
   * @param context
   * @throws Exception
   */
  @SuppressWarnings("all")
  public void doEditSchema(Context context) throws Exception {
    // SchemaResult schema = getPostedSchema(context);
    //
    // if (!schema.success) {
    // return;
    // }
    //
    // Isv isv = this.getCurrentIsv();
    // // 在从缓存中取得内容
    // NewAppInfo appinfo = this.getAppinfoFromTair();
    //
    // if (appinfo.getAppId() == null) {
    // throw new IllegalArgumentException("appid can not be null");
    // }
    //
    // Application app = this.getAppById(appinfo.getAppId());
    // if (!appendNewSchema(context,
    // schema.content.getBytes(Charset.forName("utf8")), app)) {
    // return;
    // }
    //
    // // 缓存失效
    // this.getCache().invalid(getAppInfoKey(isv));
    //
    // context.put("query_result", "{\"code\":\"200\"}");
  }

  /**
   * 编辑完成schema之后需要跳转到索引確認頁面<br>
   * 这里只作校验
   *
   * @param context
   * @throws Exception
   */
  @Func(value = PermissionConstant.APP_ADD)
  public void doGotoAppCreateConfirm(Context context) throws Exception {
    this.errorsPageShow(context);
    // 这里只做schema的校验
    CreateIndexConfirmModel confiemModel = parseJsonPost(CreateIndexConfirmModel.class);
    SchemaResult schemaParse = parseSchema(context, confiemModel);
    LuceneVersion ver = confiemModel.parseTplVersion();
    if (schemaParse.success) {
      // 服务器端自动选机器
      // TODO 目前先不选机器
      // TISZkStateReader zookeeper = this.getZkStateReader();
      // Optional<CoreNode> cn = zookeeper.getCoreNodeCandidate(ver);
      // if (cn.isPresent()) {
      // this.setBizResult(context, cn.get());
      // }
    } else {
      // this.addErrorMessage(context, "Schema解析有错");
    }
  }

  protected SchemaResult parseSchema(Context context, CreateIndexConfirmModel confiemModel) throws Exception, UnsupportedEncodingException {
    String schemaContent = null;
    SchemaResult schemaParse = null;
    if (!confiemModel.isExpertModel()) {
      // 傻瓜模式
      schemaContent = createSchema(confiemModel.getStupid().getModel(), context);
      if (!this.validateStupidContent(context, confiemModel.getStupid().getModel())) {
        // 校验失败
        schemaParse = new SchemaResult(confiemModel.isExpertModel());
        schemaParse.success = false;
        return schemaParse;
      }
    } else {
      // 专家模式
      schemaContent = confiemModel.getExpert().getXml();
    }
    schemaParse = this.parseSchemaResult(context, schemaContent.getBytes(getEncode()), true, /* shallValidate */
      confiemModel.isExpertModel());
    return schemaParse;
  }

  @SuppressWarnings("all")
  protected boolean appendNewSchema(Context context, byte[] content, Application app) throws UnsupportedEncodingException, JSONException {
    if (content == null) {
      throw new NullPointerException("param content can not be null");
    }
    Integer publishSnapshotId = null;
    final ServerGroupCriteria gquery = new ServerGroupCriteria();
    final RunEnvironment runtime = RunEnvironment.getSysRuntime();
    gquery.createCriteria().andAppIdEqualTo(app.getAppId()).andRuntEnvironmentEqualTo(runtime.getId()).andGroupIndexEqualTo((short) 0);
    for (ServerGroup group : this.getServerGroupDAO().selectByExample(gquery)) {
      publishSnapshotId = group.getPublishSnapshotId();
      break;
    }
    if (publishSnapshotId == null) {
      throw new IllegalStateException("app:" + app.getProjectName() + " publishSnapshotId can not be null");
    }
    IUser user = this.getUser();
    // new
    Long usrId = Long.parseLong(user.getId());
    // Long(this.getCurrentIsv().getId());
    CreateSnapshotResult result = createNewSnapshot(context, this.getSnapshotViewDAO().getView(publishSnapshotId), ConfigFileReader.FILE_SCHEMA, content, this, new IMessageHandler() {

      @Override
      public void errorsPageShow(Context context) {
      }

      @Override
      public void addActionMessage(Context context, String msg) {
        SchemaAction.this.addActionMessage(context, msg);
      }

      @Override
      public void setBizResult(Context context, Object result) {
        SchemaAction.this.setBizResult(context, result);
      }

      @Override
      public void addErrorMessage(Context context, String msg) {
        SchemaAction.this.addErrorMessage(context, msg);
      }
    }, StringUtils.EMPTY, usrId, user.getName());
    if (!result.isSuccess()) {
      List<String> errorMsgList = (List<String>) context.get(BasicModule.ACTION_ERROR_MSG);
      StringBuffer err = new StringBuffer();
      if (errorMsgList != null) {
        for (String e : errorMsgList) {
          err.append(e).append("<br/>");
        }
      }
      JSONObject errors = new JSONObject();
      errors.put("code", 300);
      errors.put("reason", err.toString());
      context.put("query_result", errors.toString(1));
      return false;
    }
    ServerGroup record = new ServerGroup();
    record.setPublishSnapshotId(result.getNewId());
    // .andGroupIndexEqualTo((short) 0);
    if (this.getServerGroupDAO().updateByExampleSelective(record, gquery) < 0) {
      throw new IllegalStateException("app:" + app.getProjectName() + " upate getServerGroupDAO have not success");
    }
    return true;
  }

  /**
   * @param context
   * @return
   * @throws Exception
   */
  // private String getUploadeSchema(Context context) throws Exception {
  // NewAppInfo appinfo;
  // // 先更新缓存
  // if (StringUtils.isNotEmpty(this.getString("content"))) {
  // doModifedContext2CacheXml(context);
  // } else {
  // this.doModifedContext2CacheCommon(context);
  // }
  //
  // appinfo = this.getAppinfoFromTair();
  // if (StringUtils.isEmpty(appinfo.getSchemaXml())) {
  // throw new IllegalStateException("xml can not be null");
  // }
  // return appinfo.getSchemaXml();
  // }
  private static Integer createNewResource(Context context, final byte[] uploadContent, final String md5, PropteryGetter fileGetter, IMessageHandler messageHandler, RunContext runContext) throws UnsupportedEncodingException, SchemaFileInvalidException {
    UploadResource resource = new UploadResource();
    resource.setContent(uploadContent);
    resource.setCreateTime(new Date());
    resource.setResourceType(fileGetter.getFileName());
    resource.setMd5Code(md5);
    ConfigFileValidateResult validateResult = fileGetter.validate(resource);
    // 校验文件格式是否正确，通用用DTD来校验
    if (!validateResult.isValid()) {
      messageHandler.addErrorMessage(context, "更新流程中用DTD来校验XML的合法性，请先在文档头部添加<br/>“&lt;!DOCTYPE schema SYSTEM &quot;solrres://tisrepository/dtd/solrschema.dtd&quot;&gt;”<br/>");
      messageHandler.addErrorMessage(context, validateResult.getValidateResult());
      throw new SchemaFileInvalidException(validateResult.getValidateResult());
    }
    return runContext.getUploadResourceDAO().insert(resource);
  }

  public static CreateSnapshotResult createNewSnapshot(Context context, final SnapshotDomain domain, PropteryGetter fileGetter, byte[] uploadContent, RunContext runContext, IMessageHandler messageHandler, String memo, Long userId, String userName) throws UnsupportedEncodingException {
    CreateSnapshotResult createResult = new CreateSnapshotResult();
    try {
      // final byte[] uploadContent = content.getContentBytes();
      final String md5 = ConfigFileReader.md5file(uploadContent);
      // 创建一条资源记录
      try {
        Integer newResId = createNewResource(context, uploadContent, md5, fileGetter, messageHandler, runContext);
        final Snapshot snapshot = fileGetter.createNewSnapshot(newResId, domain.getSnapshot());
        snapshot.setMemo(memo);
        createResult.setNewSnapshotId(createNewSnapshot(snapshot, memo, runContext, userId, userName));
        snapshot.setSnId(createResult.getNewId());
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

  private static // BasicModule module
  Integer createNewSnapshot(// BasicModule module
                            final Snapshot snapshot, // BasicModule module
                            final String memo, // BasicModule module
                            RunContext runContext, // BasicModule module
                            Long userid, String userName) {
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

  public static class CreateSnapshotResult {

    private Integer newSnapshotId;

    private Integer newAppId;

    private boolean success = false;

    public Integer getNewId() {
      return newSnapshotId;
    }

    public Integer getNewAppId() {
      return newAppId;
    }

    public void setNewAppId(Integer newAppId) {
      this.newAppId = newAppId;
    }

    public void setNewSnapshotId(Integer newId) {
      this.newSnapshotId = newId;
    }

    public boolean isSuccess() {
      return success;
    }

    public CreateSnapshotResult setSuccess(boolean success) {
      this.success = success;
      return this;
    }
  }


  public static final DocumentBuilderFactory schemaDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

  static {
    // 只是读取schema不作校验
    schemaDocumentBuilderFactory.setValidating(false);
    // schemaDocumentBuilderFactory.setSchema(schema)
  }

  private static org.w3c.dom.Document createDocument(byte[] schema) throws Exception {
    // javax.xml.parsers.DocumentBuilderFactory dbf =
    // DocumentBuilderFactory
    // .newInstance();
    // dbf.setValidating(false);
    // // dbf.setXIncludeAware(false);
    // dbf.setNamespaceAware(true);
    DocumentBuilder builder = schemaDocumentBuilderFactory.newDocumentBuilder();
    builder.setEntityResolver(new EntityResolver() {

      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource source = new InputSource();
        source.setCharacterStream(new StringReader(""));
        return source;
      }
    });
    ByteArrayInputStream reader = null;
    try {
      reader = new ByteArrayInputStream(schema);
      org.w3c.dom.Document document = builder.parse(reader);
      return document;
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  public static final String KEY_FILE_CONTENT = "filecontent";

  // protected String getContent(Context context)
  // throws UnsupportedEncodingException {
  // String configContent = (String) context.get(KEY_FILE_CONTENT);
  // SnapshotDomain snapshot = getSnapshot(context);
  //
  // if (configContent != null) {
  // return configContent;
  // }
  //
  // return new String(this.getSolrDependency().getContent(snapshot),
  // "utf8");
  // }
  public static Integer getPublishSnapshotId(IServerGroupDAO groupDAO, Application app) {
    if (app == null) {
      throw new IllegalArgumentException("app can not be null");
    }
    final RunEnvironment runtime = RunEnvironment.getSysRuntime();
    ServerGroupCriteria sgCriteria = new ServerGroupCriteria();
    sgCriteria.createCriteria().andAppIdEqualTo(app.getAppId()).andRuntEnvironmentEqualTo(runtime.getId()).andGroupIndexEqualTo((short) 0);
    List<ServerGroup> sGroupList = groupDAO.selectByExample(sgCriteria);
    for (ServerGroup group : sGroupList) {
      return group.getPublishSnapshotId();
    }
    throw new IllegalStateException("app:" + app.getAppId() + " can not retrive group");
  }

  // protected PropteryGetter getSolrDependency() {
  // return ConfigFileReader.FILE_SCHEMA;
  // }

  /**
   * 修改应用的数据源
   *
   * @param context
   * @throws JSONException
   * @throws IOException
   */
  public void doModifyDataSource(Context context) throws Exception, IOException {
    String step = this.getString("step");
    // step == 3 只修改了数据源，没有继续修改schema
    // step == 5 不止修改了数据源，同时还继续修改了schema
    // if ("5".equals(step)) {
    // modifyDataSourceStep5(context);
    // } else if ("3".equals(step)) {
    // modifyDataSourceStep3(context);
    // }
  }

  // private void modifyDataSourceStep3(Context context) throws Exception
  // {
  // NewAppInfo appinfo = this.getAppinfoFromTair();
  // IDataSourceManage dataSource;
  // Integer aid = appinfo.getAppId();
  // Application app = this.getApplicationDAO().selectByPrimaryKey(aid);
  // dataSource = AbstractDataSourceManage.createDataSource(this,
  // appinfo.getDataType());
  // // 3.表单数据写入tair缓存
  // dataSource.putToTair(appinfo);
  // // 4.清理原来datatype对应的数据源的数据库信息(rds_table,odps,tddl)
  // ApplicationExtend appExtend = this.getApplicationExtendDAO()
  // .selectByAppId(aid);
  // dataSource = AbstractDataSourceManage.createDataSource(this,
  // appExtend.getSourceType());
  // dataSource.deleteDataSource(aid);
  // // 5.更新application_extend
  // updataApplicationExtend(appinfo, aid);
  // // 6.按照数据类型写入不同的表(rds_table,odps,tddl)
  // dataSource = AbstractDataSourceManage.createDataSource(this,
  // appinfo.getDataType());
  // dataSource.insertDataSource(appinfo, aid);
  // // 7.更新t_service表
  // updateTservice(app);
  // // 8.更新zk
  // TerminatorZooKeeper zooker = this.getJstZooKeeper();
  // zooker.setData(
  // ConstantUtil.LASTUPDAATE_ZK_PATH,
  // String.valueOf(
  // ManageUtils.formatDateYYYYMMddHHmmss(Long
  // .valueOf(System.currentTimeMillis())))
  // .getBytes());
  // context.put("query_result", "{\"code\":200}");
  // }
  // private void modifyDataSourceStep5(Context context) throws Exception
  // {
  // Isv isv = this.getCurrentIsv();
  // NewAppInfo appinfo = this.getAppinfoFromTair();
  // IDataSourceManage dataSource;
  // Integer aid = appinfo.getAppId();
  // Application app = this.getApplicationDAO().selectByPrimaryKey(aid);
  // if (!appendNewSchema(context,
  // appinfo.getSchemaXml().getBytes(Charset.forName(getEncode())),
  // app)) {
  // return;
  // }
  //
  // // 4.清理原来datatype对应的数据源的数据库信息(rds_table,odps,tddl)
  // ApplicationExtend appExtend = this.getApplicationExtendDAO()
  // .selectByAppId(aid);
  // dataSource = AbstractDataSourceManage.createDataSource(this,
  // appExtend.getSourceType());
  // dataSource.deleteDataSource(aid);
  // // 5.更新application_extend
  // updataApplicationExtend(appinfo, aid);
  // // 6.按照数据类型写入不同的表(rds_table,odps,tddl)
  // dataSource = AbstractDataSourceManage.createDataSource(this,
  // appinfo.getDataType());
  // dataSource.insertDataSource(appinfo, aid);
  // IAppManage appManage = AbstractAppManage.createAppManage(this,
  // appinfo.getDataType());
  // if (!this.isCoreExisted(app.getProjectName())) {//
  // 未激活应用不进行后续的同步schema操作
  // context.put("query_result", "{\"code\":600,\"aid\":" + aid + "}");
  // return;
  // }
  // if (appManage.hasAnyDumpTaskExecuting(app.getProjectName())) {
  // context.put("query_result",
  // "{\"code\":300,\"desc\":\"有任务正在执行中，请稍后再做操作\"}");
  // return;
  // }
  // // 7.更新t_service表
  // updateTservice(app);
  // // 8.更新zk
  // TerminatorZooKeeper zooker = this.getJstZooKeeper();
  // zooker.setData(
  // ConstantUtil.LASTUPDAATE_ZK_PATH,
  // String.valueOf(
  // ManageUtils.formatDateYYYYMMddHHmmss(Long
  // .valueOf(System.currentTimeMillis())))
  // .getBytes());
  // // 7.同步更新schema
  // String updateSchemaResp = appManage.updateSchema(app);
  // // 8.触发同步之后，tair缓存加锁，防止频发触发
  // appManage.setTairTriggerLock(aid, isv.getNickName());
  // if (StringUtils.isNotBlank(updateSchemaResp)) {
  // try {
  // JSONObject obj = new JSONObject(updateSchemaResp);
  // if (obj.getBoolean("success")) {
  // context.put("query_result", "{\"code\":200,\"taskid\":\""
  // + obj.getLong("taskid") + "\",\"aid\":\"" + aid
  // + "\",\"locktime\":\""
  // + ConstantUtil.ENSPIRETIME_LOCK + "\"}");
  // } else {
  // context.put("query_result", "{\"code\":1005,\"desc\":\""
  // + obj.getString("reason") + "\"}");
  // }
  // } catch (Exception e) {
  // log.info("JSON解析出错：" + updateSchemaResp);
  // context.put("query_result",
  // "{\"code\":1006,\"desc\":\"后端数据解析出错\"}");
  // }
  //
  // } else {
  // log.error("[function=doModifedSchema]create app failed snapshot is
  // null");
  // context.put("query_result", new JSONObject(
  // "{\"code\":1007,\"desc\":\"设置snapshot出错\"}"));
  // }
  // }
  // private void updataApplicationExtend(NewAppInfo appinfo, Integer aid)
  // {
  // Calendar c = Calendar.getInstance();
  // Date date = c.getTime();
  // ApplicationExtendCriteria appExtCriteria = new
  // ApplicationExtendCriteria();
  // appExtCriteria.createCriteria().andAIdEqualTo(aid);
  // ApplicationExtend appExtend = this.getApplicationExtendDAO()
  // .selectByAppId(aid);
  // appExtend.setGmtModified(date);
  // appExtend.setSourceType(StringUtils.lowerCase(appinfo.getDataType()));
  // appExtend.setMaxDocCount(appinfo.getQuota());
  // appExtend.setMaxDumpCount(appinfo.getQuota());
  // appExtend.setMaxPvCount(appinfo.getQps());
  // this.getApplicationExtendDAO().updateByExampleSelective(appExtend,
  // appExtCriteria);
  // }
  // 更新t_service表
  // private void updateTservice(Application app) throws Exception {
  // Date date = Calendar.getInstance().getTime();
  // Instance ins = this.getInstanceDAO().selectByPrimaryKey(
  // app.getInstanceId());
  // TServiceCriteria tCriteria = new TServiceCriteria();
  // tCriteria.createCriteria().andIsvServiceNameEqualTo(
  // app.getProjectName());
  // List<TService> tList =
  // this.getTServiceDAO().selectByExample(tCriteria);
  // if (tList.size() > 0) {
  // TService record = tList.get(0);
  // record.setGmtModified(date);// 修改时间
  // ServerGroupCriteria sgCriteria = new ServerGroupCriteria();
  // sgCriteria
  // .createCriteria()
  // .andAppIdEqualTo(app.getAppId())
  // .andRuntEnvironmentEqualTo(ManageUtils.getRuntime().getId());
  // List<ServerGroup> sGroupList = this.getServerGroupDAO()
  // .selectByExample(sgCriteria);
  // if (sGroupList.size() > 0) {
  // Snapshot snapshot = this.getSnapshotDAO().selectByPrimaryKey(
  // sGroupList.get(0).getPublishSnapshotId());
  // UploadResource uploadRes = this.getUploadResourceDAO()
  // .selectByPrimaryKey(snapshot.getResSchemaId());
  // String shardkey = new AppManageAction().getShardkey(new String(
  // uploadRes.getContent()));
  // record.setSharedKey(shardkey);// 分组键
  // }
  // if ("Y".equals(ins.getIsLock()) || "Y".equals(ins.getIsRelease())) {
  // record.setStatus(new Byte("1"));// 状态，0正常 1暂停
  // } else {
  // record.setStatus(new Byte("0"));// 状态，0正常 1暂停
  // }
  // this.getTServiceDAO().updateByExampleSelective(record, tCriteria);
  // log.info("update t_service success!");
  // }
  //
  // }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    // for (VisualType type : new
    // HashSet<VisualType>(TokenizerType.visualTypeMap.values())) {
    // System.out.println(type.type + "," + type.ranageQueryAware);
    // }
    // Calendar c = Calendar.getInstance();
    // Date date = c.getTime();
    // System.out.println("time:"+date.toString());
    // System.out.println("time:"+c.getTimeInMillis());
    // System.out.println(System.currentTimeMillis()/1000);
    // org.w3c.dom.Document document = createDocument(FileUtils
    // .readFileToString(new File("D:\\home\\schema.xml")));
    // SAXReader saxReader = new SAXReader();
    // saxReader.setDocumentFactory(new DOMDocumentFactory());
    // // 根据saxReader的read重写方法可知，既可以通过inputStream输入流来读取，也可以通过file对象来读取
    // // Document document = saxReader.read(inputStream);
    // DOMDocument document4j = (DOMDocument) saxReader.read(new File(
    // "D:\\home\\schema.xml"));
    //
    // XModifier modifier = new XModifier(document4j);
    // modifier.addModify(
    // "/schema/types/fieldType[@name='singleString']/@class",
    // "java.lang.String");
    // modifier.addModify("/schema/types/fieldType[@name='kkkkkkkk']/@class",
    // "ddddddd");
    // modifier.addModify("/schema/types/fieldType[@name='xxxxxx'][@class='java.lang.String']");
    // modifier.addModify(
    // "/schema/types/fieldType[@name='singleStringggg']/@xxxxx",
    // "xxxxxx");
    // modifier.modify();
    // NodeList nl = document.getChildNodes().item(1).getChildNodes();
    // Node n = null;
    // for (int i = 0; i < nl.getLength(); i++) {
    // n = nl.item(i);
    // System.out.println(String.format("xxx %s,%s",
    // new String[] { n.getNodeName(), n.getNodeValue() }));
    // }
    // modifier.modify();
    // TransformerFactory tf = TransformerFactory.newInstance();
    // Transformer transformer = tf.newTransformer();
    //
    // transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
    // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
    // "no");
    // StringWriter writer = new StringWriter();
    // transformer
    // .transform(new DOMSource(document), new StreamResult(writer));
    // // System.out.println(writer.getBuffer());
    // DOMBuilder domBuilder = new DOMBuilder();
    // // 设置xml文件格式---选用这种格式可以使生成的xml文件自动换行同时缩进
    // Format format = Format.getRawFormat();
    //
    // // ▲▲▲▲▲▲************************ 构建schema文件格式
    // // 将生成的元素加入文档：根元素
    //
    // org.jdom2.Document doc = domBuilder.build(document4j);
    //
    // // 添加docType属性
    // DocType docType = new DocType("schema",
    // doc.setDocType(docType);
    // XMLOutputter xmlout = new XMLOutputter(format);
    // // 设置xml内容编码
    // xmlout.setFormat(format.setEncoding("utf8"));
    //
    // ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
    //
    // xmlout.output(doc, byteRsp);
    //
    // System.out.println(byteRsp.toString("utf8"));
  }

  public static class NumericVisualType extends VisualType {

    public static NumericVisualType create(VisualType type) {
      NumericVisualType result = new NumericVisualType(type.getType());
      // result.setRangeEnable(rangeEnable);
      return result;
    }

    // private boolean rangeEnable;

    private NumericVisualType(String type) {
      super(type, false);
    }

  }
}
