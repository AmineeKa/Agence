package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

public class ClientForm extends JFrame {
    private static boolean isOpen = false;

    private final ClientList parent;

    private JTextField nomField;
    private JTextField prenomField;
    private JTextField emailField;
    private JTextField phoneField;

    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public ClientForm(ClientList parent) {
        if (isOpen) {
            JOptionPane.showMessageDialog(parent, "La fenêtre d'ajout client est déjà ouverte.");
            dispose();
            throw new IllegalStateException("ClientForm déjà ouvert");
        }
        isOpen = true;

        this.parent = parent;

        setTitle("Ajouter Client");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { isOpen = false; }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { isOpen = false; }
        });
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 12, 10, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;

        nomField = new JTextField(28);
        prenomField = new JTextField(28);
        emailField = new JTextField(28);
        phoneField = new JTextField(18);

        int r = 0;
        addRow(form, gc, r++, "Nom", nomField);
        addRow(form, gc, r++, "Prénom", prenomField);
        addRow(form, gc, r++, "Email", emailField);
        addRow(form, gc, r++, "Téléphone", phoneField);

        JButton save = new JButton("Enregistrer");
        save.addActionListener(e -> enregistrer());
        JButton cancel = new JButton("Annuler");
        cancel.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(save);
        actions.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(actions, BorderLayout.SOUTH);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        p.add(new JLabel(label + " :"), gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(field, gc);
    }

    private void enregistrer() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom, prénom et email sont obligatoires.");
            return;
        }
        if (!EMAIL_RX.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un email valide.");
            return;
        }

        try (Connection c = DatabaseManager.getConnection()) {
            // duplicate email
            try (PreparedStatement chk = c.prepareStatement("SELECT 1 FROM CLIENT WHERE EMAIL = ?")) {
                chk.setString(1, email);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Un client avec cet email existe déjà.");
                        return;
                    }
                }
            }
            // duplicate phone (allow null/empty)
            if (!phone.isBlank()) {
                try (PreparedStatement chk = c.prepareStatement("SELECT 1 FROM CLIENT WHERE TELEPHONE = ?")) {
                    chk.setString(1, phone);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this, "Un client avec ce numéro de téléphone existe déjà.");
                            return;
                        }
                    }
                }
            }

            // insert with UUID ID
            String ins = "INSERT INTO CLIENT (ID, NOM, PRENOM, EMAIL, TELEPHONE, ISDELETED) VALUES (?,?,?,?,?,FALSE)";
            try (PreparedStatement ps = c.prepareStatement(ins)) {
                int i = 1;
                ps.setString(i++, UUID.randomUUID().toString());
                ps.setString(i++, nom);
                ps.setString(i++, prenom);
                ps.setString(i++, email);
                ps.setString(i,   phone.isBlank() ? null : phone);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Client ajouté avec succès.");
            if (parent != null) parent.rafraichir();
            dispose();

        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                JOptionPane.showMessageDialog(this, "Email ou téléphone déjà utilisé par un autre client.");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur SQL: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        isOpen = false;
    }
}
