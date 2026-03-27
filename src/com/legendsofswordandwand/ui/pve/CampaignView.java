package com.legendsofswordandwand.ui.pve;

import com.legendsofswordandwand.battle.BattleSystem;
import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.CampaignProgress.CampaignStatus;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroFactory;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.pve.CampaignService;
import com.legendsofswordandwand.pve.InnService;
import com.legendsofswordandwand.pve.RoomGenerator.RoomType;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Main PvE campaign screen shown between rooms.
 *
 * Displays party status and inventory, and lets the player advance to the next
 * room (triggering BattleView or InnView) or exit the campaign.
 */
public class CampaignView extends JFrame {

    private final CampaignService    campaignService;
    private final InnService         innService;
    private final CampaignProgress   progress;
    private final Profile            profile;
    private final CampaignRepository campaignRepository;

    // Party status
    private JTextArea partyStatusArea;
    private JTextArea inventoryArea;
    private JLabel    goldLabel;
    private JLabel    roomLabel;

    // Navigation callback (back to main menu)
    private Runnable onReturnToMenu;

    public CampaignView(CampaignService campaignService,
                        InnService innService,
                        CampaignProgress progress,
                        Profile profile,
                        CampaignRepository campaignRepository) {
        this.campaignService    = campaignService;
        this.innService         = innService;
        this.progress           = progress;
        this.profile            = profile;
        this.campaignRepository = campaignRepository;

        setTitle("Campaign — " + profile.getUsername());
        setSize(700, 520);
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

        // Top: room counter + gold
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        roomLabel = new JLabel("", SwingConstants.CENTER);
        roomLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        goldLabel = new JLabel("", SwingConstants.CENTER);
        topPanel.add(roomLabel);
        topPanel.add(goldLabel);
        root.add(topPanel, BorderLayout.NORTH);

        // Centre: party status + inventory
        JPanel centrePanel = new JPanel(new GridLayout(1, 2, 8, 0));

        partyStatusArea = new JTextArea();
        partyStatusArea.setEditable(false);
        JScrollPane partyScroll = new JScrollPane(partyStatusArea);
        partyScroll.setBorder(BorderFactory.createTitledBorder("Party"));
        centrePanel.add(partyScroll);

        inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        JScrollPane inventoryScroll = new JScrollPane(inventoryArea);
        inventoryScroll.setBorder(BorderFactory.createTitledBorder("Inventory"));
        centrePanel.add(inventoryScroll);

        root.add(centrePanel, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));

        JButton nextRoomButton = new JButton("Next Room");
        nextRoomButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        nextRoomButton.addActionListener(e -> handleNextRoom());

        JButton exitButton = new JButton("Exit Campaign");
        exitButton.addActionListener(e -> handleExit());

        buttonPanel.add(nextRoomButton);
        buttonPanel.add(exitButton);
        root.add(buttonPanel, BorderLayout.SOUTH);

