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
package com.qlangtech.tis.runtime.module.misc.impl;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DefaultFieldErrorHandler implements IFieldErrorHandler {
    private static final Pattern JSONARRAR_TOKEN = Pattern.compile("\\[(\\d+)\\]");
    private static final Pattern JSONOBJECT_TOKEN = Pattern.compile("\\.([_\\dA-Za-z]+)");
    public static final String KEY_VALIDATE_FIELDS_STACK = "validate_fields_stack";

    public static final String KEY_VALIDATE_ITEM_INDEX = "validate_item_index";
    public static final String KEY_VALIDATE_ITEM_SUBITEM_DETAILED_PK_VAL = "validate_item_subitem_detailed_pk_val";

    public static final String KEY_VALIDATE_PLUGIN_INDEX = "validate_plugin_index";

    @Override
    public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
        throw new UnsupportedOperationException();
    }

    public static void pushFieldStack(Context context, String fieldName, int itemIndex) {
        Stack<FieldIndex> fieldStack = (Stack<FieldIndex>) context.get(KEY_VALIDATE_FIELDS_STACK);
        if (fieldStack == null) {
            fieldStack = new Stack<>();
            context.put(KEY_VALIDATE_FIELDS_STACK, fieldStack);
        }
        fieldStack.push(new FieldIndex(fieldName, itemIndex));
    }

    public static Stack<FieldIndex> getFieldStack(Context context) {
        return (Stack<FieldIndex>) context.get(KEY_VALIDATE_FIELDS_STACK);
    }

    public static void popFieldStack(Context context) {
        Stack<FieldIndex> fieldStack = (Stack<FieldIndex>) context.get(KEY_VALIDATE_FIELDS_STACK);
        if (fieldStack == null) {
            return;
            // fieldStack = new Stack<>();
            // context.put(KEY_VALIDATE_FIELDS_STACK, fieldStack);
        }
        fieldStack.pop();
    }

    @Override
    public final void addFieldError(Context context, String fieldName, String msg, Object... params) {
        Integer pluginIndex = (Integer) context.get(KEY_VALIDATE_PLUGIN_INDEX);
        Integer itemIndex = (Integer) context.get(KEY_VALIDATE_ITEM_INDEX);
        Optional<String> subItemDetiledPk
                = Optional.ofNullable((String) context.get(KEY_VALIDATE_ITEM_SUBITEM_DETAILED_PK_VAL));
        final Stack<FieldIndex> fieldStack = getFieldStack(context);
        itemIndex = (itemIndex == null ? 0 : itemIndex);
        pluginIndex = (pluginIndex == null ? 0 : pluginIndex);
        List<FieldError> fieldsErrorList = getFieldsError(context, fieldStack, pluginIndex, itemIndex, subItemDetiledPk);

        String pkName = getKeyFieldName(fieldName);
        Objects.requireNonNull(pkName, "pkName can not be null");
        boolean contain = false;
        for (FieldError fieldError : fieldsErrorList) {

            if (StringUtils.equals(pkName, fieldError.fieldName)) {
                fieldError.addMsg(fieldName, msg);
                contain = true;
                break;
            }
        }
        if (!contain) {
            fieldsErrorList.add(new FieldError(fieldName, msg));
        }
    }

    /**
     * @param context
     * @param fieldStack
     * @param pluginIndex
     * @param itemIndex
     * @param subItemDetiledPk 使用选表请款选择表
     * @return
     */
    private List<FieldError> getFieldsError(Context context, Stack<FieldIndex> fieldStack, Integer pluginIndex,
                                            Integer itemIndex, Optional<String> subItemDetiledPk) {

        List<List<ItemsErrors>> pluginErrorList = null;

        pluginErrorList = (List<List<ItemsErrors>>) context.get(ACTION_ERROR_FIELDS);
        if (pluginErrorList == null) {
            pluginErrorList = Lists.newArrayList();
            context.put(ACTION_ERROR_FIELDS, pluginErrorList);
        }
        /**item*/
        List<ItemsErrors>
                itemsErrorList = getFieldErrors(pluginIndex, pluginErrorList, () -> Lists.newArrayList());

        List<FieldError> fieldsErrorList = null;
        if (subItemDetiledPk.isPresent()) {
            SubFromDetailedItemsErrors multiDetailed
                    = (SubFromDetailedItemsErrors) getFieldErrors(itemIndex, itemsErrorList, () -> new SubFromDetailedItemsErrors());
            fieldsErrorList = multiDetailed.getDetailedFormError(subItemDetiledPk.get());
        } else {
            ListDetailedItemsErrors fieldErrors = (ListDetailedItemsErrors) getFieldErrors(itemIndex, itemsErrorList, () -> new ListDetailedItemsErrors());
            fieldsErrorList = fieldErrors.fieldsErrorList;
        }


        if (fieldStack == null || fieldStack.size() < 1) {
            return fieldsErrorList;
        } else {
            for (int index = 0; index < fieldStack.size(); index++) {
                FieldIndex fieldIndex = fieldStack.get(index);
                Optional<FieldError> find =
                        fieldsErrorList.stream().filter((f) -> StringUtils.equals(f.getFieldName(),
                                fieldIndex.filedName)).findFirst();
                FieldError fieldErr = null;
                if (find.isPresent()) {
                    fieldErr = find.get();
                } else {
                    fieldErr = new FieldError(fieldIndex.filedName, null);
                    fieldsErrorList.add(fieldErr);
                }
                if (fieldErr.itemsErrorList == null) {
                    fieldErr.itemsErrorList = Lists.newArrayList();
                }
                fieldsErrorList = ((ListDetailedItemsErrors) getFieldErrors(fieldIndex.itemIndex, fieldErr.itemsErrorList,
                        () -> new ListDetailedItemsErrors())).fieldsErrorList;
            }
        }
        return fieldsErrorList;
    }

    private <T> T getFieldErrors(Integer itemIndex, List<T> itemsErrorList, Callable<T> newer) {
        try {
            T fieldsErrorList;
            while (true) {
                if (itemIndex >= itemsErrorList.size()) {
                    // Lists.newArrayList();
                    fieldsErrorList = newer.call();
                    itemsErrorList.add(fieldsErrorList);
                } else {
                    fieldsErrorList = itemsErrorList.get(itemIndex);
                    break;
                }
            }
            return fieldsErrorList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class ItemsErrors {

        public abstract JSON serial2JSON();
    }

    private static class SubFromDetailedItemsErrors extends ItemsErrors {
        private Map<String /**detail Id Name*/, List<FieldError>> multiDetailed = Maps.newHashMap();

        List<FieldError> getDetailedFormError(String subItemDetiledPk) {
            if (StringUtils.isEmpty(subItemDetiledPk)) {
                throw new IllegalArgumentException("param subItemDetiledPk can not be null");
            }
            List<FieldError> fieldsErrorList = multiDetailed.get(subItemDetiledPk);
            if (fieldsErrorList == null) {
                fieldsErrorList = Lists.newArrayList();
                multiDetailed.put(subItemDetiledPk, fieldsErrorList);
            }
            return fieldsErrorList;
        }

        @Override
        public JSON serial2JSON() {
            JSONObject errors = new JSONObject();
            for (Map.Entry<String /**detail Id Name*/, List<FieldError>> entry : multiDetailed.entrySet()) {
                errors.put(entry.getKey(), ListDetailedItemsErrors.convertItemsErrorList(entry.getValue()));
            }
            return errors;
        }
    }

    public static class FieldError {

        private final String fieldName;

        /**
         * String | JSONObject
         */
        private final IFieldMsg msg;

        // 当field为插件类型时，并且定义了属性成员，则当字段出错
        public List<ItemsErrors> itemsErrorList;

        public FieldError(String fieldName, String msg) {
            this.fieldName = getKeyFieldName(fieldName);
            this.msg = setVal(null, fieldName, msg);
        }

        public String getFieldName() {
            return this.fieldName;
        }

        public void addMsg(String nestKey, String val) {
            this.msg.addNestMsg(nestKey, val);
        }

        public Object getMsg() {
            return this.msg.getContent();
        }
    }

    public interface IFieldMsg {
        public Object getContent();

        void addNestMsg(String nestKey, String val);
    }

    private static class StrFieldMsg implements IFieldMsg {
        private final String msg;

        public StrFieldMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public Object getContent() {
            return this.msg;
        }

        @Override
        public void addNestMsg(String nestKey, String val) {
            // throw new UnsupportedOperationException("nestKey:" + nestKey + ",val:" + val + ",msg:" + this.msg);
        }
    }

    private static class JSONFieldErrorMsg implements IFieldMsg {
        private final JSONObject content;

        public JSONFieldErrorMsg(JSONObject content) {
            this.content = content;
        }

        @Override
        public Object getContent() {
            for (Map.Entry<String, Object> entry : content.entrySet()) {
                return entry.getValue();
            }
            throw new IllegalStateException("can not find content from content:\n" + JsonUtil.toString(content));
        }

        @Override
        public void addNestMsg(String nestKey, String val) {
            setVal(this.content, nestKey, val);
        }
    }


    static IFieldMsg setVal(JSONObject json, String complexPropKey, String val) {
        return setVal(json, complexPropKey, val, Optional.empty());
    }

    static String getKeyFieldName(String jsonPathFieldName) {
        StrFieldMsg keyFieldName = (StrFieldMsg) setVal(null, jsonPathFieldName, null,
                (primaryKeyName) -> new StrFieldMsg(primaryKeyName));
        return keyFieldName.msg;
    }

    static IFieldMsg setVal(JSONObject json, String complexPropKey, String val,
                            Function<String, IFieldMsg> primaryKeyConsumer) {
        return setVal(json, complexPropKey, val, Optional.of(primaryKeyConsumer));
    }

    private static void println(String msg) {
        // System.out.println(msg);
    }

    private static IFieldMsg setVal(JSONObject json, String complexPropKey, String val
            , Optional<Function<String, IFieldMsg>> primaryKeyConsumer) {
        Matcher arrayMatcher = JSONARRAR_TOKEN.matcher(complexPropKey);
        Matcher objMatcher = JSONOBJECT_TOKEN.matcher(complexPropKey);
        int matchStart = 0;

        if (!primaryKeyConsumer.isPresent() && json == null) {
            json = new JSONObject();
        }

        TailObj tailObj = null;
        // JSONArray tail;
        //  println("complexProp.length():" + complexPropKey.length());
        int turn = 1;
        while (matchStart < complexPropKey.length()) {
            //   println("===============================");
            //  println("turn:" + (turn++) + ",matchStart:" + matchStart);

            int arrayMatchStart = -1;
            int objectMatchStart = -1;
            while (arrayMatcher.find(matchStart)) {
                println("---->" + arrayMatcher.group(1) + ",start:" + arrayMatcher.start());
                arrayMatchStart = arrayMatcher.start();
                break;
            }

            //  Matcher  objMatcher = JSONOBJECT_TOKEN.matcher(complexProp);
            while (objMatcher.find(matchStart)) {
                println("---->" + objMatcher.group() + ",start:" + objMatcher.start());
                objectMatchStart = objMatcher.start();
                break;
            }

            if (arrayMatchStart > -1 && (objectMatchStart < 0 || arrayMatchStart < objectMatchStart)) {
                // 识别到了一个array属性
                String arrayToken = StringUtils.substring(complexPropKey, matchStart, arrayMatchStart);

                if (StringUtils.isEmpty(arrayToken)) {
                    // 说明是key 的中间部分
                    println("arrayIndex:" + arrayMatcher.group(1));

                    tailObj = tailObj.addArray(Integer.parseInt(arrayMatcher.group(1)));

                } else {

                    if (primaryKeyConsumer.isPresent()) {
                        return primaryKeyConsumer.get().apply(arrayToken);
                    }
                    // 说明key 的头部
                    int indexOf = Integer.parseInt(arrayMatcher.group(1));
                    println("arrayMatch:" + arrayToken + ",arrayIndex:" + indexOf);

                    JSONArray array = null;
                    if (json.containsKey(arrayToken)) {
                        array = json.getJSONArray(arrayToken);
                    } else {
                        array = new JSONArray();
                        json.put(arrayToken, array);
                    }

                    tailObj = new TailObjOfArray(array, indexOf);

                }
                matchStart = arrayMatcher.end();
            } else if (objectMatchStart > -1 && (arrayMatchStart < 0 || arrayMatchStart > objectMatchStart)) {
                // 识别到了一个Object属性

                String objToken = objMatcher.group(1);// StringUtils.substring(complexProp, objMatcher.start(1),
                // objMatcher.end());


                String obj = StringUtils.substring(complexPropKey, matchStart, objMatcher.start());

                println("pre:" + obj + ",objMatch:" + objToken + ",matchStart:" + objMatcher.start());
                matchStart = objMatcher.end();
                if (StringUtils.isNotEmpty(obj)) {
                    if (primaryKeyConsumer.isPresent()) {
                        return primaryKeyConsumer.get().apply(obj);//.ifPresent((consume) -> consume.accept(obj));
                    }
                    JSONObject newObj = null;
                    if (json.containsKey(obj)) {
                        Object j = json.get(obj);
                        if (!(j instanceof JSONObject)) {
                            throw new IllegalStateException("json obj key:" + obj + " relevant element:\n" //
                                    + JsonUtil.toString(j) + "\n must be type of :" + JSONObject.class.getSimpleName());
                        }
                        newObj = (JSONObject) j;
                    } else {
                        newObj = new JSONObject();
                        json.put(obj, newObj);
                    }

                    tailObj = new TailOfObject(newObj, objToken);
                } else {

                    tailObj = tailObj.addObj(objToken);
                }


            } else {
                if (primaryKeyConsumer.isPresent()) {
                    return primaryKeyConsumer.get().apply(complexPropKey);//.ifPresent((consume) -> consume.accept
                    // (complexPropKey));
                    // return null;
                }
                // throw new IllegalStateException("arrayMatchStart can not equal with objectMatchStart");
                return new StrFieldMsg(val);
            }
        } //
        tailObj.finalSetVal(val);
        return new JSONFieldErrorMsg(json);
    }

    static abstract class TailObj {
        public abstract TailObj addArray(int index);

        public abstract TailObj addObj(String objToken);

        public abstract void finalSetVal(String val);
    }

    static class TailObjOfArray extends TailObj {
        JSONArray array = new JSONArray();
        int indexOf;

        public TailObjOfArray(JSONArray array, int indexOf) {
            this.array = Objects.requireNonNull(array);
            this.indexOf = indexOf;
            if (indexOf >= array.size()) {
                array.set(indexOf, null);
            }
        }

        @Override
        public TailObj addArray(int index) {
            JSONArray newArray = null;

            if ((newArray = this.array.getJSONArray(indexOf)) == null) {
                newArray = new JSONArray();
                this.array.set(indexOf, newArray);
            }
            return new TailObjOfArray(Objects.requireNonNull(newArray, "newArray can not be null"), index);
        }

        @Override
        public TailObj addObj(String objToken) {

            JSONObject obj = null;

            if ((obj = this.array.getJSONObject(indexOf)) == null) {
                obj = new JSONObject();
                this.array.set(indexOf, obj);
            }

            return new TailOfObject(obj, objToken);
        }

        @Override
        public void finalSetVal(String val) {
            array.set(indexOf, val);
        }
    }

    static class TailOfObject extends TailObj {
        private final JSONObject obj;
        private final String key;

        public TailOfObject(JSONObject obj, String key) {
            this.obj = Objects.requireNonNull(obj, "obj can not be null");
            this.key = key;
        }

        @Override
        public TailObj addArray(int index) {
            JSONArray array = null;

            if ((array = obj.getJSONArray(key)) == null) {
                array = new JSONArray();
                obj.put(key, array);
            }
            return new TailObjOfArray(array, index);
        }

        @Override
        public TailObj addObj(String objToken) {
            JSONObject newObj = null;

            if ((newObj = this.obj.getJSONObject(key)) == null) {
                newObj = new JSONObject();
                this.obj.put(key, newObj);
            }

            return new TailOfObject(newObj, objToken);
        }

        @Override
        public void finalSetVal(String val) {
            this.obj.put(key, val);
        }
    }

    public static class FieldIndex {

        // 字段名称
        public final String filedName;

        // 该字段在当前深度中的Item序号
        public final int itemIndex;

        public FieldIndex(String filedName, int itemIndex) {
            this.filedName = filedName;
            this.itemIndex = itemIndex;
        }
    }
}
