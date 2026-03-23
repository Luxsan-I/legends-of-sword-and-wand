package com.legendsofswordandwand.ui.shared;

import com.legendsofswordandwand.model.Profile;

import javax.swing.*;
import java.awt.*;

public class ProfileView extends JFrame {
    private JLabel usernameLabel;
    private JLabel scoreLabel;
    private JLabel winsLabel;
    private JLabel lossesLabel;

    public ProfileView(Profile profile) {
        setTitle("Profile");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        usernameLabel = new JLabel("Username: " + profile.getUsername());
        scoreLabel = new JLabel("Total Score: " + profile.getTotalScore());
        winsLabel = new JLabel("PvP Wins: " + profile.getPvpWins());
        lossesLabel = new JLabel("PvP Losses: " + profile.getPvpLosses());

        panel.add(usernameLabel);
        panel.add(scoreLabel);
        panel.add(winsLabel);
        panel.add(lossesLabel);

        add(panel);
    }
}