/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * HTTPResponseView.java is part of Cool Request
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

import com.cool.request.action.response.BaseAction;
import com.cool.request.action.response.BaseToggleAction;
import com.cool.request.action.response.ToggleManager;
import com.cool.request.common.icons.CoolRequestIcons;
import com.cool.request.utils.GsonUtils;
import com.cool.request.utils.MessagesWrapperUtils;
import com.cool.request.utils.ResourceBundleUtils;
import com.cool.request.utils.StringUtils;
import com.cool.request.utils.file.FileChooseUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.HtmlPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponseView extends SimpleToolWindowPanel implements Disposable {
    private byte[] bytes;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel leftResponse = new JPanel(cardLayout);
    private final Map<String, ResponsePage> responsePageMap = new HashMap<>();
    private String currentTypeName = "json";
    private final ToggleManager toggleManager = getToggleManager();
    private String contentType = "";

    public HTTPResponseView(Project project) {
        super(false);
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new BaseToggleAction("json", AllIcons.Json.Object, toggleManager));
        group.add(new BaseToggleAction("text", CoolRequestIcons.TEXT, toggleManager));
        group.add(new BaseToggleAction("image", CoolRequestIcons.IMAGE, toggleManager));
        group.add(new BaseToggleAction("html", CoolRequestIcons.HTML, toggleManager));
        group.add(new BaseToggleAction("xml", CoolRequestIcons.XML, toggleManager));

        DefaultActionGroup toolGroup = new DefaultActionGroup();
        toolGroup.add(new BaseAction("Save", CoolRequestIcons.SAVE) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (HTTPResponseView.this.bytes == null || HTTPResponseView.this.bytes.length == 0) {
                    MessagesWrapperUtils.showErrorDialog("Response is Null", ResourceBundleUtils.getString("tip"));
                    return;
                }
                String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")) + "." + getDefaultSuffix();
                String storagePath = FileChooseUtils.chooseFileSavePath(null, name, e.getProject());
                if (storagePath == null) return;
                try {
                    Files.write(Paths.get(storagePath), HTTPResponseView.this.bytes);
                } catch (IOException ignored) {
                }
            }
        });
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("left-bar", group, false);
        ActionToolbar rightToolBar = ActionManager.getInstance().createActionToolbar("right-bar", toolGroup, false);
        toolbar.setTargetComponent(this);
        rightToolBar.setTargetComponent(this);
        setToolbar(toolbar.getComponent());

        responsePageMap.put("json", new JSON(project));
        responsePageMap.put("text", new Text(project));
        responsePageMap.put("image", new Image());
        responsePageMap.put("xml", new XML(project));
        responsePageMap.put("html", new Html());
        for (String key : responsePageMap.keySet()) {
            leftResponse.add(key, ((Component) responsePageMap.get(key)));
        }
        JPanel rightTool = new JPanel(new BorderLayout());
        rightTool.add(rightToolBar.getComponent());

        JPanel root = new JPanel(new BorderLayout());
        root.add(leftResponse, BorderLayout.CENTER);
        root.add(rightTool, BorderLayout.EAST);

        add(root);
        switchPage(currentTypeName);
    }

    @Override
    public void dispose() {
        responsePageMap.get("json").dispose();
        responsePageMap.get("text").dispose();
        responsePageMap.get("image").dispose();
        responsePageMap.get("xml").dispose();
        responsePageMap.get("html").dispose();
    }

    private String getDefaultSuffix() {
        if (this.contentType == null) return "txt";
        if (contentType.toLowerCase().startsWith("text/html")) return "html";
        if (contentType.toLowerCase().startsWith("application/json")) return "json";
        if (contentType.toLowerCase().startsWith("application/xml")) return "xml";
        if (contentType.toLowerCase().startsWith("text/plain")) return "txt";

        if (contentType.toLowerCase().startsWith("image/jpeg")) return "jpeg";
        if (contentType.toLowerCase().startsWith("image/jpg")) return "jpg";
        if (contentType.toLowerCase().startsWith("image/png")) return "png";
        if (contentType.toLowerCase().startsWith("image/gif")) return "gif";
        if (contentType.toLowerCase().startsWith("image/bmp")) return "bmp";
        if (contentType.toLowerCase().startsWith("image/webp")) return "webp";
        if (contentType.toLowerCase().startsWith("image/ico")) return "ico";
        if (contentType.toLowerCase().startsWith("image")) return "jpg";
        return "txt";
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setResponseData(String contentType, byte[] bytes) {
        this.bytes = bytes;
        this.contentType = contentType;
        responsePageMap.get(currentTypeName).init();
        autoSetType(contentType);
    }

    private void autoSetType(String contentType) {
        if (StringUtils.isEmpty(contentType)) return;
        if (contentType.startsWith("text/html")) {
            toggleManager.setSelect("html");
        } else if (contentType.startsWith("application/json")) {
            toggleManager.setSelect("json");
        } else if (contentType.startsWith("application/xml")) {
            toggleManager.setSelect("xml");
        } else if (contentType.startsWith("text")) {
            toggleManager.setSelect("text");
        } else if (contentType.startsWith("image")) {
            toggleManager.setSelect("image");
        } else {
            toggleManager.setSelect("text");
        }
    }

    public void switchPage(String name) {
        cardLayout.show(leftResponse, name);
        if (responsePageMap.containsKey(name)) {
            if (bytes != null) {
                responsePageMap.get(name).init();
                currentTypeName = name;
            }
        }
    }

    private ToggleManager getToggleManager() {
        Map<String, Boolean> selectMap = new HashMap<>();
        selectMap.put("json", true);
        selectMap.put("text", false);
        selectMap.put("image", false);
        selectMap.put("xml", false);
        selectMap.put("html", false);
        return new ToggleManager() {
            @Override
            public void setSelect(String name) {
                selectMap.replaceAll((s, v) -> false);
                selectMap.put(name, true);
                switchPage(name);
            }

            @Override
            public boolean isSelected(String name) {
                return selectMap.get(name);
            }
        };
    }

    public void reset() {
        this.bytes = new byte[]{};
        this.toggleManager.setSelect("json");
    }

    interface ResponsePage extends Disposable {
        void init();
    }

    class JSON extends BasicJSONRequestBodyPage implements ResponsePage {

        public JSON(Project project) {
            super(project);
        }

        @Override
        public void init() {
            setText(GsonUtils.format(new String(bytes, StandardCharsets.UTF_8)));
        }
    }

    class XML extends XmlParamRequestBodyPage implements ResponsePage {

        public XML(Project project) {
            super(project);
        }

        @Override
        public void init() {
            setText(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    class Html extends JScrollPane implements ResponsePage {
        private final HtmlPanel jEditorPane = new HtmlPanel() {
            @Override
            protected @NotNull @Nls String getBody() {
                return "<b></b>";
            }
        };

        public Html() {
            jEditorPane.setContentType("text/html");
            jEditorPane.setEditable(false);
            setViewportView(jEditorPane);
        }

        @Override
        public void init() {
            jEditorPane.setBody(new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public void dispose() {
        }
    }

    class Image extends JPanel implements ResponsePage {
        private BufferedImage image;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) return;
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            if (imageHeight < panelWidth && imageHeight < panelHeight) {

                g.drawImage(image, (panelWidth / 2) - (imageWidth / 2), (panelHeight / 2) - (imageHeight / 2), imageWidth, imageHeight, this);
                return;
            }
            double scaleX = (double) panelWidth / imageWidth;
            double scaleY = (double) panelHeight / imageHeight;
            double scale = Math.min(scaleX, scaleY);

            int scaledWidth = (int) (imageWidth * scale);
            int scaledHeight = (int) (imageHeight * scale);

            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            g.drawImage(image, x, y, scaledWidth, scaledHeight, this);
        }

        @Override
        public void dispose() {

        }

        @Override
        public void init() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try {
                this.image = ImageIO.read(inputStream);
                repaint();
            } catch (IOException ignored) {
            }
        }
    }

    class Text extends RawParamRequestBodyPage implements ResponsePage {

        public Text(Project project) {
            super(project);
        }

        @Override
        public void init() {
            setText(new String(bytes, StandardCharsets.UTF_8));
        }
    }

}
