package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ClientList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton addBtn, editBtn, archiveBtn, openArchiveBtn, refreshBtn;

    private static final int COL_ID = 0;
    private static final int COL_NOM = 1;
    private static final int COL_PRENOM = 2;
    private static final int COL_EMAIL = 3;
    private static final int COL_TEL = 4;
    private static final int COL_ISDELETED = 5;

    public ClientList() {
        setTitle("Clients");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        top.add(new JLabel("Recherche:"));
        searchField = new JTextField(30);
        JButton searchBtn = new JButton("Rechercher");
        top.add(searchField);
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "ID", "Nom", "Prénom", "Email", "Téléphone", "ISDELETED"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c==COL_ISDELETED?Boolean.class:super.getColumnClass(c); }
        };
        table = new JTable(model);
        TableColumn hidden = table.getColumnModel().getColumn(COL_ISDELETED);
        table.getColumnModel().removeColumn(hidden);
        table.setRowHeight(table.getRowHeight() + 10);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        addBtn = new JButton("Ajouter");
        editBtn = new JButton("Modifier");
        archiveBtn = new JButton("Archiver");
        openArchiveBtn = new JButton("ARCHIVE"); // opens archived list window
        refreshBtn = new JButton("Rafraîchir");
        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(archiveBtn);
        bottom.add(openArchiveBtn);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        // listeners
        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        addBtn.addActionListener(e -> new ClientForm(this).setVisible(true));
        editBtn.addActionListener(e -> {
            String id = selectedId();
            if (id != null) new EditClientForm(this, id).setVisible(true);
        });
        archiveBtn.addActionListener(e -> {
            String id = selectedId();
            if (id != null) setArchived(id, true);
        });
        openArchiveBtn.addActionListener(e -> new ArchivedClientList().setVisible(true));
        refreshBtn.addActionListener(e -> refresh());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        refresh();
        updateButtons();
    }

    private String selectedId() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        int m = table.convertRowIndexToModel(r);
        return (String) model.getValueAt(m, COL_ID);
    }

    private void setArchived(String id, boolean isDeleted) {
        int ok = JOptionPane.showConfirmDialog(this,
                (isDeleted ? "Archiver" : "Restaurer") + " ce client ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE CLIENT SET ISDELETED=? WHERE ID=?")) {
            ps.setBoolean(1, isDeleted);
            ps.setString(2, id);
            ps.executeUpdate();
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur SQL: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtons() {
        boolean hasSel = table.getSelectedRow() >= 0;
        boolean isDeleted = false;
        if (hasSel) {
            int m = table.convertRowIndexToModel(table.getSelectedRow());
            isDeleted = (Boolean) model.getValueAt(m, COL_ISDELETED);
        }
        editBtn.setEnabled(hasSel && !isDeleted);
        archiveBtn.setEnabled(hasSel && !isDeleted);
        // No "Restaurer" here anymore; that lives in ArchivedClientList
    }

    public void refresh() {
        model.setRowCount(0);
        String kw = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        StringBuilder sb = new StringBuilder(
                "SELECT ID, NOM, PRENOM, EMAIL, TELEPHONE, ISDELETED " +
                        "FROM CLIENT WHERE ISDELETED=FALSE ");
        if (!kw.isEmpty()) {
            sb.append("AND (LOWER(NOM) LIKE ? OR LOWER(PRENOM) LIKE ? OR LOWER(EMAIL) LIKE ? OR TELEPHONE LIKE ?) ");
        }
        sb.append("ORDER BY NOM");

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            int i = 1;
            if (!kw.isEmpty()) {
                String like = "%" + kw + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString(1));
                    row.add(rs.getString(2));
                    row.add(rs.getString(3));
                    row.add(rs.getString(4));
                    row.add(rs.getString(5));
                    row.add(rs.getBoolean(6));
                    model.addRow(row);
                }
            }
            updateButtons();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void rafraichir() { refresh(); }

    // For ButtonEditor compatibility (if you use it)
    public void modifierClient(String id) {
        new EditClientForm(this, id).setVisible(true);
        refresh();
    }
}
