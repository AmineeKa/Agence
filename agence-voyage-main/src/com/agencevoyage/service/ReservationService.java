package com.agencevoyage.service;

import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class ReservationService {

    public boolean creerReservation(String clientId, String prestationId) {
        String chkSql = "SELECT 1 FROM RESERVATION WHERE CLIENT_ID=? AND PRESTATION_ID=?";
        String decSql = "UPDATE PRESTATION SET PLACESDISPONIBLES = PLACESDISPONIBLES - 1 WHERE ID=? AND PLACESDISPONIBLES > 0";
        String insSql = "INSERT INTO RESERVATION (ID, CLIENT_ID, PRESTATION_ID, DATE_RESERVATION) VALUES (?,?,?,?)";

        try (Connection c = DatabaseManager.getConnection()) {
            c.setAutoCommit(false);

            // prevent duplicates
            try (PreparedStatement chk = c.prepareStatement(chkSql)) {
                chk.setString(1, clientId);
                chk.setString(2, prestationId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) { c.rollback(); return false; }
                }
            }

            // decrement a seat
            try (PreparedStatement dec = c.prepareStatement(decSql)) {
                dec.setString(1, prestationId);
                if (dec.executeUpdate() == 0) { c.rollback(); return false; }
            }

            // insert reservation
            try (PreparedStatement ins = c.prepareStatement(insSql)) {
                ins.setString(1, UUID.randomUUID().toString());
                ins.setString(2, clientId);
                ins.setString(3, prestationId);
                ins.setDate(4, Date.valueOf(LocalDate.now()));
                ins.executeUpdate();
            }

            c.commit();
            return true;
        } catch (SQLException e) {
            // safety: unique index could still trigger
            if ("23505".equals(e.getSQLState())) return false;
            return false;
        }
    }
}
