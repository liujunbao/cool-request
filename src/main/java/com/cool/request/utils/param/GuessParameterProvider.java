/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * GuessParameterProvider.java is part of Cool Request
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

package com.cool.request.utils.param;

import com.cool.request.common.bean.EmptyEnvironment;
import com.cool.request.common.bean.RequestEnvironment;
import com.cool.request.components.http.Controller;
import com.cool.request.components.http.KeyValue;
import com.cool.request.components.http.RequestParameterDescription;
import com.cool.request.components.http.net.HttpMethod;
import com.cool.request.lib.springmvc.*;
import com.cool.request.scan.HttpRequestParamUtils;
import com.cool.request.utils.CollectionUtils;
import com.cool.request.utils.ControllerUtils;
import com.cool.request.utils.StringUtils;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuessParameterProvider implements HTTPParameterProvider {
    private HttpRequestInfo getHttpRequestInfo(Project project, Controller controller) {
        HttpRequestInfo httpRequestInfo = null;
        if (SwingUtilities.isEventDispatchThread()) {
            httpRequestInfo = HttpRequestParamUtils.getHttpRequestInfo(project, controller);
        } else {
            httpRequestInfo = ApplicationManager
                    .getApplication()
                    .runReadAction((Computable<HttpRequestInfo>) () -> HttpRequestParamUtils.getHttpRequestInfo(project, controller));
        }
        return httpRequestInfo;
    }

    @Override
    public List<KeyValue> getHeader(Project project, Controller controller, RequestEnvironment environment) {

        List<KeyValue> guessHeader = getHttpRequestInfo(project, controller).getHeaders().stream()
                .map(requestParameterDescription -> new KeyValue(requestParameterDescription.getName(), "")).collect(Collectors.toList());
        return CollectionUtils.merge(guessHeader, environment.getHeader());
    }

    @Override
    public List<KeyValue> getUrlParam(Project project, Controller controller, RequestEnvironment environment) {
        HttpRequestInfo httpRequestInfo = getHttpRequestInfo(project, controller);

        List<KeyValue> guessParam = httpRequestInfo.getUrlParams().stream()
                .map(requestParameterDescription -> new KeyValue(requestParameterDescription.getName(), "",
                        requestParameterDescription.getType())).collect(Collectors.toList());
        return CollectionUtils.merge(guessParam, environment.getUrlParam());
    }

    @Override
    public Body getBody(Project project, Controller controller, RequestEnvironment environment) {
        HttpRequestInfo httpRequestInfo = getHttpRequestInfo(project, controller);
        GuessBody requestBody = httpRequestInfo.getRequestBody();
        //目前参数推测只支持两种String和JSON
        if (requestBody instanceof StringGuessBody) {
            return new StringBody(((StringGuessBody) requestBody).getValue());
        }

        if (requestBody instanceof JSONObjectGuessBody) {
            Map<String, Object> json = ((JSONObjectGuessBody) requestBody).getJson();
            if (json != null) {
                return new JSONBody(new Gson().toJson(((JSONObjectGuessBody) requestBody).getJson()));
            }
        }
        if (httpRequestInfo.getFormDataInfos() != null && !httpRequestInfo.getFormDataInfos().isEmpty()) {
            return new FormBody(httpRequestInfo.getFormDataInfos());
        }

        List<RequestParameterDescription> urlencodedBody = httpRequestInfo.getUrlencodedBody();
        if (urlencodedBody != null && !urlencodedBody.isEmpty()) {
            List<KeyValue> keyValues = urlencodedBody.stream()
                    .map(requestParameterDescription ->
                            new KeyValue(requestParameterDescription.getName(), "")).collect(Collectors.toList());
            return new FormUrlBody(CollectionUtils.merge(keyValues, environment.getFormUrlencoded()));
        }
        return new EmptyBody();
    }

    @Override
    public String getFullUrl(Project project, Controller controller, RequestEnvironment environment) {
        if (environment instanceof EmptyEnvironment) {
            return ControllerUtils.buildLocalhostUrl(controller);
        }
        return StringUtils.joinUrlPath(environment.getHostAddress(), controller.getUrl());
    }

    @Override
    public HttpMethod getHttpMethod(Project project, Controller controller, RequestEnvironment environment) {
        return HttpMethod.parse(controller.getHttpMethod());
    }
}
