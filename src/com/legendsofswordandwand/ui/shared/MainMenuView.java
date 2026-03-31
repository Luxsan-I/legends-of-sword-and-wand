package com.legendsofswordandwand.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main menu screen shown after login.
 * Includes buttons for PvE (new + continue), PvP, profile, rankings, and exit.
 */
public class MainMenuView extends JFrame {

    private JButton newPveButton;
    private JButton continuePveButton;
    private JButton pvpButton;
    private JButton profileButton;
    private JButton rankingsButton;
    private JButton exitButton;

    public MainMenuView() {
        setTitle("Legends of Sword and Wand - Main Menu");
        setSize(400, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        newPveButton      = new JButton("New PvE Campaign");
        continuePveButton = new JButton("Continue PvE Campaign");
        pvpButton         = new JButton("Play PvP");
        profileButton     = new JButton("View Profile");
        rankingsButton    = new JButton("View Rankings");
        exitButton        = new JButton("Exit");

        panel.add(newPveButton);
        panel.add(continuePveButton);
        panel.add(pvpButton);
        panel.add(profileButton);
        panel.add(rankingsButton);
        panel.add(exitButton);

        add(panel);
    }

    public void addNewPveListener(ActionListener listener) {
        newPveButton.addActionListener(listener);
    }

    public void addContinuePveListener(ActionListener listener) {
        continuePveButton.addActionListener(listener);
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

    /** Enables or disables the Continue PvE button based on whether a save exists. */
    public void setContinuePveEnabled(boolean enabled) {
        continuePveButton.setEnabled(enabled);
    }
}