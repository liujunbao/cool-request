/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * Request.java is part of Cool Request
 *
 * License: GPL-3.0+
 *
 * Cool Request is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Request is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Request.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cool.request.components.http.script;

import com.cool.request.components.http.FormDataInfo;
import com.cool.request.components.http.KeyValue;
import com.cool.request.components.http.net.request.HttpRequestParamUtils;
import com.cool.request.components.http.net.request.StandardHttpRequestParam;
import com.cool.request.lib.springmvc.*;
import com.cool.request.script.FormURLEncodedBody;
import com.cool.request.script.HTTPRequest;
import com.cool.request.script.ILog;
import com.cool.request.script.JSONBody;
import com.cool.request.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request implements HTTPRequest {
    private final StandardHttpRequestParam standardHttpRequestParam;
    private final SimpleScriptLog scriptSimpleLog;

    public Request(StandardHttpRequestParam standardHttpRequestParam, SimpleScriptLog scriptSimpleLog) {
        this.standardHttpRequestParam = standardHttpRequestParam;
        this.scriptSimpleLog = scriptSimpleLog;
    }

    public ILog getScriptSimpleLog() {
        return scriptSimpleLog;
    }

    @Override
    public List<String> getHeaders(String key) {
        return this.standardHttpRequestParam.getHeaders().stream()
                .filter(keyValue -> keyValue.getKey().equalsIgnoreCase(key))
                .map(KeyValue::getValue).collect(Collectors.toList());
    }

    @Override
    public String getParameter(String key) {
        List<String> values = getParameterMap().getOrDefault(key, null);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    @Override
    public void setParameter(String key, String newValues) {
        if (StringUtils.isEmpty(key)) return;
        if (newValues == null) return;
        Map<String, List<String>> queryParamsMap = getParameterMap();
        queryParamsMap.remove(key);
        List<String> newParam = new ArrayList<>();
        newParam.add(newValues);
        queryParamsMap.putIfAbsent(key, newParam);
        URI uri = URI.create(standardHttpRequestParam.getUrl());
        StringBuilder query = new StringBuilder();
        for (String paramKey : queryParamsMap.keySet()) {
            for (String s : queryParamsMap.get(paramKey)) {
                query.append(paramKey).append("=").append(s).append("&");
            }
        }
        try {
            this.standardHttpRequestParam.setUrl(new URI(uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    query.toString(),
                    uri.getFragment()).toString());

        } catch (URISyntaxException ignored) {
        }
    }

    @Override
    public void addParameter(String key, String value) {
        try {
            URI uri = URI.create(standardHttpRequestParam.getUrl());
            Map<String, List<String>> queryParamsMap = getParameterMap();
            queryParamsMap.computeIfAbsent(key, s -> new ArrayList<>()).add(value);
            StringBuilder query = new StringBuilder();
            for (String paramKey : queryParamsMap.keySet()) {
                for (String s : queryParamsMap.get(paramKey)) {
                    query.append(paramKey).append("=").append(s).append("&");
                }
            }
            standardHttpRequestParam.setUrl(new URI(uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    query.toString(),
                    uri.getFragment()).toString());
        } catch (URISyntaxException ignored) {

        }
    }

    @Override
    public void removeParameter(String key) {
        try {
            URI uri = URI.create(standardHttpRequestParam.getUrl());
            Map<String, List<String>> queryParamsMap = getParameterMap();
            queryParamsMap.remove(key);
            StringBuilder query = new StringBuilder();
            for (String paramKey : queryParamsMap.keySet()) {
                for (String s : queryParamsMap.get(paramKey)) {
                    query.append(paramKey).append("=").append(s).append("&");
                }
            }
            standardHttpRequestParam.setUrl(new URI(uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    query.toString(),
                    uri.getFragment()).toString());
        } catch (URISyntaxException ignored) {

        }
    }

    @Override
    public void setFormData(String key, String value, boolean isFile) {
        Body body = standardHttpRequestParam.getBody();
        if (body instanceof FormBody) {
            ((FormBody) body).getData().removeIf(formDataInfo -> StringUtils.isEqualsIgnoreCase(formDataInfo.getName(), key));
            ((FormBody) body).getData().add(new FormDataInfo(key, value, isFile ? "file" : "text"));
        }
    }

    @Override
    public void setHeader(String key, String value) {
        removeHeader(key);
        addHeader(key, value);
    }

    @Override
    public void addHeader(String key, String value) {
        standardHttpRequestParam.getHeaders().add(new KeyValue(key, value));
    }

    @Override
    public void removeHeader(String key) {
        standardHttpRequestParam.getHeaders().removeIf(keyValue -> StringUtils.isEqualsIgnoreCase(keyValue.getKey(), key));
    }

    @Override
    public byte[] getRequestBody() {
        return standardHttpRequestParam.getBody().contentConversion();
    }

    @Override
    public void setRequestBody(String body) {
        this.standardHttpRequestParam.setBody(new StringBody(body));
    }

    @Override
    public void setRequestBody(byte[] bytes) {
        this.standardHttpRequestParam.setBody(new ByteBody(bytes));
    }

    @Override
    public void setRequestBody(InputStream inputStream) {
        if (inputStream != null) {
            try {
                byte[] bytes = inputStream.readAllBytes();
                this.standardHttpRequestParam.setBody(new ByteBody(bytes));
            } catch (IOException e) {
                scriptSimpleLog.println(e.getMessage());
            }
        }
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        try {
            return HttpRequestParamUtils.splitQuery(new URL(this.standardHttpRequestParam.getUrl()));
        } catch (Exception e) {
            e.printStackTrace(this.scriptSimpleLog);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getParameterValues(String key) {
        return getParameterMap().getOrDefault(key, new ArrayList<>());
    }

    @Override
    public void setUrl(String newURL) {
        standardHttpRequestParam.setUrl(newURL);
    }

    @Override
    public String getUrl() {
        return URLDecoder.decode(standardHttpRequestParam.getUrl(), StandardCharsets.UTF_8);
    }

    @Override
    public String getHeader(String key) {
        for (KeyValue header : this.standardHttpRequestParam.getHeaders()) {
            if (StringUtils.isEqualsIgnoreCase(header.getKey(), key)) return header.getValue();
        }
        return null;
    }

    @Override
    public List<String> getHeaderKeys() {
        List<KeyValue> headers = this.standardHttpRequestParam.getHeaders();
        if (headers == null) {
            return new ArrayList<>();
        }
        return headers.stream().map(KeyValue::getKey).collect(Collectors.toList());
    }

    @Override
    public FormURLEncodedBody getIfFormURLEncodedBody() {
        Body body = standardHttpRequestParam.getBody();
        return new FormURLEncodedBody() {
            @Override
            public List<String> getValue(String key) {
                if (body instanceof FormUrlBody) {
                    List<KeyValue> data = ((FormUrlBody) body).getData();
                    if (data == null) return null;
                    List<String> result = new ArrayList<>();
                    for (KeyValue datum : data) {
                        if (StringUtils.isEqualsIgnoreCase(key, datum.getKey())) {
                            result.add(datum.getValue());
                        }
                    }
                    return result;
                }
                return null;
            }

            @Override
            public String getOneValue(String key) {
                List<String> value = getValue(key);
                if (value == null || value.isEmpty()) return null;
                return value.get(0);
            }
        };
    }

    @Override
    public JSONBody getIfJSONBody() {
        Body body = standardHttpRequestParam.getBody();
        if (body instanceof com.cool.request.lib.springmvc.JSONBody) {
            return key -> {
                TypeFactory typeFactory = TypeFactory.defaultInstance();
                MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
                try {
                    return ((Map<String,Object>)(new ObjectMapper().readValue(((com.cool.request.lib.springmvc.JSONBody) body).getValue(), mapType))).get(key);
                } catch (Exception ignored) {
                }
                return null;
            };
        }
        return null;
    }
}
