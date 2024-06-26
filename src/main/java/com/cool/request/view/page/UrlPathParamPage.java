/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * UrlPathParamPage.java is part of Cool Request
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

package com.cool.request.view.page;

import com.cool.request.components.http.net.RequestParamApply;
import com.cool.request.components.http.net.request.StandardHttpRequestParam;
import com.cool.request.view.table.EmptySuggestFactory;
import com.cool.request.view.table.KeyValueTablePanel;
import com.intellij.openapi.project.Project;

public class UrlPathParamPage extends KeyValueTablePanel implements RequestParamApply {
    public UrlPathParamPage(Project project) {
        super(EmptySuggestFactory.getInstance());
    }

    @Override
    public void configRequest(StandardHttpRequestParam standardHttpRequestParam) {

    }
}
