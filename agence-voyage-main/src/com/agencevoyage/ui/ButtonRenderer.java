package com.agencevoyage.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton modifierButton = new JButton("Modifier");
    private JButton supprimerButton = new JButton("Supprimer");

    public ButtonRenderer() {
        setLayout(new FlowLayout());
        add(modifierButton);
        add(supprimerButton);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        return this;
    }
}