        add(root);
    }

    // -------------------------------------------------------------------------
    // Navigation handlers
    // -------------------------------------------------------------------------

    private void handleNextRoom() {
        RoomType roomType = campaignService.nextRoom(progress);

        if (roomType == null) {
            // Campaign completed after 30 rooms
            dispose();
            CampaignResultView resultView = new CampaignResultView(
                    campaignService, progress, profile, campaignRepository,
                    () -> { if (onReturnToMenu != null) onReturnToMenu.run(); }
            );
            resultView.setVisible(true);
            return;
        }

        if (roomType == RoomType.INN) {
            openInnView();
        } else {
            openBattleView();
        }
    }

    private void handleExit() {
        boolean saved = campaignService.exitCampaign(profile, progress);
        if (saved) {
            dispose();
            if (onReturnToMenu != null) onReturnToMenu.run();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cannot exit during a battle.",
                    "Exit Blocked", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openInnView() {
        setVisible(false);
        InnView innView = new InnView(innService, progress, profile, campaignRepository);
        innView.setOnNextRoom(() -> {
            innView.dispose();
            refreshAll();
            setVisible(true);
        });
        innView.setVisible(true);
    }

    private void openBattleView() {
        setVisible(false);

        // Build a random enemy party scaled to the player's cumulative level
        Party enemyParty = generateEnemyParty();
        BattleSystem battleSystem = new BattleSystem(progress.getCurrentParty(), enemyParty);

        BattleView battleView = new BattleView(
                battleSystem, campaignService, progress, profile, enemyParty);

        battleView.setOnVictory(() -> {
            battleView.dispose();
            refreshAll();
            setVisible(true);
        });

        battleView.setOnDefeat(() -> {
            battleView.dispose();
            // After defeat, player is sent back to an inn
            openInnView();
        });

        battleView.setVisible(true);
    }

    /**
     * Generates a random enemy party scaled to the player's cumulative level.
     * Enemy count: 1-5 heroes. Cumulative level scales with party strength.
     */
    private Party generateEnemyParty() {
        HeroFactory factory = new HeroFactory();
        Random random = new Random();

        int playerCumLevel = campaignService.getCumulativeLevel(progress.getCurrentParty());
        int enemyCount = 1 + random.nextInt(Math.min(5, Math.max(1,
                progress.getCurrentParty().getHeroes().size())));

        // Target cumulative level between 75-100% of player's
        int targetCumLevel = Math.max(1, (int)(playerCumLevel * (0.75 + random.nextDouble() * 0.25)));
        int levelPerEnemy  = Math.max(1, targetCumLevel / enemyCount);

        Party enemyParty = new Party("Enemies");
        HeroClass[] classes = HeroClass.values();
        for (int i = 0; i < enemyCount; i++) {
            HeroClass heroClass = classes[random.nextInt(classes.length)];
            Hero enemy = factory.createHero("Enemy " + (i + 1), heroClass);
            if (enemy != null) {
                for (int lvl = 1; lvl < Math.min(levelPerEnemy, 10); lvl++) {
                    enemy.levelUp();
                }
                enemyParty.addHero(enemy);
            }
        }
        return enemyParty;
    }

    // -------------------------------------------------------------------------
    // Refresh helpers
    // -------------------------------------------------------------------------

    private void refreshAll() {
        refreshTopLabels();
        refreshPartyStatus();
        refreshInventory();
    }

    private void refreshTopLabels() {
        int stage = progress.getCurrentStage();
        roomLabel.setText("Room " + stage + " / 30"
                + (progress.isCampaignCompleted() ? "  — COMPLETED" : ""));
        goldLabel.setText("Gold: " + innService.getGold() + "g");
    }

    private void refreshPartyStatus() {
        StringBuilder sb = new StringBuilder();
        for (Hero hero : progress.getCurrentParty().getHeroes()) {
            sb.append(hero.getName())
                    .append(" [").append(hero.getHeroClass()).append("]")
                    .append("  Lvl ").append(hero.getLevel())
                    .append("\n  HP:").append(hero.getCurrentHp()).append("/").append(hero.getMaxHp())
                    .append("  MP:").append(hero.getCurrentMana()).append("/").append(hero.getMaxMana())
                    .append("  ATK:").append(hero.getAttack())
                    .append("  DEF:").append(hero.getDefense())
                    .append("\n  XP: ").append(progress.getExperience(hero))
                    .append(" / ").append(progress.expToNextLevel(hero.getLevel()))
                    .append("\n\n");
        }
        partyStatusArea.setText(sb.toString());
    }

    private void refreshInventory() {
        StringBuilder sb = new StringBuilder();
        var items = progress.getCurrentParty().getInventory().getItems();
        if (items.isEmpty()) {
            sb.append("No items.");
        } else {
            for (var item : items) {
                sb.append(item.getName()).append("\n");
            }
        }
        inventoryArea.setText(sb.toString());
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    public void setOnReturnToMenu(Runnable callback) {this.onReturnToMenu = callback;}
}