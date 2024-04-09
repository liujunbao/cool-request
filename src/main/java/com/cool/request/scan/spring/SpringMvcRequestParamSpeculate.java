/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * SpringMvcRequestParamSpeculate.java is part of Cool Request
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

package com.cool.request.scan.spring;

import com.cool.request.lib.springmvc.HttpRequestInfo;
import com.cool.request.lib.springmvc.param.*;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

public class SpringMvcRequestParamSpeculate implements RequestParamSpeculate {
    private final List<RequestParamSpeculate> requestParamSpeculates = new ArrayList<>();

    public SpringMvcRequestParamSpeculate() {
        requestParamSpeculates.add(new UrlencodedSpeculate());
        requestParamSpeculates.add(new UrlParamSpeculate());
        requestParamSpeculates.add(new HeaderParamSpeculate());
        requestParamSpeculates.add(new JSONBodyParamSpeculate());
        requestParamSpeculates.add(new FormDataSpeculate());

        requestParamSpeculates.add(new PathParamSpeculate());
        requestParamSpeculates.add(new ResponseBodySpeculate());
        requestParamSpeculates.add(new StringBodyParamSpeculate());
    }

    @Override
    public void set(PsiMethod psiMethod, HttpRequestInfo httpRequestInfo) {
        for (RequestParamSpeculate requestParamSpeculate : requestParamSpeculates) {
            try {
                requestParamSpeculate.set(psiMethod, httpRequestInfo);
            } catch (Exception ignored) {
            }
        }
    }
}
