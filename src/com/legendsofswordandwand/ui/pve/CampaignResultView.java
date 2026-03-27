package com.legendsofswordandwand.ui.pve;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.persistence.RankingRepository;
import com.legendsofswordandwand.pve.CampaignService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Shown at the end of a completed 30-room PvE campaign.
 *
 * Displays the final score, offers the player the option to save their party
 * for PvP (up to 5 saved parties, with a replace prompt if at the limit),
 * and then returns to the main menu.
 */
public class CampaignResultView extends JFrame {

    private static final int MAX_SAVED_PARTIES = 5;

    private final CampaignService    campaignService;
    private final CampaignProgress   progress;
    private final Profile            profile;
    private final CampaignRepository campaignRepository;
    private final Runnable           onReturnToMenu;

    public CampaignResultView(CampaignService campaignService,
                              CampaignProgress progress,
                              Profile profile,
                              CampaignRepository campaignRepository,
                              Runnable onReturnToMenu) {
        this.campaignService    = campaignService;
        this.progress           = progress;
        this.profile            = profile;
        this.campaignRepository = campaignRepository;
        this.onReturnToMenu     = onReturnToMenu;

        setTitle("Campaign Complete!");
        setSize(500, 420);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top: title
        JLabel titleLabel = new JLabel("Campaign Complete!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        root.add(titleLabel, BorderLayout.NORTH);

        // Centre: score breakdown + party summary
        JPanel centrePanel = new JPanel(new BorderLayout(8, 8));

        int finalScore = campaignService.calculateFinalScore(progress);
        profile.addScore(finalScore);

        JTextArea scoreArea = new JTextArea();
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scoreArea.setText(buildScoreSummary(finalScore));
        scoreArea.setBorder(BorderFactory.createTitledBorder("Results"));
        centrePanel.add(new JScrollPane(scoreArea), BorderLayout.CENTER);

        JTextArea partyArea = new JTextArea();
        partyArea.setEditable(false);
        partyArea.setText(buildPartySummary());
        partyArea.setBorder(BorderFactory.createTitledBorder("Final Party"));
        centrePanel.add(new JScrollPane(partyArea), BorderLayout.SOUTH);

        root.add(centrePanel, BorderLayout.CENTER);

        // Bottom: save party + return buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));

        JButton savePartyButton = new JButton("Save Party for PvP");
        savePartyButton.addActionListener(e -> handleSaveParty(savePartyButton));

        JButton returnButton = new JButton("Return to Main Menu");
        returnButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        returnButton.addActionListener(e -> {
            // Delete the completed campaign so it doesn't appear as "incomplete"
            campaignRepository.deleteCampaign(profile);
            dispose();
            if (onReturnToMenu != null) onReturnToMenu.run();
        });

        buttonPanel.add(savePartyButton);
        buttonPanel.add(returnButton);
        root.add(buttonPanel, BorderLayout.SOUTH);

        add(root);
    }

    // -------------------------------------------------------------------------
    // Party save handling
    // -------------------------------------------------------------------------

    private void handleSaveParty(JButton savePartyButton) {
        Party party = progress.getCurrentParty();
        List<Party> savedParties = profile.getSavedParties();

        if (savedParties.size() < MAX_SAVED_PARTIES) {
            boolean saved = profile.saveParty(party);
            if (saved) {
                JOptionPane.showMessageDialog(this,
                        "Party saved! You now have " + profile.getSavedParties().size()
                                + "/" + MAX_SAVED_PARTIES + " saved parties.",
                        "Party Saved", JOptionPane.INFORMATION_MESSAGE);
                savePartyButton.setEnabled(false);
            }
        } else {
            // At limit — prompt to replace one
            String[] options = new String[MAX_SAVED_PARTIES];
            for (int i = 0; i < savedParties.size(); i++) {
                options[i] = (i + 1) + ": " + savedParties.get(i).getPartyName();
            }

            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "You already have " + MAX_SAVED_PARTIES + " saved parties.\n"
                            + "Choose one to replace:",
                    "Replace Party",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice != null) {
                int index = Integer.parseInt(choice.substring(0, 1)) - 1;
                boolean replaced = profile.replaceParty(index, party);
                if (replaced) {
                    JOptionPane.showMessageDialog(this,
                            "Party slot " + (index + 1) + " replaced.",
                            "Party Replaced", JOptionPane.INFORMATION_MESSAGE);
                    savePartyButton.setEnabled(false);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Text builders
    // -------------------------------------------------------------------------

    private String buildScoreSummary(int finalScore) {
        StringBuilder sb = new StringBuilder();
        sb.append("Final Score:  ").append(finalScore).append("\n\n");
        sb.append("Rooms completed:  30 / 30\n");
        sb.append("Heroes in party:  ")
                .append(progress.getCurrentParty().getHeroes().size()).append("\n");

        sb.append("\nHero levels:\n");
        for (var hero : progress.getCurrentParty().getHeroes()) {
            sb.append("  ").append(hero.getName())
                    .append(" [").append(hero.getHeroClass()).append("]")
                    .append("  Lvl ").append(hero.getLevel()).append("\n");
        }
        return sb.toString();
    }

    private String buildPartySummary() {
        StringBuilder sb = new StringBuilder();
        for (var hero : progress.getCurrentParty().getHeroes()) {
            sb.append(hero.getName())
                    .append(" [").append(hero.getHeroClass()).append("]")
                    .append("  Lvl ").append(hero.getLevel())
                    .append("  HP:").append(hero.getMaxHp())
                    .append("  ATK:").append(hero.getAttack())
                    .append("  DEF:").append(hero.getDefense())
                    .append("\n");
        }
        return sb.toString();
    }
}