package com.legendsofswordandwand.ui.pvp;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.HeroFactory;
import com.legendsofswordandwand.model.Party;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Screen where a player builds their party before a PvP match.
 * Allows adding up to 4 heroes by entering a name and selecting a class.
 */
public class PartyBuilderView extends JFrame {

    private final String playerLabel;
    private final Party party;
    private final HeroFactory heroFactory;

    private JTextField partyNameField;
    private JTextField heroNameField;
    private JComboBox<HeroClass> heroClassCombo;
    private JTextArea partyDisplay;
    private JButton addHeroButton;
    private JButton confirmButton;

    public PartyBuilderView(String playerLabel) {
        this.playerLabel = playerLabel;
        this.party = new Party(playerLabel + "'s Party");
        this.heroFactory = new HeroFactory();

        setTitle("Build Party — " + playerLabel);
        setSize(450, 420);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top: party name ---
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Party Name:"));
        partyNameField = new JTextField(party.getPartyName(), 20);
        namePanel.add(partyNameField);
        mainPanel.add(namePanel, BorderLayout.NORTH);

        // --- Centre: add hero controls + party display ---
        JPanel centrePanel = new JPanel(new BorderLayout(5, 5));

        JPanel addPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("Add Hero (max 4)"));
        addPanel.add(new JLabel("Hero Name:"));
        heroNameField = new JTextField();
        addPanel.add(heroNameField);
        addPanel.add(new JLabel("Class:"));
        heroClassCombo = new JComboBox<>(HeroClass.values());
        addPanel.add(heroClassCombo);
        addHeroButton = new JButton("Add Hero");
        addPanel.add(new JLabel());
        addPanel.add(addHeroButton);
        centrePanel.add(addPanel, BorderLayout.NORTH);

        partyDisplay = new JTextArea(5, 30);
        partyDisplay.setEditable(false);
        partyDisplay.setBorder(BorderFactory.createTitledBorder("Current Party"));
        centrePanel.add(new JScrollPane(partyDisplay), BorderLayout.CENTER);

        mainPanel.add(centrePanel, BorderLayout.CENTER);

        // --- Bottom: confirm button ---
        confirmButton = new JButton("Confirm Party & Continue");
        confirmButton.setEnabled(false);
        mainPanel.add(confirmButton, BorderLayout.SOUTH);

        add(mainPanel);

        // Wire up Add Hero button
        addHeroButton.addActionListener(e -> handleAddHero());
    }

    private void handleAddHero() {
        String heroName = heroNameField.getText().trim();
        if (heroName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a hero name.");
            return;
        }

        HeroClass heroClass = (HeroClass) heroClassCombo.getSelectedItem();
        Hero hero = heroFactory.createHero(heroName, heroClass);

        if (!party.addHero(hero)) {
            JOptionPane.showMessageDialog(this, "Party is full (max 4 heroes).");
            return;
        }

        heroNameField.setText("");
        refreshPartyDisplay();

        // Enable confirm once there is at least 1 hero
        confirmButton.setEnabled(true);

        if (party.getHeroes().size() >= 4) {
            addHeroButton.setEnabled(false);
        }
    }

    private void refreshPartyDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Hero hero : party.getHeroes()) {
            sb.append(hero.getName())
              .append(" (").append(hero.getHeroClass()).append(")")
              .append("  HP:").append(hero.getMaxHp())
              .append("  ATK:").append(hero.getAttack())
              .append("  DEF:").append(hero.getDefense())
              .append("  MAG:").append(hero.getMagic())
              .append("\n");
        }
        partyDisplay.setText(sb.toString());
    }

    /** Returns the party built by the player (call after confirmation). */
    public Party getParty() {
        // Update party name in case the player changed the field
        return party;
    }

    public void addConfirmListener(ActionListener listener) {
        confirmButton.addActionListener(listener);
    }
}
