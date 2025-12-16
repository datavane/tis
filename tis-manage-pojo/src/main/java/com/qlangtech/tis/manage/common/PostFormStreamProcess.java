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

import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.ConfigFileContext.Header;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提交html表单
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年1月18日
 */
public abstract class PostFormStreamProcess<T> extends StreamProcess<T> {

    public static final List<Header> HEADERS_application_x_www_form_urlencoded =
            Collections.singletonList(new Header("content-type", "application/x-www-form-urlencoded"));

    public static final List<Header> HEADERS_multipart_byteranges = Collections.singletonList(new Header("content" +
            "-type", "multipart/byteranges"));

    public static final List<Header> HEADERS_CONTENT_TYPE_JSON =
            Collections.singletonList(new ConfigFileContext.Header("Content-Type", "application/json"));

    public static final List<Header> HEADER_TEXT_HTML = Lists.newArrayList(new Header("content-type", "text/html"));

    public ContentType getContentType() {
        return ContentType.Application_x_www_form_urlencoded;
    }

    private final List<Header> appendHeaders;

    public PostFormStreamProcess(List<Header> appendHeaders) {
        this.appendHeaders = appendHeaders;
    }

    public PostFormStreamProcess() {
        this(Collections.emptyList());
    }

    public PostFormStreamProcess(Header appendHeader) {
        this(Collections.singletonList(appendHeader));
    }

    @Override
    public final List<Header> getHeaders() {
        // return HEADERS_application_x_www_form_urlencoded;

        if (CollectionUtils.isEmpty(appendHeaders)) {
            return getContentType().headers;
        } else {
            List<ConfigFileContext.Header> hds = Lists.newArrayList(getContentType().headers);
            hds.addAll(appendHeaders);
            return hds;
        }


        // return getContentType().headers;
    }

    public int getMaxRetry() {
        return 1;
    }

    public enum ContentType {

        Multipart_byteranges(HEADERS_multipart_byteranges, (params) -> {
            // try {
            // StringBuilder content = new StringBuilder();
            if (CollectionUtils.isNotEmpty(params)) {

                return params.stream().map((param) -> {
                    try {
                        return param.getKey() + "=" + URLEncoder.encode(param.getValue(), TisUTF8.getName());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.joining("&")).getBytes(Charset.forName(TisUTF8.getName()));

                //                    for (HttpUtils.PostParam p : params) {
                //                        content.append(p.getKey()).append("=").append(URLEncoder.encode(p.getValue(),
                //                                TisUTF8.getName())).append("&");
                //                    }
            }
            return null;
            //  return content.toString().getBytes(Charset.forName(TisUTF8.getName()));
            //            } catch (UnsupportedEncodingException e) {
            //                throw new RuntimeException(e);
            //            }
        }), //
        TEXT_HTML(HEADER_TEXT_HTML, Multipart_byteranges.paramSerialize), //
        Application_x_www_form_urlencoded(//
                HEADERS_application_x_www_form_urlencoded, Multipart_byteranges.paramSerialize), //
        JSON(HEADERS_CONTENT_TYPE_JSON, (params) -> {
            JSONObject json = new JSONObject();
            for (HttpUtils.PostParam param : params) {
                json.put(param.getKey(), param.getRawVal());
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
