package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

public class EditClientForm extends JFrame {
    private static final Pattern EMAIL_RX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ClientList parent;
    private String editId; // UUID (VARCHAR)
    public static boolean isOpen = false;

    private JTextField nomField, prenomField, emailField, telField;

    public EditClientForm(ClientList parent, String clientId) {
        if (isOpen) {
            JOptionPane.showMessageDialog(parent, "La fenêtre de modification client est déjà ouverte.");
            dispose();
            return;
        }
        isOpen = true;

        this.parent = parent;
        this.editId = clientId;

        setTitle("Modifier Client");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10,12,10,12);
        gc.fill = GridBagConstraints.HORIZONTAL;

        nomField = new JTextField(28);
        prenomField = new JTextField(28);
        emailField = new JTextField(28);
        telField = new JTextField(20);

        int r=0;
        addRow(p, gc, r++, "Nom", nomField);
        addRow(p, gc, r++, "Prénom", prenomField);
        addRow(p, gc, r++, "Email", emailField);
        addRow(p, gc, r++, "Téléphone", telField);

        JButton save = new JButton("Mettre à jour");
        save.addActionListener(e -> saveClient());
        JButton cancel = new JButton("Annuler");
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(save); south.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { isOpen = false; }
            public void windowClosed (java.awt.event.WindowEvent e) { isOpen = false; }
        });

        loadClient();
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field){
        gc.gridx=0; gc.gridy=row; gc.weightx=0; p.add(new JLabel(label+" :"), gc);
        gc.gridx=1; gc.weightx=1; p.add(field, gc);
    }

    private void loadClient() {
        String sql = "SELECT NOM, PRENOM, EMAIL, TELEPHONE FROM CLIENT WHERE ID=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, editId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nomField.setText(nz(rs.getString(1)));
                    prenomField.setText(nz(rs.getString(2)));
                    emailField.setText(nz(rs.getString(3)));
                    telField.setText(nz(rs.getString(4)));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveClient() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String tel = telField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom, prénom et email sont obligatoires.");
            return;
        }
        if (!EMAIL_RX.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un email valide.");
            return;
        }

        try (Connection c = DatabaseManager.getConnection()) {
            // email duplicate (excluding this ID)
            try (PreparedStatement chk = c.prepareStatement(
                    "SELECT 1 FROM CLIENT WHERE EMAIL=? AND ID<>?")) {
                chk.setString(1, email);
                chk.setString(2, editId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Un client avec cet email existe déjà.");
                        return;
                    }
                }
            }
            // phone duplicate (excluding this ID), allow NULLs
            if (!tel.isBlank()) {
                try (PreparedStatement chk = c.prepareStatement(
                        "SELECT 1 FROM CLIENT WHERE TELEPHONE=? AND ID<>?")) {
                    chk.setString(1, tel);
                    chk.setString(2, editId);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this, "Un client avec ce numéro existe déjà.");
                            return;
                        }
                    }
                }
            }

            String upd = "UPDATE CLIENT SET NOM=?, PRENOM=?, EMAIL=?, TELEPHONE=? WHERE ID=?";
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, nom);
                ps.setString(2, prenom);
                ps.setString(3, email);
                ps.setString(4, tel.isBlank()? null : tel);
                ps.setString(5, editId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Client mis à jour.");
            if (parent != null) parent.rafraichir();
            dispose();

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this, "Email ou téléphone déjà utilisé par un autre client.");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage());
            }
        }
    }

    private static String nz(String s){ return s==null? "" : s; }

    @Override
    public void dispose() {
        super.dispose();
        isOpen = false;
    }
}
