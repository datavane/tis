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
package com.qlangtech.tis.manage.common;

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.ConfigFileContext.Header;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * 提交html表单
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年1月18日
 */
public abstract class PostFormStreamProcess<T> extends StreamProcess<T> {

    public static final List<Header> HEADERS_application_x_www_form_urlencoded = Collections.singletonList(new Header("content-type", "application/x-www-form-urlencoded"));

    public static final List<Header> HEADERS_multipart_byteranges = Collections.singletonList(new Header("content-type", "multipart/byteranges"));

    public static final List<Header> HEADERS_CONTENT_TYPE_JSON = Collections.singletonList(new ConfigFileContext.Header("Content-Type", "application/json"));

    public static final List<Header> HEADER_TEXT_HTML = Lists.newArrayList(new Header("content-type", "text/html"));

    public ContentType getContentType() {
        return ContentType.Application_x_www_form_urlencoded;
    }

    @Override
    public List<Header> getHeaders() {
        // return HEADERS_application_x_www_form_urlencoded;
        return getContentType().headers;
    }

    public enum ContentType {

        Multipart_byteranges(HEADERS_multipart_byteranges, (params) -> {
            try {
                StringBuilder content = new StringBuilder();
                if (params != null) {
                    for (HttpUtils.PostParam p : params) {
                        content.append(p.getKey()).append("=").append(URLEncoder.encode(p.getValue(), TisUTF8.getName())).append("&");
                    }
                }
                return content.toString().getBytes(Charset.forName(TisUTF8.getName()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }),
        // 
        TEXT_HTML(HEADER_TEXT_HTML, Multipart_byteranges.paramSerialize),
        // 
        Application_x_www_form_urlencoded(// 
        HEADERS_application_x_www_form_urlencoded, Multipart_byteranges.paramSerialize),
        // 
        JSON(HEADERS_CONTENT_TYPE_JSON, (params) -> {
            JSONObject json = new JSONObject();
            for (HttpUtils.PostParam param : params) {
                json.put(param.getKey(), param.getValue());
            }
            return json.toString().getBytes(Charset.forName(TisUTF8.getName()));
        });

        private final List<Header> headers;

        private final ISerializeParams paramSerialize;

        private ContentType(List<Header> headers, ISerializeParams paramSerialize) {
            this.headers = headers;
            this.paramSerialize = paramSerialize;
        }

        public byte[] serializeParams(List<HttpUtils.PostParam> params) {
            return paramSerialize.serializeParams(params);
        }

        public List<Header> getHeaders() {
            return headers;
        }
    }

    interface ISerializeParams {

        public byte[] serializeParams(List<HttpUtils.PostParam> params);
    }
}
