package com.agencevoyage.ui;

import javax.swing.*;
import java.awt.*;

public class ButtonEditorPrestations extends DefaultCellEditor {
    private final PrestationList parent;
    private final JButton button = new JButton();
    private JTable table;                 // set per-cell by Swing
    private static final int COL_ID = 0;  // ID column in model

    public ButtonEditorPrestations(JCheckBox checkBox, PrestationList parent) {
        super(checkBox);
        this.parent = parent;
        button.addActionListener(e -> {
            fireEditingStopped();
            if (table == null) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object v = table.getModel().getValueAt(modelRow, COL_ID);
            if (v != null) parent.modifierPrestation(v.toString());
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int column) {
        this.table = t; // capture current table
        button.setText(value == null ? "" : value.toString());
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }
}
