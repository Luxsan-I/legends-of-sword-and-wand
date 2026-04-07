package com.legendsofswordandwand.ui.pve;

import com.legendsofswordandwand.battle.BattleSystem;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.pve.CampaignService;
import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Profile;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * PvE battle screen.
 *
 * After each player action, an enemy AI loop runs automatically until it is
 * the player's turn again or the battle ends. Enemy AI always attacks a
 * random alive hero from the player's party.
 */
public class BattleView extends JFrame {

    private final BattleSystem     battleSystem;
    private final CampaignService  campaignService;
    private final CampaignProgress progress;
    private final Profile          profile;
    private final Party            enemyParty;
    private final Random           random = new Random();

    // Top labels
    private JLabel roomLabel;
    private JLabel currentHeroLabel;

    // Party status panels
    private JTextArea playerPartyStatus;
    private JTextArea enemyPartyStatus;

    // Action controls
    private JComboBox<Hero> targetCombo;
    private JButton attackButton;
    private JButton defendButton;
    private JButton castButton;
    private JButton waitButton;

    // Battle log
    private JTextArea battleLog;

    // Navigation callbacks
    private Runnable onVictory;
    private Runnable onDefeat;

    public BattleView(BattleSystem battleSystem,
                      CampaignService campaignService,
                      CampaignProgress progress,
                      Profile profile,
                      Party enemyParty) {
        this.battleSystem    = battleSystem;
        this.campaignService = campaignService;
        this.progress        = progress;
        this.profile         = profile;
        this.enemyParty      = enemyParty;

        setTitle("Battle — Room " + progress.getCurrentStage());
        setSize(700, 580);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        refreshAll();
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top: room info + acting hero
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        roomLabel = new JLabel("", SwingConstants.CENTER);
        roomLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        currentHeroLabel = new JLabel("", SwingConstants.CENTER);
        topPanel.add(roomLabel);
        topPanel.add(currentHeroLabel);
        root.add(topPanel, BorderLayout.NORTH);

        // Centre: player party vs enemy party
        JPanel centrePanel = new JPanel(new GridLayout(1, 2, 8, 0));
        playerPartyStatus = makeStatusArea("Your Party");
        enemyPartyStatus  = makeStatusArea("Enemies");
        centrePanel.add(new JScrollPane(playerPartyStatus));
        centrePanel.add(new JScrollPane(enemyPartyStatus));
        root.add(centrePanel, BorderLayout.CENTER);

        // Right: action controls
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.gridy = 0;

        actionPanel.add(new JLabel("Target:"), gbc); gbc.gridy++;
        targetCombo = new JComboBox<>();
        actionPanel.add(targetCombo, gbc); gbc.gridy++;

        attackButton = new JButton("Attack");
        actionPanel.add(attackButton, gbc); gbc.gridy++;

        castButton = new JButton("Cast Ability");
        actionPanel.add(castButton, gbc); gbc.gridy++;

        defendButton = new JButton("Defend");
        actionPanel.add(defendButton, gbc); gbc.gridy++;

        waitButton = new JButton("Wait");
        actionPanel.add(waitButton, gbc);

        root.add(actionPanel, BorderLayout.EAST);

        // Bottom: battle log
        battleLog = new JTextArea(6, 50);
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Battle Log"));
        root.add(logScroll, BorderLayout.SOUTH);

        add(root);

        // Wire up player action buttons
        attackButton.addActionListener(e -> {
            Hero actor  = battleSystem.getCurrentHero();
            Hero target = (Hero) targetCombo.getSelectedItem();
            boolean ok  = battleSystem.performAttack(actor, target);
            if (ok) {
                appendLog(actor.getName() + " attacks " + target.getName()
                        + " for " + Math.max(1, actor.getAttack() - target.getEffectiveDefense()) + " damage!");
                afterPlayerAction();
            }
        });

        defendButton.addActionListener(e -> {
            Hero actor = battleSystem.getCurrentHero();
            boolean ok = battleSystem.performDefend(actor);
            if (ok) {
                appendLog(actor.getName() + " defends! (+10 HP, +5 MP)");
                afterPlayerAction();
            }
        });

        castButton.addActionListener(e -> {
            Hero actor  = battleSystem.getCurrentHero();
            Hero target = (Hero) targetCombo.getSelectedItem();
            boolean ok  = battleSystem.performCastAbility(actor, target);
            if (ok) {
                appendLog(actor.getName() + " casts an ability on " + target.getName() + "!");
                afterPlayerAction();
            } else {
                appendLog("Not enough mana!");
            }
        });

        waitButton.addActionListener(e -> {
            Hero actor = battleSystem.getCurrentHero();
            boolean ok = battleSystem.performWait(actor);
            if (ok) {
                appendLog(actor.getName() + " waits.");
                afterPlayerAction();
            }
        });
    }

