package com.agencevoyage.model;

import java.time.LocalDate;
import java.util.UUID;

public class Reservation {
    private String id;
    private Client client;
    private Prestation prestation;
    private LocalDate dateReservation;

    public Reservation(Client client, Prestation prestation) {
        this.id = UUID.randomUUID().toString();
        this.client = client;
        this.prestation = prestation;
        this.dateReservation = LocalDate.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }  // Added this setter
    public Client getClient() { return client; }
    public Prestation getPrestation() { return prestation; }
    public LocalDate getDateReservation() { return dateReservation; }

    @Override
    public String toString() {
        return "Reservation #" + id + " - " + client.getNom() + " for " + prestation.getDesignation();
    }
}