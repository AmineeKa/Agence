package com.agencevoyage.ui;

import com.agencevoyage.model.Client;
import com.agencevoyage.model.Prestation;
import com.agencevoyage.service.ClientService;
import com.agencevoyage.service.PrestationService;
import com.agencevoyage.service.ReservationService;

import java.time.LocalDate;
import java.util.Scanner;

public class MenuConsole {
    private ClientService clientService = new ClientService();
    private PrestationService prestationService = new PrestationService();
    private ReservationService reservationService = new ReservationService();
    private Scanner scanner = new Scanner(System.in);

    public void afficherMenuPrincipal() {
        int choix = -1;

        do {
            System.out.println("\n====== MENU PRINCIPAL ======");
            System.out.println("1. Gérer les clients");
            System.out.println("2. Gérer les prestations");
            System.out.println("3. Gérer les réservations");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");
            choix = scanner.nextInt();
            scanner.nextLine(); // Consomme le retour chariot

            switch (choix) {
                case 1:
                    menuClients();
                    break;
                case 2:
                    menuPrestations();
                    break;
                case 3:
                    menuReservations();
                    break;
                case 0:
                    System.out.println("Au revoir !");
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        } while (choix != 0);
    }

    private void menuClients() {
        System.out.println("\n--- GESTION DES CLIENTS ---");
        // Ici tu pourras ajouter des options
    }

    private void menuPrestations() {
        System.out.println("\n--- GESTION DES PRESTATIONS ---");
        // Ici tu pourras ajouter des options
    }

    private void menuReservations() {
        System.out.println("\n--- GESTION DES RESERVATIONS ---");
        // Ici tu pourras ajouter des options
    }
}