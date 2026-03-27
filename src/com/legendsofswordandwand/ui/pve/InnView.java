package com.legendsofswordandwand.ui.pve;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.CampaignProgress.CampaignStatus;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.ItemType;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.pve.InnService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Inn screen.
 *
 * On open, InnService automatically restores the party (heal, mana, revive).
 * The player may then buy items from the shop and recruit heroes (rooms 1-10 only).
 * Clicking "Next Room" saves progress and returns to CampaignView.
 */
public class InnView extends JFrame {

    private final InnService innService;
    private final CampaignProgress progress;
    private final Profile profile;
    private final CampaignRepository campaignRepository;

    // Party status
    private JTextArea partyStatusArea;

    // Shop
    private JTextArea shopLog;

    // Recruit panel
    private JPanel recruitPanel;
    private JTextArea recruitLog;

    // Gold label
    private JLabel goldLabel;

    // Navigation
    private Runnable onNextRoom;

    public InnView(InnService innService,
                   CampaignProgress progress,
                   Profile profile,
                   CampaignRepository campaignRepository) {
        this.innService          = innService;
        this.progress            = progress;
        this.profile             = profile;
        this.campaignRepository  = campaignRepository;

        setTitle("Inn — Room " + progress.getCurrentStage());
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Apply restoration immediately on entry
        innService.applyRestoration(progress.getCurrentParty());
        progress.setStatus(CampaignStatus.IN_INN);
        campaignRepository.saveCampaign(profile, progress);

        buildUI();
        refreshAll();
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top: room info + gold
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        JLabel titleLabel = new JLabel("Welcome to the Inn!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        goldLabel = new JLabel("", SwingConstants.CENTER);
        topPanel.add(titleLabel);
        topPanel.add(goldLabel);
        root.add(topPanel, BorderLayout.NORTH);

        // Centre: party status + shop side by side
        JPanel centrePanel = new JPanel(new GridLayout(1, 2, 8, 0));

        // Left: party status
        partyStatusArea = new JTextArea();
        partyStatusArea.setEditable(false);
        JScrollPane partyScroll = new JScrollPane(partyStatusArea);
        partyScroll.setBorder(BorderFactory.createTitledBorder("Your Party (Restored)"));
        centrePanel.add(partyScroll);

        // Right: item shop
        JPanel shopPanel = buildShopPanel();
        centrePanel.add(shopPanel);

        root.add(centrePanel, BorderLayout.CENTER);

        // Bottom: recruit panel + next room button
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));

        recruitPanel = buildRecruitPanel();
        bottomPanel.add(recruitPanel, BorderLayout.CENTER);

        JButton nextRoomButton = new JButton("Next Room");
        nextRoomButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        nextRoomButton.addActionListener(e -> {
            progress.setStatus(CampaignStatus.BETWEEN_ROOMS);
            campaignRepository.saveCampaign(profile, progress);
            dispose();
            if (onNextRoom != null) onNextRoom.run();
        });
        bottomPanel.add(nextRoomButton, BorderLayout.SOUTH);

        root.add(bottomPanel, BorderLayout.SOUTH);

        add(root);
    }

    private JPanel buildShopPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Item Shop"));

        // Item buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.gridx = 0; gbc.gridy = 0;

        addShopButton(buttonPanel, gbc, "Health Potion — " + InnService.HEALTH_POTION_COST + "g", ItemType.HEALTH_POTION);
        addShopButton(buttonPanel, gbc, "Mana Potion — "   + InnService.MANA_POTION_COST   + "g", ItemType.MANA_POTION);
        addShopButton(buttonPanel, gbc, "Revive Scroll — " + InnService.REVIVE_SCROLL_COST + "g", ItemType.REVIVE_SCROLL);
        addShopButton(buttonPanel, gbc, "Attack Boost — "  + InnService.ATTACK_BOOST_COST  + "g", ItemType.ATTACK_BOOST);
        addShopButton(buttonPanel, gbc, "Defense Boost — " + InnService.DEFENSE_BOOST_COST + "g", ItemType.DEFENSE_BOOST);

        panel.add(buttonPanel, BorderLayout.NORTH);

        shopLog = new JTextArea(4, 20);
        shopLog.setEditable(false);
        shopLog.setLineWrap(true);
        shopLog.setWrapStyleWord(true);
        panel.add(new JScrollPane(shopLog), BorderLayout.CENTER);

        return panel;
    }

    private void addShopButton(JPanel panel, GridBagConstraints gbc, String label, ItemType type) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> {
            boolean bought = innService.buyItem(progress.getCurrentParty(), type);
            if (bought) {
                shopLog.append("Purchased " + type + ".\n");
            } else {
                shopLog.append("Not enough gold for " + type + ".\n");
            }
            refreshGold();
        });
        panel.add(btn, gbc);
        gbc.gridy++;
    }

    private JPanel buildRecruitPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Recruit Heroes"));

        int currentRoom = progress.getCurrentStage();

        if (currentRoom > 10) {
            panel.add(new JLabel("No heroes available after room 10.", SwingConstants.CENTER),
                    BorderLayout.CENTER);
            return panel;
        }

        List<Hero> recruits = innService.getAvailableRecruits(
                progress.getCurrentParty(), currentRoom);

        if (recruits.isEmpty()) {
            panel.add(new JLabel("No recruits available (party may be full).", SwingConstants.CENTER),
                    BorderLayout.CENTER);
            return panel;
        }

        JPanel recruitButtons = new JPanel(new GridLayout(1, recruits.size(), 6, 0));
        recruitLog = new JTextArea(2, 30);
        recruitLog.setEditable(false);
        recruitLog.setLineWrap(true);

        for (Hero recruit : recruits) {
            int cost = innService.getRecruitCost(recruit);
            String label = recruit.getName() + " [" + recruit.getHeroClass() + "]"
                    + " Lvl " + recruit.getLevel()
                    + " — " + (cost == 0 ? "Free" : cost + "g");
            JButton btn = new JButton("<html><center>" + label + "</center></html>");
            btn.addActionListener(e -> {
                boolean recruited = innService.recruitHero(
                        progress.getCurrentParty(), recruit, currentRoom);
                if (recruited) {
                    recruitLog.append(recruit.getName() + " joined your party!\n");
                    btn.setEnabled(false);
                } else {
                    recruitLog.append("Could not recruit " + recruit.getName()
                            + " (party full or insufficient gold).\n");
                }
                refreshAll();
            });
            recruitButtons.add(btn);
        }

        panel.add(recruitButtons, BorderLayout.CENTER);
        panel.add(new JScrollPane(recruitLog), BorderLayout.SOUTH);

        return panel;
    }

    // -------------------------------------------------------------------------
    // Refresh helpers
    // -------------------------------------------------------------------------

    private void refreshAll() {
        refreshPartyStatus();
        refreshGold();
    }

    private void refreshPartyStatus() {
        StringBuilder sb = new StringBuilder();
        for (Hero hero : progress.getCurrentParty().getHeroes()) {
            sb.append(hero.getName())
                    .append(" [").append(hero.getHeroClass()).append("]")
                    .append("  Lvl ").append(hero.getLevel())
                    .append("  HP:").append(hero.getCurrentHp()).append("/").append(hero.getMaxHp())
                    .append("  MP:").append(hero.getCurrentMana()).append("/").append(hero.getMaxMana())
                    .append("\n");
        }
        partyStatusArea.setText(sb.toString());
    }

    private void refreshGold() {
        goldLabel.setText("Gold: " + innService.getGold() + "g");
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    public void setOnNextRoom(Runnable callback) {this.onNextRoom = callback;}
}