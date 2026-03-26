package com.legendsofswordandwand.ui.pvp;

import com.legendsofswordandwand.model.BattleResult;
import com.legendsofswordandwand.model.Profile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Displayed after a PvP match concludes.
 * Shows the winner, loser, and updated win/loss record.
 */
public class PvPResultView extends JFrame {

    private JButton returnToMenuButton;

    public PvPResultView(BattleResult result, Profile playerOneProfile, Profile playerTwoProfile) {
        setTitle("PvP Result");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Determine winner profile
        boolean partyOneWon = result.getWinningParty().getPartyName()
                .equals(playerOneProfile.getUsername() + "'s Party")
                || !result.getWinningParty().getPartyName()
                    .equals(playerTwoProfile.getUsername() + "'s Party");

        Profile winner = result.getWinningParty().getHeroes().stream().anyMatch(h ->
                playerOneProfile.getUsername() != null) ? resolveWinner(result, playerOneProfile, playerTwoProfile) : playerTwoProfile;

        // Title
        JLabel titleLabel = new JLabel("\uD83C\uDFC6  " + winner.getUsername() + " wins!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Stats
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setOpaque(false);
        statsArea.setText(
                "Turns played: " + result.getTurnsPlayed() + "\n\n"
                + playerOneProfile.getUsername() + "\n"
                + "  Wins: " + playerOneProfile.getPvpWins()
                + "  Losses: " + playerOneProfile.getPvpLosses() + "\n\n"
                + playerTwoProfile.getUsername() + "\n"
                + "  Wins: " + playerTwoProfile.getPvpWins()
                + "  Losses: " + playerTwoProfile.getPvpLosses()
        );
        panel.add(statsArea, BorderLayout.CENTER);

        returnToMenuButton = new JButton("Return to Main Menu");
        panel.add(returnToMenuButton, BorderLayout.SOUTH);

        add(panel);
    }

    /**
     * Resolves which Profile is the winner based on the BattleResult.
     * The winning party belongs to whoever has fewer losses after the update.
     */
    private Profile resolveWinner(BattleResult result, Profile p1, Profile p2) {
        // After recordPvPResult, the winner has more wins
        if (p1.getPvpWins() >= p2.getPvpWins()) {
            return p1;
        }
        return p2;
    }

    public void addReturnToMenuListener(ActionListener listener) {
        returnToMenuButton.addActionListener(listener);
    }
}
