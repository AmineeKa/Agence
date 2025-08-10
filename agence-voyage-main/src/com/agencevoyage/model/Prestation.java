package com.agencevoyage.model;

import java.time.LocalDate;
import java.util.UUID;

public class Prestation {
    private String id;
    private String designation;
    private String type;
    private String hotel;
    private String ville;
    private String pays;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prix;
    private int placesDisponibles;

    public Prestation(String designation, String type, String hotel, String ville, String pays,
                      LocalDate dateDebut, LocalDate dateFin, double prix, int placesDisponibles) {
        this.id = UUID.randomUUID().toString();
        this.designation = designation;
        this.type = type;
        this.hotel = hotel;
        this.ville = ville;
        this.pays = pays;
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setPrix(prix);
        setPlacesDisponibles(placesDisponibles);
    }

    // Getters and Setters with validation
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getHotel() { return hotel; }
    public void setHotel(String hotel) { this.hotel = hotel; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) {
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        this.dateFin = dateFin;
    }
    public double getPrix() { return prix; }
    public void setPrix(double prix) {
        if (prix < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.prix = prix;
    }
    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) {
        if (placesDisponibles < 0) throw new IllegalArgumentException("Available places cannot be negative");
        this.placesDisponibles = placesDisponibles;
    }

    /**
     * Returns a convenience string combining the city and country.
     * If either value is null, it will be omitted gracefully. An empty
     * string will be returned if both are null.
     *
     * @return a formatted string like "Ville/Pays" or just the non-null part
     */
    public String getVillePays() {
        String v = (ville != null ? ville : "");
        String p = (pays != null ? pays : "");
        if (!v.isEmpty() && !p.isEmpty()) {
            return v + "/" + p;
        } else if (!v.isEmpty()) {
            return v;
        } else {
            return p;
        }
    }

    /**
     * Convenience accessor mirroring {@link #getPlacesDisponibles()} for UI usage.
     *
     * @return the number of available places
     */
    public int getPlacesDispo() {
        return getPlacesDisponibles();
    }

    @Override
    public String toString() {
        return designation + " (" + ville + ", " + pays + ")";
    }
}