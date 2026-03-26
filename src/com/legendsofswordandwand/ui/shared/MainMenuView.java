package com.legendsofswordandwand.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main menu screen shown after login.
 * Includes buttons for PvP, profile, rankings, and exit.
 */
public class MainMenuView extends JFrame {

    private JButton pvpButton;
    private JButton profileButton;
    private JButton rankingsButton;
    private JButton exitButton;

    public MainMenuView() {
        setTitle("Legends of Sword and Wand - Main Menu");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        pvpButton      = new JButton("Play PvP");
        profileButton  = new JButton("View Profile");
        rankingsButton = new JButton("View Rankings");
        exitButton     = new JButton("Exit");

        panel.add(pvpButton);
        panel.add(profileButton);
        panel.add(rankingsButton);
        panel.add(exitButton);

        add(panel);
    }

    public void addPvpListener(ActionListener listener) {
        pvpButton.addActionListener(listener);
    }

    public void addProfileListener(ActionListener listener) {
        profileButton.addActionListener(listener);
    }

    public void addRankingsListener(ActionListener listener) {
        rankingsButton.addActionListener(listener);
    }

    public void addExitListener(ActionListener listener) {
        exitButton.addActionListener(listener);
    }
}
