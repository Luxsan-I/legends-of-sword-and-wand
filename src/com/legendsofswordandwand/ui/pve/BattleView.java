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

/**
 * PvE battle screen.
 *
 * Wraps a BattleSystem directly — no observer pattern needed since PvE
 * battle is single-player and synchronous. Buttons call BattleSystem
 * perform* methods and the view refreshes itself after each action.
 */
public class BattleView extends JFrame {

    private final BattleSystem battleSystem;
    private final CampaignService campaignService;
    private final CampaignProgress progress;
    private final Profile profile;
    private final Party enemyParty;

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

        // Wire up buttons
        attackButton.addActionListener(e -> {
            Hero actor  = battleSystem.getCurrentHero();
            Hero target = (Hero) targetCombo.getSelectedItem();
            boolean ok  = battleSystem.performAttack(actor, target);
            if (ok) {
                appendLog(actor.getName() + " attacks " + target.getName() + "!");
                checkBattleOver();
                refreshAll();
            }
        });

        defendButton.addActionListener(e -> {
            Hero actor = battleSystem.getCurrentHero();
            boolean ok = battleSystem.performDefend(actor);
            if (ok) {
                appendLog(actor.getName() + " defends! (+10 HP, +5 MP)");
                checkBattleOver();
                refreshAll();
            }
        });

        castButton.addActionListener(e -> {
            Hero actor  = battleSystem.getCurrentHero();
            Hero target = (Hero) targetCombo.getSelectedItem();
            boolean ok  = battleSystem.performCastAbility(actor, target);
            if (ok) {
                appendLog(actor.getName() + " casts an ability on " + target.getName() + "!");
                checkBattleOver();
                refreshAll();
            } else {
                appendLog("Not enough mana!");
            }
        });

        waitButton.addActionListener(e -> {
            Hero actor = battleSystem.getCurrentHero();
            boolean ok = battleSystem.performWait(actor);
            if (ok) {
                appendLog(actor.getName() + " waits.");
                refreshAll();
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

        // Player heroes act against enemy party and vice versa
        boolean actorInPartyOne = battleSystem.getPartyOne().getHeroes().contains(current);
        Party enemySide = actorInPartyOne
                ? battleSystem.getPartyTwo()
                : battleSystem.getPartyOne();

        for (Hero enemy : enemySide.getAliveHeroes()) {
            targetCombo.addItem(enemy);
        }
    }

    private void refreshActionButtons() {
        boolean active  = !battleSystem.isBattleOver();
        Hero current    = battleSystem.getCurrentHero();
        boolean myTurn  = current != null
                && battleSystem.getPartyOne().getHeroes().contains(current);

        attackButton.setEnabled(active && myTurn);
        defendButton.setEnabled(active && myTurn);
        waitButton.setEnabled(active && myTurn);
        castButton.setEnabled(active && myTurn
                && current.getCurrentMana() >= 20);
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

    private void checkBattleOver() {
        if (!battleSystem.isBattleOver()) return;

        disableActions();

        boolean playerWon = !battleSystem.getPartyOne().isDefeated();

        SwingUtilities.invokeLater(() -> {
            if (playerWon) {
                campaignService.onBattleVictory(progress, enemyParty);
                appendLog("Victory! Experience and gold awarded.");
                showLevelUpMessages();
                if (onVictory != null) onVictory.run();
            } else {
                campaignService.onBattleDefeat(progress);
                appendLog("Defeated... Gold and experience lost. Returning to inn.");
                if (onDefeat != null) onDefeat.run();
            }
        });
    }

    private void showLevelUpMessages() {
        for (Hero hero : progress.getCurrentParty().getHeroes()) {
            // Level-up messages are logged; levelUpIfReady was already called
            // inside onBattleVictory via CampaignProgress
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