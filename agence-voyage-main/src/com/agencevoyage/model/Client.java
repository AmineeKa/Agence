package com.agencevoyage.model;

import java.util.UUID;

public class Client {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private boolean archive;

    public Client(String nom, String prenom, String email, String telephone) {
        this.id = UUID.randomUUID().toString();
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.archive = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}