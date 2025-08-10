package com.agencevoyage.ui;

import com.agencevoyage.service.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditPrestationForm extends JFrame {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PrestationList parent;
    private String prestationId;
    public static boolean isOpen = false;

    private JTextField designationField, typeField, hotelField, villeField, paysField;
    private JTextField dateDebutField, dateFinField, prixField, placesField;

    public EditPrestationForm(PrestationList parent, String prestationId) {
        if (isOpen) {
            JOptionPane.showMessageDialog(parent, "La fenêtre de modification prestation est déjà ouverte.");
            dispose();
            return;
        }
        isOpen = true;

        this.parent = parent;
        this.prestationId = prestationId;

        setTitle("Modifier Prestation");
        setSize(620, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 12, 8, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;

        designationField = new JTextField(28);
        typeField = new JTextField(28);
        hotelField = new JTextField(28);
        villeField = new JTextField(18);
        paysField = new JTextField(18);
        dateDebutField = new JTextField(12);
        dateFinField = new JTextField(12);
        prixField = new JTextField(10);
        placesField = new JTextField(10);

        int r=0;
        addRow(form, gc, r++, "Désignation", designationField);
        addRow(form, gc, r++, "Type",        typeField);
        addRow(form, gc, r++, "Hôtel",       hotelField);

        JPanel vp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        vp.add(new JLabel("Ville")); vp.add(villeField);
        vp.add(new JLabel("Pays"));  vp.add(paysField);
        addRow(form, gc, r++, "Localisation", vp);

        JPanel dp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dp.add(new JLabel("Début (yyyy-MM-dd)")); dp.add(dateDebutField);
        dp.add(new JLabel("Fin (yyyy-MM-dd)"));   dp.add(dateFinField);
        addRow(form, gc, r++, "Période", dp);

        JPanel tp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tp.add(new JLabel("Prix")); tp.add(prixField);
        tp.add(new JLabel("Places dispo")); tp.add(placesField);
        addRow(form, gc, r++, "Tarif & Places", tp);

        JButton save = new JButton("Mettre à jour");
        save.addActionListener(e -> enregistrer());
        JButton cancel = new JButton("Annuler");
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(save); south.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { isOpen = false; }
            public void windowClosed (java.awt.event.WindowEvent e) { isOpen = false; }
        });

        charger(prestationId);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field){
        gc.gridx=0; gc.gridy=row; gc.weightx=0; p.add(new JLabel(label+" :"), gc);
        gc.gridx=1; gc.weightx=1; p.add(field, gc);
    }

    private void charger(String id) {
        String sql = "SELECT DESIGNATION, TYPE, HOTEL, VILLE, PAYS, DATEDEBUT, DATEFIN, PRIX, PLACESDISPONIBLES FROM PRESTATION WHERE ID=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    designationField.setText(nz(rs.getString(1)));
                    typeField.setText(nz(rs.getString(2)));
                    hotelField.setText(nz(rs.getString(3)));
                    villeField.setText(nz(rs.getString(4)));
                    paysField.setText(nz(rs.getString(5)));
                    dateDebutField.setText(rs.getDate(6) != null ? rs.getDate(6).toLocalDate().format(DATE_FMT) : "");
                    dateFinField.setText(rs.getDate(7) != null ? rs.getDate(7).toLocalDate().format(DATE_FMT) : "");
                    prixField.setText(String.valueOf(rs.getDouble(8)));
                    placesField.setText(String.valueOf(rs.getInt(9)));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de chargement: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enregistrer() {
        String designation = designationField.getText().trim();
        String type        = typeField.getText().trim();
        String hotel       = hotelField.getText().trim();
        String ville       = villeField.getText().trim();
        String pays        = paysField.getText().trim();

        if (designation.isEmpty()) {
            err("La désignation est obligatoire.");
            return;
        }

        LocalDate dDebut, dFin;
        try {
            dDebut = parse(dateDebutField.getText().trim(), "Date début invalide (yyyy-MM-dd).");
            dFin   = parse(dateFinField.getText().trim(),   "Date fin invalide (yyyy-MM-dd).");
        } catch (IllegalArgumentException ex) { err(ex.getMessage()); return; }
        if (dFin.isBefore(dDebut)) { err("La date de fin doit être ≥ la date de début."); return; }

        double prix; int places;
        try { prix = Double.parseDouble(prixField.getText().trim()); if (prix < 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { err("Le prix doit être un nombre positif."); return; }
        try { places = Integer.parseInt(placesField.getText().trim()); if (places < 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { err("Les places doivent être un entier positif."); return; }

        // duplicate safety (natural key) excluding current ID
        String dup = """
            SELECT 1 FROM PRESTATION
            WHERE DESIGNATION=? AND COALESCE(HOTEL,'')=? AND COALESCE(VILLE,'')=? AND COALESCE(PAYS,'')=?
              AND DATEDEBUT=? AND DATEFIN=? AND ID<>?
        """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement chk = c.prepareStatement(dup)) {
            int i=1;
            chk.setString(i++, designation);
            chk.setString(i++, hotel.isBlank()? "" : hotel);
            chk.setString(i++, ville.isBlank()? "" : ville);
            chk.setString(i++, pays.isBlank()? ""  : pays);
            chk.setDate(i++, Date.valueOf(dDebut));
            chk.setDate(i++, Date.valueOf(dFin));
            chk.setString(i,  prestationId);
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) { err("Cette prestation existe déjà (doublon)."); return; }
            }
        } catch (SQLException ex) {
            err("Erreur SQL (vérif doublon): " + ex.getMessage());
            return;
        }

        String upd = "UPDATE PRESTATION SET DESIGNATION=?, TYPE=?, HOTEL=?, VILLE=?, PAYS=?, DATEDEBUT=?, DATEFIN=?, PRIX=?, PLACESDISPONIBLES=? WHERE ID=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(upd)) {
            int i=1;
            ps.setString(i++, designation);
            ps.setString(i++, type.isBlank()? null : type);
            ps.setString(i++, hotel.isBlank()? null : hotel);
            ps.setString(i++, ville.isBlank()? null : ville);
            ps.setString(i++, pays.isBlank()? null : pays);
            ps.setDate(i++, Date.valueOf(dDebut));
            ps.setDate(i++, Date.valueOf(dFin));
            ps.setDouble(i++, prix);
            ps.setInt(i++, places);
            ps.setString(i, prestationId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Prestation mise à jour.");
            if (parent != null) parent.rafraichir();
            dispose();

        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                err("Doublon prestation (mêmes infos et période).");
            } else {
                err("Erreur SQL: " + ex.getMessage());
            }
        }
    }

    private LocalDate parse(String s, String msg) {
        try { return LocalDate.parse(s, DATE_FMT); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException(msg); }
    }

    private static String nz(String s){ return s==null? "" : s; }
    private void err(String m){ JOptionPane.showMessageDialog(this, m, "Erreur", JOptionPane.ERROR_MESSAGE); }

    @Override
    public void dispose() {
        super.dispose();
        isOpen = false;
    }
}
