package com.agencevoyage.ui;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        setTitle("Gestion Agence de Voyage");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridLayout(2, 2, 16, 16));
        JButton clientsBtn = new JButton("Gestion des Clients");
        clientsBtn.addActionListener(e -> new ClientList().setVisible(true));

        JButton prestationsBtn = new JButton("Gestion des Prestations");
        prestationsBtn.addActionListener(e -> new PrestationList().setVisible(true));

        JButton reservationsBtn = new JButton("Réservations");
        reservationsBtn.addActionListener(e -> new ReservationList().setVisible(true));

        JButton quitterBtn = new JButton("Quitter");
        quitterBtn.addActionListener(e -> System.exit(0));

        add(clientsBtn);
        add(prestationsBtn);
        add(reservationsBtn);
        add(quitterBtn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIUtil.scaleUI(2.0);
            new Main().setVisible(true);
        });
    }
}
