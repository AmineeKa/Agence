package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ClientHistory extends JFrame {
    private final String clientId;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Réservation", "Date", "Prestation", "Hôtel", "Ville", "Pays"}, 0);

    public ClientHistory(String clientId) {
        this.clientId = clientId;
        setTitle("Historique des séjours");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        charger();
    }

    private void charger() {
        model.setRowCount(0);
        String sql = """
            SELECT r.ID, r.DATE_RESERVATION, p.DESIGNATION, p.HOTEL, p.VILLE, p.PAYS
            FROM RESERVATION r
            JOIN PRESTATION p ON p.ID = r.PRESTATION_ID
            WHERE r.CLIENT_ID = ?
            ORDER BY r.DATE_RESERVATION DESC
        """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString(1), rs.getDate(2),
                            rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
