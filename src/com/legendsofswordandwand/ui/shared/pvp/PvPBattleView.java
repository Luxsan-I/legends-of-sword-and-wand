package com.legendsofswordandwand.ui.pvp;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.pvp.BattleEvent;
import com.legendsofswordandwand.pvp.BattleEventListener;
import com.legendsofswordandwand.pvp.PvPController;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * The main PvP battle screen.
 *
 * Implements BattleEventListener (Observer pattern) so that it automatically
 * refreshes itself whenever the PvPController fires a battle event.
 */
public class PvPBattleView extends JFrame implements BattleEventListener {

    private final PvPController controller;

    // Current-hero action panel
    private JLabel stateLabel;
    private JLabel currentHeroLabel;
    private JComboBox<Hero> targetCombo;
    private JButton attackButton;
    private JButton defendButton;
    private JButton castButton;
    private JButton waitButton;

    // Party status panels
    private JTextArea partyOneStatus;
    private JTextArea partyTwoStatus;

    // Battle log
    private JTextArea battleLog;

    // Result callback
    private Runnable onBattleOver;

    public PvPBattleView(PvPController controller) {
        this.controller = controller;

        setTitle("PvP Battle: " + controller.getPlayerOneProfile().getUsername()
                + " vs " + controller.getPlayerTwoProfile().getUsername());
        setSize(700, 580);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();

        // Register as Observer — the view will be notified of all battle events
        controller.addBattleEventListener(this);

        // Initial render
        refreshAll();
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top: state + current hero
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        stateLabel = new JLabel("", SwingConstants.CENTER);
        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        currentHeroLabel = new JLabel("", SwingConstants.CENTER);
        topPanel.add(stateLabel);
        topPanel.add(currentHeroLabel);
        root.add(topPanel, BorderLayout.NORTH);

        // Centre: party statuses side by side
        JPanel centrePanel = new JPanel(new GridLayout(1, 2, 8, 0));
        partyOneStatus = makeStatusArea(controller.getPlayerOneProfile().getUsername());
        partyTwoStatus = makeStatusArea(controller.getPlayerTwoProfile().getUsername());
        centrePanel.add(new JScrollPane(partyOneStatus));
        centrePanel.add(new JScrollPane(partyTwoStatus));
        root.add(centrePanel, BorderLayout.CENTER);

        // Right: action controls
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.gridy = 0;

        actionPanel.add(new JLabel("Target:"), gbc);
        gbc.gridy++;
        targetCombo = new JComboBox<>();
        actionPanel.add(targetCombo, gbc);
        gbc.gridy++;

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

        // Wire up action buttons
        attackButton.addActionListener(e -> {
            Hero actor = controller.getBattleSystem().getCurrentHero();
            Hero target = (Hero) targetCombo.getSelectedItem();
            controller.attack(actor, target);
        });

        defendButton.addActionListener(e -> {
            Hero actor = controller.getBattleSystem().getCurrentHero();
            controller.defend(actor);
        });

        castButton.addActionListener(e -> {
            Hero actor = controller.getBattleSystem().getCurrentHero();
            Hero target = (Hero) targetCombo.getSelectedItem();
            controller.castAbility(actor, target);
        });

        waitButton.addActionListener(e -> {
            Hero actor = controller.getBattleSystem().getCurrentHero();
            controller.waitTurn(actor);
        });
    }

    private JTextArea makeStatusArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBorder(BorderFactory.createTitledBorder(title));
        return area;
    }

    // -------------------------------------------------------------------------
    // Observer callback
    // -------------------------------------------------------------------------

    /**
     * Invoked by PvPController whenever a BattleEvent fires.
     * Updates the UI on the Swing event dispatch thread.
     */
    @Override
    public void onBattleEvent(BattleEvent event) {
        SwingUtilities.invokeLater(() -> {
            appendLog(event.getMessage());
            refreshAll();

            if (event.getType() == BattleEvent.Type.BATTLE_OVER) {
                disableActions();
                if (onBattleOver != null) {
                    onBattleOver.run();
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Refresh helpers
    // -------------------------------------------------------------------------

    private void refreshAll() {
        refreshStateLabels();
        refreshPartyStatus(partyOneStatus, controller.getBattleSystem().getPartyOne());
        refreshPartyStatus(partyTwoStatus, controller.getBattleSystem().getPartyTwo());
        refreshTargetCombo();
        refreshActionButtons();
    }

    private void refreshStateLabels() {
        stateLabel.setText(controller.getCurrentState().getStateLabel());

        Hero current = controller.getBattleSystem().getCurrentHero();
        if (current != null) {
            currentHeroLabel.setText("Acting: " + current.getName()
                    + " (HP " + current.getCurrentHp() + "/" + current.getMaxHp()
                    + "  MP " + current.getCurrentMana() + "/" + current.getMaxMana() + ")");
        } else {
            currentHeroLabel.setText("");
        }
    }

    private void refreshPartyStatus(JTextArea area, Party party) {
        StringBuilder sb = new StringBuilder();
        for (Hero hero : party.getHeroes()) {
            sb.append(hero.getName())
              .append(" [").append(hero.getHeroClass()).append("]");
            if (!hero.isAlive()) {
                sb.append(" — DEFEATED");
            } else {
                sb.append("  HP:").append(hero.getCurrentHp()).append("/").append(hero.getMaxHp())
                  .append("  MP:").append(hero.getCurrentMana()).append("/").append(hero.getMaxMana());
                if (hero.isDefending()) {
                    sb.append(" [DEF]");
                }
            }
            sb.append("\n");
        }
        area.setText(sb.toString());
    }

    private void refreshTargetCombo() {
        targetCombo.removeAllItems();
        Hero current = controller.getBattleSystem().getCurrentHero();
        if (current == null) return;

        // Populate enemy heroes as valid targets
        Party enemyParty = isInPartyOne(current)
                ? controller.getBattleSystem().getPartyTwo()
                : controller.getBattleSystem().getPartyOne();

        List<Hero> aliveEnemies = enemyParty.getAliveHeroes();
        for (Hero enemy : aliveEnemies) {
            targetCombo.addItem(enemy);
        }
    }

    private void refreshActionButtons() {
        boolean active = !controller.isBattleOver();
        Hero current = controller.getBattleSystem().getCurrentHero();

        attackButton.setEnabled(active && current != null);
        defendButton.setEnabled(active && current != null);
        waitButton.setEnabled(active && current != null);
        castButton.setEnabled(active && current != null
                && current.getCurrentMana() >= 20);
    }

    private void disableActions() {
        attackButton.setEnabled(false);
        defendButton.setEnabled(false);
        castButton.setEnabled(false);
        waitButton.setEnabled(false);
        targetCombo.setEnabled(false);
    }

    private boolean isInPartyOne(Hero hero) {
        return controller.getBattleSystem().getPartyOne().getHeroes().contains(hero);
    }

    private void appendLog(String message) {
        if (message != null && !message.isEmpty()) {
            battleLog.append(message + "\n");
            battleLog.setCaretPosition(battleLog.getDocument().getLength());
        }
    }

    /** Sets a callback to run when the battle ends (e.g. open results screen). */
    public void setOnBattleOver(Runnable callback) {
        this.onBattleOver = callback;
    }
}
