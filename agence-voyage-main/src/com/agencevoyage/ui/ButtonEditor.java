package com.agencevoyage.ui;

import javax.swing.*;
import java.awt.*;

public class ButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private String label;
    private boolean isPushed;
    private final JTable table;
    private final ClientList parent;
    private static final int COL_ID = 0;

    public ButtonEditor(JCheckBox checkBox, JTable table, ClientList parent) {
        super(checkBox);
        this.table = table;
        this.parent = parent;

        button = new JButton();
        button.setOpaque(true);

        button.addActionListener(e -> {
            fireEditingStopped();
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                Object val = table.getModel().getValueAt(modelRow, COL_ID);
                if (val != null) {
                    parent.modifierClient(val.toString());
                } else {
                    JOptionPane.showMessageDialog(table, "ID du client introuvable.");
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        button.setText((value == null) ? "" : value.toString());
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() { isPushed = false; return label; }

    @Override
    public boolean stopCellEditing() { isPushed = false; return super.stopCellEditing(); }

    @Override
    protected void fireEditingStopped() { super.fireEditingStopped(); }
}
