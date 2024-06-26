/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * HTTPCallMethodResponse.java is part of Cool Request
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

package com.cool.request.components.http.net.response;

import com.cool.request.common.constant.CoolRequestIdeaTopic;
import com.cool.request.common.model.ErrorHTTPResponseBody;
import com.cool.request.components.http.HTTPResponseListener;
import com.cool.request.components.http.HTTPResponseManager;
import com.cool.request.components.http.Header;
import com.cool.request.components.http.net.HTTPHeader;
import com.cool.request.components.http.net.HTTPResponseBody;
import com.cool.request.components.http.net.HttpRequestCallMethod;
import com.cool.request.components.http.net.RequestContext;
import com.cool.request.utils.Base64Utils;
import com.cool.request.utils.StringUtils;
import com.intellij.openapi.project.Project;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPCallMethodResponse implements HttpRequestCallMethod.SimpleCallback {
    private final Project project;
    private final Map<RequestContext, Thread> waitResponseThread;
    private final List<HTTPResponseListener> httpResponseListeners;
    private final RequestContext requestContext;

    public HTTPCallMethodResponse(Project project,
                                  Map<RequestContext, Thread> waitResponseThread,
                                  List<HTTPResponseListener> httpResponseListeners,
                                  RequestContext requestContext) {
        this.project = project;
        this.waitResponseThread = waitResponseThread;
        this.httpResponseListeners = httpResponseListeners;
        this.requestContext = requestContext;
    }

    @Override
    public void onResponse(String requestId, int code, Response response) {
        //可能被用户取消了，然后才响应成功，不做处理
        if (!waitResponseThread.containsKey(requestContext)) {
            return;
        }
        Headers okHttpHeaders = response.headers();
        List<Header> headers = new ArrayList<>();
        int headerCount = okHttpHeaders.size();
        for (int i = 0; i < headerCount; i++) {
            String headerName = okHttpHeaders.name(i);
            String headerValue = okHttpHeaders.value(i);
            headers.add(new Header(headerName, headerValue));
        }
        HTTPResponseBody httpResponseBody = new HTTPResponseBody();

        httpResponseBody.setBase64BodyData("");
        httpResponseBody.setCode(response.code());
        httpResponseBody.setId(requestId);
        httpResponseBody.setHeader(headers);
        httpResponseBody.setSize(0);
        if (response.body() != null) {
            try {
                byte[] bytes = response.body().bytes();
                httpResponseBody.setSize(bytes.length);
                bytes = HTTPResponseManager.getInstance(project).bodyConverter(bytes, new HTTPHeader(headers));
                httpResponseBody.setBase64BodyData(Base64Utils.encodeToString(bytes));
            } catch (IOException ignored) {
            }
        }
        requestContext.endSend(httpResponseBody);
        //通知全局的监听器
        HTTPResponseManager.getInstance(project).onHTTPResponse(httpResponseBody,requestContext);
    }

    @Override
    public void onError(String requestId, IOException e) {
        ErrorHTTPResponseBody errorHTTPResponseBody = new ErrorHTTPResponseBody(e.getMessage().getBytes());
        requestContext.endSend(errorHTTPResponseBody);
        project.getMessageBus()
                .syncPublisher(CoolRequestIdeaTopic.HTTP_RESPONSE)
                .onResponseEvent(requestId, errorHTTPResponseBody, requestContext);
    }
}
