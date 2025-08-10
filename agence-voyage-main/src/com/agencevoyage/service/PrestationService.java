package com.agencevoyage.service;

import com.agencevoyage.model.Prestation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestationService {

    public List<Prestation> getAllPrestations() {
        String sql = """
            SELECT ID, DESIGNATION, TYPE, HOTEL, VILLE, PAYS,
                   DATEDEBUT, DATEFIN, PRIX, PLACESDISPONIBLES
            FROM PRESTATION
            ORDER BY DATEDEBUT
        """;
        List<Prestation> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur chargement prestations: " + e.getMessage(), e);
        }
        return list;
    }

    public Prestation getPrestationById(String id) {
        String sql = """
            SELECT ID, DESIGNATION, TYPE, HOTEL, VILLE, PAYS,
                   DATEDEBUT, DATEFIN, PRIX, PLACESDISPONIBLES
            FROM PRESTATION
            WHERE ID = ?
        """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur getPrestationById: " + e.getMessage(), e);
        }
    }

    public void updatePrestation(Prestation p) {
        String sql = """
            UPDATE PRESTATION
               SET DESIGNATION = ?, TYPE = ?, HOTEL = ?, VILLE = ?, PAYS = ?,
                   DATEDEBUT = ?, DATEFIN = ?, PRIX = ?, PLACESDISPONIBLES = ?
             WHERE ID = ?
        """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, p.getDesignation());
            ps.setString(i++, nullIfBlank(p.getType()));
            ps.setString(i++, nullIfBlank(p.getHotel()));
            ps.setString(i++, nullIfBlank(p.getVille()));
            ps.setString(i++, nullIfBlank(p.getPays()));
            ps.setDate(i++, toSqlDate(p.getDateDebut()));
            ps.setDate(i++, toSqlDate(p.getDateFin()));
            ps.setDouble(i++, p.getPrix());
            ps.setInt(i++, p.getPlacesDisponibles());
            ps.setString(i, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur updatePrestation: " + e.getMessage(), e);
        }
    }

    public void deletePrestation(String id) {
        String sql = "DELETE FROM PRESTATION WHERE ID = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur deletePrestation: " + e.getMessage(), e);
        }
    }

    // --- helpers ---

    private Prestation map(ResultSet rs) throws SQLException {
        // match 9-arg constructor (no ID), then set ID
        Prestation p = new Prestation(
                rs.getString("DESIGNATION"),
                rs.getString("TYPE"),
                rs.getString("HOTEL"),
                rs.getString("VILLE"),
                rs.getString("PAYS"),
                toLocalDate(rs.getDate("DATEDEBUT")),
                toLocalDate(rs.getDate("DATEFIN")),
                rs.getDouble("PRIX"),
                rs.getInt("PLACESDISPONIBLES")
        );
        p.setId(rs.getString("ID"));
        return p;
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static Date toSqlDate(LocalDate d) {
        return d == null ? null : Date.valueOf(d);
    }

    private static LocalDate toLocalDate(Date d) {
        return d == null ? null : d.toLocalDate();
    }
}
