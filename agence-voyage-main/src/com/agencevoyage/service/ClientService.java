package com.agencevoyage.service;

import com.agencevoyage.model.Client;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class ClientService {
    private List<Client> clients = new ArrayList<>();

    public void ajouterClient(Client client) {
        clients.add(client);
    }

    public void modifierClient(Client client, String nom, String prenom, String email, String telephone) {
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);
        client.setTelephone(telephone);
    }

    public void archiverClient(Client client) {
        client.setArchive(true);
    }

    public void restaurerClient(Client client) {
        client.setArchive(false);
    }

    public List<Client> listerClientsActifs() {
        List<Client> actifs = new ArrayList<>();
        for (Client c : clients) {
            if (!c.isArchive()) {
                actifs.add(c);
            }
        }
        return actifs;
    }

    public List<Client> listerClientsArchives() {
        List<Client> archives = new ArrayList<>();
        for (Client c : clients) {
            if (c.isArchive()) {
                archives.add(c);
            }
        }
        return archives;
    }

    public Client rechercherParId(String id) {
        for (Client c : clients) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
    /**
     * Retrieves all clients from the CLIENT table in the database.
     *
     * @return a list of clients persisted in the database
     */
    public List<Client> getClients() {
        List<Client> clientsFromDb = new ArrayList<>();
        String sql = "SELECT * FROM CLIENT";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Client c = new Client(
                        rs.getString("NOM"),
                        rs.getString("PRENOM"),
                        rs.getString("EMAIL"),
                        rs.getString("TELEPHONE")
                );
                c.setId(rs.getString("ID"));
                // Map the ISDELETED flag to the archive property
                boolean isDeleted = rs.getBoolean("ISDELETED");
                c.setArchive(isDeleted);
                clientsFromDb.add(c);
            }
        } catch (SQLException e) {
            // In case of error, log and return the in-memory list as fallback
            System.err.println("Error loading clients from DB: " + e.getMessage());
            return new ArrayList<>(clients);
        }
        return clientsFromDb;
    }

    /**
     * Returns all clients. This method delegates to {@link #getClients()} and is kept
     * for backward compatibility with older code.
     *
     * @return a list of all clients
     */
    public List<Client> getAllClients() {
        return getClients();
    }
}