    private JTextArea makeStatusArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBorder(BorderFactory.createTitledBorder(title));
        return area;
    }

    // -------------------------------------------------------------------------
    // Enemy AI
    // -------------------------------------------------------------------------

    /**
     * Called after every player action.
     * Checks if the battle is over, then runs the enemy AI for all consecutive
     * enemy turns until it is the player's turn again or the battle ends.
     */
    private void afterPlayerAction() {
        if (checkBattleOver()) return;
        runEnemyTurns();
        refreshAll();
    }

    /**
     * Executes enemy turns automatically until a player hero is next or the
     * battle ends. Enemy AI always attacks a random alive player hero.
     */
    private void runEnemyTurns() {
        while (!battleSystem.isBattleOver()) {
            Hero current = battleSystem.getCurrentHero();
            if (current == null) break;

            // If it's the player's turn, stop and let the player act
            if (battleSystem.getPartyOne().getHeroes().contains(current)) break;

            // Enemy turn — pick a random alive player hero to attack
            List<Hero> targets = battleSystem.getPartyOne().getAliveHeroes();
            if (targets.isEmpty()) break;

            Hero target = targets.get(random.nextInt(targets.size()));
            int damage  = Math.max(1, current.getAttack() - target.getEffectiveDefense());
            battleSystem.performAttack(current, target);
            appendLog(current.getName() + " attacks " + target.getName()
                    + " for " + damage + " damage!");

            if (checkBattleOver()) return;
        }
    }

    // -------------------------------------------------------------------------
    // Refresh helpers
    // -------------------------------------------------------------------------

    private void refreshAll() {
        refreshTopLabels();
        refreshPartyStatus(playerPartyStatus, battleSystem.getPartyOne());
        refreshPartyStatus(enemyPartyStatus,  battleSystem.getPartyTwo());
        refreshTargetCombo();
        refreshActionButtons();
    }

    private void refreshTopLabels() {
        roomLabel.setText("Room " + progress.getCurrentStage() + " / 30  —  Battle");
        Hero current = battleSystem.getCurrentHero();
        if (current != null) {
            currentHeroLabel.setText("Acting: " + current.getName()
                    + "  HP " + current.getCurrentHp() + "/" + current.getMaxHp()
                    + "  MP " + current.getCurrentMana() + "/" + current.getMaxMana());
        } else {
            currentHeroLabel.setText("");
        }
    }

    private void refreshPartyStatus(JTextArea area, Party party) {
        StringBuilder sb = new StringBuilder();
        for (Hero hero : party.getHeroes()) {
            sb.append(hero.getName()).append(" [").append(hero.getHeroClass()).append("]");
            if (!hero.isAlive()) {
                sb.append(" — DEFEATED");
            } else {
                sb.append("  HP:").append(hero.getCurrentHp()).append("/").append(hero.getMaxHp())
                        .append("  MP:").append(hero.getCurrentMana()).append("/").append(hero.getMaxMana());
                if (hero.isDefending()) sb.append(" [DEF]");
            }
            sb.append("\n");
        }
        area.setText(sb.toString());
    }

    private void refreshTargetCombo() {
        targetCombo.removeAllItems();
        Hero current = battleSystem.getCurrentHero();
        if (current == null) return;

        boolean actorInPartyOne = battleSystem.getPartyOne().getHeroes().contains(current);
        Party enemySide = actorInPartyOne
                ? battleSystem.getPartyTwo()
                : battleSystem.getPartyOne();

        for (Hero enemy : enemySide.getAliveHeroes()) {
            targetCombo.addItem(enemy);
        }
    }

    private void refreshActionButtons() {
        boolean active = !battleSystem.isBattleOver();
        Hero current   = battleSystem.getCurrentHero();
        boolean myTurn = current != null
                && battleSystem.getPartyOne().getHeroes().contains(current);

        attackButton.setEnabled(active && myTurn);
        defendButton.setEnabled(active && myTurn);
        waitButton.setEnabled(active && myTurn);
        castButton.setEnabled(active && myTurn && current.getCurrentMana() >= 20);
        targetCombo.setEnabled(active && myTurn);
    }

    private void disableActions() {
        attackButton.setEnabled(false);
        defendButton.setEnabled(false);
        castButton.setEnabled(false);
        waitButton.setEnabled(false);
        targetCombo.setEnabled(false);
    }

    // -------------------------------------------------------------------------
    // Battle-over handling
    // -------------------------------------------------------------------------

    /**
     * Checks if the battle is over and handles the outcome.
     * @return true if the battle has ended
     */
    private boolean checkBattleOver() {
        if (!battleSystem.isBattleOver()) return false;

        disableActions();
        boolean playerWon = !battleSystem.getPartyOne().isDefeated();

        SwingUtilities.invokeLater(() -> {
            if (playerWon) {
                int totalExp  = enemyParty.getHeroes().stream()
                        .mapToInt(h -> 50 * h.getLevel()).sum();
                int totalGold = enemyParty.getHeroes().stream()
                        .mapToInt(h -> 75 * h.getLevel()).sum();

                campaignService.onBattleVictory(progress, enemyParty);
                appendLog("Victory! Experience and gold awarded.");
                showLevelUpMessages();

                BattleResultView result = new BattleResultView(
                        true,
                        battleSystem.getPartyOne(),
                        battleSystem.getPartyTwo(),
                        totalGold, totalExp, 0,
                        () -> { if (onVictory != null) onVictory.run(); });
                result.setVisible(true);

            } else {
                int goldBefore = campaignService.getInnService().getGold();
                campaignService.onBattleDefeat(progress);
                int goldLost = goldBefore - campaignService.getInnService().getGold();

                appendLog("Defeated... Gold and experience lost. Returning to inn.");

                BattleResultView result = new BattleResultView(
                        false,
                        battleSystem.getPartyOne(),
                        battleSystem.getPartyTwo(),
                        0, 0, goldLost,
                        () -> { if (onDefeat != null) onDefeat.run(); });
                result.setVisible(true);
            }
        });

        return true;
    }

    private void showLevelUpMessages() {
        for (Hero hero : progress.getCurrentParty().getHeroes()) {
            appendLog(hero.getName() + " is now level " + hero.getLevel() + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void appendLog(String message) {
        if (message != null && !message.isEmpty()) {
            battleLog.append(message + "\n");
            battleLog.setCaretPosition(battleLog.getDocument().getLength());
        }
    }

    public void setOnVictory(Runnable callback) {this.onVictory = callback;}
    public void setOnDefeat(Runnable callback)  {this.onDefeat  = callback;}
}