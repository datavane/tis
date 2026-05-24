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
package com.qlangtech.tis.manage.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Option implements IdentityName {

    public static final String KEY_VALUE = "val";
    public static final String KEY_LABEL = "label";
    public static final String keyChecked = "checked";
    public static final String KEY_HELP = "help";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_COMMENT_COLOR = "color";
    public static final String KEY_CONTENT = "content";
    public static String KEY_END_TYPE = "endType";
    // public static String KEY_HELP = "help";

    public static Option create(JSONObject option) {
        return new Option(Objects.requireNonNull(option, "option can not be null").getString(Option.KEY_LABEL),
                option.getString(Option.KEY_VALUE));
    }

    private final String name;

    private final Object value;

    private Boolean checked;

    /**
     * @param name  label
     * @param value
     * @see
     */
    public Option(String name, Object value) {
        super();
        this.name = name;
        this.value = value;
    }

    public Option(String val) {
        this(val, val);
    }

    public static JSONArray toJson(List<?> options) {
        JSONArray enums = new JSONArray();
        if (options != null) {
            Stream<Option> optStream = options.stream().map(new Function<Object, Option>() {
                @Override
                public Option apply(Object o) {
                    if (o instanceof Option) {
                        return (Option) o;
                    } else if (o instanceof IdentityName) {
                        return new Option(((IdentityName) o).identityValue());
                    } else {
                        throw new IllegalStateException("illegal type:" + o.getClass());
                    }
                }
            });
            optStream.forEach((opt) -> {
                JSONObject o = new JSONObject();
                o.put(KEY_LABEL, opt.getName());
                o.put(KEY_VALUE, opt.getValue());
                String endtype = null;
                if ((endtype = opt.endType()) != null) {
                    o.put(KEY_END_TYPE, endtype);
                }
                String help = null;
                if ((help = opt.description()) != null) {
                    o.put(KEY_HELP, help);
                }
                OptionComment comment = null;
                if ((comment = opt.comment()) != null) {
                    JSONObject commentObj = new JSONObject();
                    commentObj.put(KEY_COMMENT_COLOR, comment.color);
                    commentObj.put(KEY_CONTENT, comment.content);
                    o.put(KEY_COMMENT, commentObj);
                }
                if (opt.isChecked() != null) {
                    o.put(keyChecked, opt.isChecked());
                }
                enums.add(o);
            });
        }
        return enums;
    }

    @Override
    public String identityValue() {
        return String.valueOf(value);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @JSONField(serialize = false)
    public Boolean isChecked() {
        return checked;
    }

    public Option setChecked(Boolean checked) {
        this.checked = checked;
        return this;
    }

    /**
     * 下啦选项中也可以设置图标
     * //@see EndType
     *
     * @return
     */
    public String endType() {
        return null;
    }

    /**
     * 描述信息
     *
     * @return
     */
    public String description() {
        return null;
    }

    /**
     * 评论性的信息，例如列的列表中，是否为主键在最右侧显示
     *
     * @return
     */
    public OptionComment comment() {
        return null;
    }

    public static class OptionComment {
        private final String color;
        private final String content;

        public OptionComment(OptionCommentColor color, String content) {
            this.color = color.getColor();
            this.content = content;
        }

        public String getColor() {
            return color;
        }

        public String getContent() {
            return content;
        }
    }

    public enum OptionCommentColor {
        Geekblue("geekblue"), Blue("blue"), Green("green"), Purple("purple"), Pink("pink");
        private final String color;

        public static OptionCommentColor parse(String color) {
            if (StringUtils.isEmpty(color)) {
                throw new IllegalArgumentException("param color can not be empty");
            }
            for (OptionCommentColor c : OptionCommentColor.values()) {
                if (c.color.equalsIgnoreCase(color)) {
                    return c;
                }
            }
            throw new IllegalStateException("illegal color param:" + color);
        }

        public String getColor() {
            return color;
        }

        private OptionCommentColor(String color) {
            this.color = color;
        }
    }
}
