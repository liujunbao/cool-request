package com.hxl.plugin.springboot.invoke.view.page;

import com.hxl.plugin.springboot.invoke.net.FormDataInfo;
import com.hxl.plugin.springboot.invoke.view.ButtonColumn;
import com.hxl.plugin.springboot.invoke.view.page.cell.FormDataRequestBodyComboBoxEditor;
import com.hxl.plugin.springboot.invoke.view.page.cell.FormDataRequestBodyComboBoxRenderer;
import com.hxl.plugin.springboot.invoke.view.page.cell.FormDataRequestBodyValueEditor;
import com.hxl.plugin.springboot.invoke.view.page.cell.FormDataRequestBodyValueRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class FormDataRequestBodyPage extends JPanel {
    private static final String[] TABLE_HEADER_NAME = {"Key", "Value", "Type", "Delete"};
    private final DefaultTableModel defaultTableModel = new DefaultTableModel(null, TABLE_HEADER_NAME);
    private JBTable jTable;
    private final Project project;

    public FormDataRequestBodyPage(Project project) {
        this.project = project;
        init();
    }

    public void setFormData(List<FormDataInfo> value) {
        if (value == null) value = new ArrayList<>();
        value.add(new FormDataInfo("", "", "text"));
        defaultTableModel.setRowCount(0);
        for (FormDataInfo formDataInfo : value) {
            defaultTableModel.addRow(new String[]{formDataInfo.getName(), formDataInfo.getValue(), formDataInfo.getType(), "Delete"});
        }
        jTable.revalidate();
    }

    public void removeAllRow() {
        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }
        (((FormDataRequestBodyValueEditor) jTable.getColumnModel().getColumn(1).getCellEditor())).setCellEditorValue("");
    }

    public List<FormDataInfo> getFormData() {
        List<FormDataInfo> result = new ArrayList<>();
        for (int i = 0; i < jTable.getModel().getRowCount(); i++) {
            String key = jTable.getModel().getValueAt(i, 0).toString();
            if ("".equalsIgnoreCase(key)) continue;
            result.add(new FormDataInfo(key, jTable.getModel().getValueAt(i, 1).toString(),
                    jTable.getModel().getValueAt(i, 2).toString()));
        }
        return result;
    }

    private void init() {
        setLayout(new BorderLayout());
        defaultTableModel.addRow(new String[]{"", "", "text", "Delete"});
        jTable = new JBTable(defaultTableModel) {
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        Action delete = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.parseInt(e.getActionCommand());
                ((DefaultTableModel) table.getModel()).removeRow(modelRow);
                if (table.getModel().getRowCount() == 0)
                    defaultTableModel.addRow(new String[]{"", "", "text", "Delete"});
            }
        };
        defaultTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0
                    && !defaultTableModel.getValueAt(defaultTableModel.getRowCount() - 1, 0).toString().isEmpty()) {
                String[] strings = {"", "", "text", "Delete"};
                defaultTableModel.addRow(strings);
            }
        });
        ButtonColumn buttonColumn = new ButtonColumn(jTable, delete, 3);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
        jTable.setSelectionBackground(Color.getColor("#00000000"));
        TableColumn column = jTable.getColumnModel().getColumn(2);
        column.setCellRenderer(new FormDataRequestBodyComboBoxRenderer(jTable));
        column.setCellEditor(new FormDataRequestBodyComboBoxEditor(jTable));


        TableColumn column1 = jTable.getColumnModel().getColumn(1);
        column1.setCellRenderer(new FormDataRequestBodyValueRenderer());
        column1.setCellEditor(new FormDataRequestBodyValueEditor(jTable,project));
        jTable.setRowHeight(40);
        add(new JScrollPane(jTable), BorderLayout.CENTER);
    }
}