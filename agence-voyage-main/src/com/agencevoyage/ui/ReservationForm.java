package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;
import com.agencevoyage.service.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReservationForm extends JFrame {
    private final JComboBox<Item> clientCombo = new JComboBox<>();
    private final JComboBox<Item> prestationCombo = new JComboBox<>();
    private final ReservationService service = new ReservationService();

    public ReservationForm() {
        setTitle("Créer une réservation");
        setSize(700, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12,12));

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12,12,12,12);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx=0; gc.gridy=0; center.add(new JLabel("Client :"), gc);
        gc.gridx=1; center.add(clientCombo, gc);
        gc.gridx=0; gc.gridy=1; center.add(new JLabel("Prestation :"), gc);
        gc.gridx=1; center.add(prestationCombo, gc);

        JButton create = new JButton("Confirmer la réservation");
        create.addActionListener(e -> createReservation());

        add(center, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(create);
        add(south, BorderLayout.SOUTH);

        loadClients();
        loadPrestations();
    }

    private void loadClients() {
        clientCombo.removeAllItems();
        String sql = "SELECT ID, NOM || ' ' || PRENOM FROM CLIENT WHERE ISDELETED=FALSE ORDER BY NOM";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) clientCombo.addItem(new Item(rs.getString(1), rs.getString(2)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement clients: " + e.getMessage());
        }
    }

    private void loadPrestations() {
        prestationCombo.removeAllItems();
        String sql = "SELECT ID, DESIGNATION || ' (' || COALESCE(VILLE,'') || '/' || COALESCE(PAYS,'') || ')' FROM PRESTATION WHERE PLACESDISPONIBLES > 0 ORDER BY DATEDEBUT";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prestationCombo.addItem(new Item(rs.getString(1), rs.getString(2)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement prestations: " + e.getMessage());
        }
    }

    private void createReservation() {
        Item client = (Item) clientCombo.getSelectedItem();
        Item prestation = (Item) prestationCombo.getSelectedItem();
        if (client == null || prestation == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un client et une prestation.");
            return;
        }
        boolean ok = service.creerReservation(client.id, prestation.id);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Réservation confirmée.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Réservation déjà existante pour ce client / Aucune place disponible.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static class Item {
        final String id; final String label;
        Item(String id, String label){ this.id=id; this.label=label==null?("(id:"+id+")"):label; }
        @Override public String toString(){ return label; }
    }
}
