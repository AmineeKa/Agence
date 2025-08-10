package com.agencevoyage.ui;

import com.agencevoyage.model.Prestation;
import com.agencevoyage.service.PrestationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PrestationList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private final PrestationService prestationService = new PrestationService();

    private JButton addButton;
    private JButton editButton;
    private JButton refreshButton;

    private static final int COL_ID = 0;

    public PrestationList() {
        setTitle("Liste des prestations");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        model = new DefaultTableModel(
                new Object[]{"ID", "Désignation", "Type", "Hôtel", "Ville/Pays", "Date début", "Date fin", "Prix", "Places dispo"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(table.getFont().deriveFont(18f));
        table.setRowHeight(table.getRowHeight() + 14);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        addButton = new JButton("Ajouter Prestation");
        addButton.setFont(addButton.getFont().deriveFont(18f));
        addButton.addActionListener(e -> new PrestationForm(PrestationList.this).setVisible(true));

        editButton = new JButton("Modifier");
        editButton.setFont(editButton.getFont().deriveFont(18f));
        editButton.addActionListener(e -> {
            String id = selectedId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une prestation à modifier.");
                return;
            }
            modifierPrestation(id);
        });

        refreshButton = new JButton("Rafraîchir");
        refreshButton.setFont(refreshButton.getFont().deriveFont(18f));
        refreshButton.addActionListener(e -> rafraichir());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);     // << moved here (bottom), no more per-row button
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        rafraichir();

        // Enable/disable edit based on selection
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        updateButtons();
    }

    private String selectedId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object v = model.getValueAt(modelRow, COL_ID);
        return v == null ? null : v.toString();
    }

    private void updateButtons() {
        editButton.setEnabled(table.getSelectedRow() >= 0);
    }

    public void rafraichir() {
        List<Prestation> prestations = prestationService.getAllPrestations();
        model.setRowCount(0);
        for (Prestation p : prestations) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getDesignation(),
                    p.getType(),
                    p.getHotel(),
                    p.getVillePays(),
                    p.getDateDebut(),
                    p.getDateFin(),
                    p.getPrix(),
                    p.getPlacesDispo()
            });
        }
        updateButtons();
    }

    public void modifierPrestation(String id) {
        // Use the edit-capable form; if you created EditPrestationForm, you can swap it here.
        new PrestationForm(this, id).setVisible(true);
        rafraichir();
    }
}
