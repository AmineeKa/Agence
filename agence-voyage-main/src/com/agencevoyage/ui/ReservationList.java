package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ReservationList extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton newBtn, refreshBtn, deleteBtn, closeBtn;

    public ReservationList() {
        setTitle("Réservations");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12,12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        top.add(new JLabel("Recherche (client/prestation):"));
        searchField = new JTextField(30);
        JButton searchBtn = new JButton("Rechercher");
        top.add(searchField);
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "ID", "Date réservation", "Client", "Prestation", "Ville/Pays"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(table.getRowHeight() + 10);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        newBtn = new JButton("Nouvelle réservation");
        refreshBtn = new JButton("Rafraîchir");
        deleteBtn = new JButton("Supprimer");
        closeBtn = new JButton("Fermer");
        bottom.add(newBtn); bottom.add(refreshBtn); bottom.add(deleteBtn); bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        // actions
        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        refreshBtn.addActionListener(e -> refresh());
        newBtn.addActionListener(e -> {
            new ReservationForm().setVisible(true);
            refresh();
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> dispose());

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        String kw = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        String base = """
            SELECT r.ID,
                   r.DATE_RESERVATION,
                   (c.NOM || ' ' || c.PRENOM) AS CLIENT,
                   p.DESIGNATION AS PRESTATION,
                   COALESCE(p.VILLE,'') || '/' || COALESCE(p.PAYS,'') AS VILLE_PAYS
            FROM RESERVATION r
            JOIN CLIENT c ON c.ID = r.CLIENT_ID
            JOIN PRESTATION p ON p.ID = r.PRESTATION_ID
            """;

        String where = "";
        if (!kw.isEmpty()) {
            where = "WHERE LOWER(c.NOM) LIKE ? OR LOWER(c.PRENOM) LIKE ? OR LOWER(p.DESIGNATION) LIKE ? ";
        }
        String order = "ORDER BY r.DATE_RESERVATION DESC";

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(base + where + order)) {
            if (!kw.isEmpty()) {
                String like = "%" + kw + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString(1));
                    row.add(rs.getDate(2));
                    row.add(rs.getString(3));
                    row.add(rs.getString(4));
                    row.add(rs.getString(5));
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        String id = (String) model.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        int ok = JOptionPane.showConfirmDialog(this,
                "Supprimer cette réservation ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // Optional: when deleting, you may want to increment back PLACESDISPONIBLES.
        String delSql = "DELETE FROM RESERVATION WHERE ID=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(delSql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            refresh();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur suppression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
