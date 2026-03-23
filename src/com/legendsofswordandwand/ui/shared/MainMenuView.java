package com.legendsofswordandwand.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuView extends JFrame {
    private JButton profileButton;
    private JButton rankingsButton;
    private JButton exitButton;

    public MainMenuView() {
        setTitle("Legends of Sword and Wand - Main Menu");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));

        profileButton = new JButton("View Profile");
        rankingsButton = new JButton("View Rankings");
        exitButton = new JButton("Exit");

        panel.add(profileButton);
        panel.add(rankingsButton);
        panel.add(exitButton);

        add(panel);
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