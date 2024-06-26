/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * ApiFox.java is part of Cool Request
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

package com.cool.request.view.dialog;

import com.cool.request.common.icons.CoolRequestIcons;
import com.cool.request.common.state.SettingPersistentState;
import com.cool.request.plugin.apifox.ApiFoxExport;
import com.cool.request.plugin.apifox.ApiFoxExportCondition;
import com.cool.request.plugin.apifox.ApifoxSetting;
import com.cool.request.utils.ProgressWindowWrapper;
import com.cool.request.utils.WebBrowseUtils;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class ApiFox implements ConfigurableUi<ApifoxSetting>, ActionListener {

    private JPanel root;
    private JTextField httpText;
    private JTextField tokenText;
    private JButton checkButton;
    private JLabel httpResult;
    private JLabel tokenResult;
    private JLabel help;
    private final ApiFoxExport apiFoxExport;
    private final Project project;

    public ApiFox(Project project) {
        apiFoxExport = new ApiFoxExport(project);
        this.project = project;
        help.setIcon(CoolRequestIcons.HELP);
        help.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                WebBrowseUtils.browse("https://plugin.houxinlin.com/docs/three-part/apifox");
            }
        });
    }

    @Override
    public void reset(@NotNull ApifoxSetting settings) {
        httpText.setText(settings.getHttpToken());
        tokenText.setText(settings.getApiToken());
    }

    @Override
    public boolean isModified(@NotNull ApifoxSetting settings) {
        SettingPersistentState instance = SettingPersistentState.getInstance();
        return !instance.getState().apiFoxAuthorization.equalsIgnoreCase(httpText.getText()) ||
                !instance.getState().openApiToken.equalsIgnoreCase(tokenText.getText());
    }

    @Override
    public void apply(@NotNull ApifoxSetting settings) throws ConfigurationException {
        SettingPersistentState instance = SettingPersistentState.getInstance();
        instance.getState().apiFoxAuthorization = httpText.getText();
        instance.getState().openApiToken = tokenText.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tokenResult.setText("");
        httpResult.setText("");
        ProgressWindowWrapper.newProgressWindowWrapper(project).run(new Task.Backgroundable(project, "Checking....") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ApiFoxExportCondition apiFoxExportCondition = new ApiFoxExportCondition(httpText.getText(), tokenText.getText());
                Map<String, Boolean> result = apiFoxExport.checkToken(apiFoxExportCondition);
                SwingUtilities.invokeLater(() -> {
                    tokenResult.setText(result.getOrDefault(ApiFoxExportCondition.KEY_API_FOX_OPEN_AUTHORIZATION, false) ? "Success" : "Invalid Token");
                    httpResult.setText(result.getOrDefault(ApiFoxExportCondition.KEY_API_FOX_AUTHORIZATION, false) ? "Success" : "Invalid Token");
                });

            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        checkButton.addActionListener(this);
        return root;
    }

}
