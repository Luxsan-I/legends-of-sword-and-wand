package com.legendsofswordandwand.ui.pve;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;

import javax.swing.*;
import java.awt.*;

/**
 * End screen shown after every PvE battle, win or loss.
 * Displays the outcome, party status, rewards or penalties, then
 * lets the player continue via a single button.
 */
public class BattleResultView extends JFrame {

    private final Runnable onContinue;

    public BattleResultView(boolean playerWon,
                            Party playerParty,
                            Party enemyParty,
                            int goldGained,
                            int expGained,
                            int goldLost,
                            Runnable onContinue) {
        this.onContinue = onContinue;

        setTitle(playerWon ? "Victory!" : "Defeated...");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI(playerWon, playerParty, enemyParty, goldGained, expGained, goldLost);
    }

    private void buildUI(boolean playerWon,
                         Party playerParty,
                         Party enemyParty,
                         int goldGained,
                         int expGained,
                         int goldLost) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top: outcome banner
        JLabel outcomeLabel = new JLabel(
                playerWon ? "Victory!" : "Defeated...", SwingConstants.CENTER);
        outcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        outcomeLabel.setForeground(playerWon
                ? new Color(0, 140, 0)
                : new Color(180, 0, 0));
        root.add(outcomeLabel, BorderLayout.NORTH);

        // Centre: summary text
        JTextArea summary = new JTextArea();
        summary.setEditable(false);
        summary.setFont(new Font("Monospaced", Font.PLAIN, 13));
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setText(buildSummary(playerWon, playerParty, enemyParty,
                goldGained, expGained, goldLost));
        summary.setBorder(BorderFactory.createTitledBorder(
                playerWon ? "Battle Summary" : "Defeat Summary"));
        root.add(new JScrollPane(summary), BorderLayout.CENTER);

        // Bottom: continue button
        String buttonText = playerWon ? "Continue" : "Return to Inn";
        JButton continueButton = new JButton(buttonText);
        continueButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        continueButton.addActionListener(e -> {
            dispose();
            if (onContinue != null) onContinue.run();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(continueButton);
        root.add(buttonPanel, BorderLayout.SOUTH);

        add(root);
    }

    private String buildSummary(boolean playerWon,
                                 Party playerParty,
                                 Party enemyParty,
                                 int goldGained,
                                 int expGained,
                                 int goldLost) {
        StringBuilder sb = new StringBuilder();

        if (playerWon) {
            sb.append("You defeated the enemy party!\n\n");
            sb.append("Rewards:\n");
            sb.append("  Gold gained:  +").append(goldGained).append("g\n");
            sb.append("  Exp gained:   +").append(expGained).append(" (split among survivors)\n\n");
        } else {
            sb.append("Your party was defeated.\n\n");
            sb.append("Penalties:\n");
            sb.append("  Gold lost:    -").append(goldLost).append("g\n");
            sb.append("  Exp lost:     30% of current level exp\n\n");
        }

        sb.append("Your party:\n");
        for (Hero hero : playerParty.getHeroes()) {
            sb.append("  ").append(hero.getName())
              .append(" [").append(hero.getHeroClass()).append("]")
              .append("  Lvl ").append(hero.getLevel());
            if (!hero.isAlive()) {
                sb.append("  — DEFEATED");
            } else {
                sb.append("  HP:").append(hero.getCurrentHp())
                  .append("/").append(hero.getMaxHp());
            }
            sb.append("\n");
        }

        sb.append("\nEnemies:\n");
        for (Hero enemy : enemyParty.getHeroes()) {
            sb.append("  ").append(enemy.getName())
              .append(" [").append(enemy.getHeroClass()).append("]");
            if (!enemy.isAlive()) {
                sb.append("  — DEFEATED");
            } else {
                sb.append("  HP:").append(enemy.getCurrentHp())
                  .append("/").append(enemy.getMaxHp());
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
