package com.legendsofswordandwand.ui.shared;

import com.legendsofswordandwand.model.RankingEntry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingView extends JFrame {
    private JTextArea rankingArea;

    public RankingView(List<RankingEntry> entries, String titleText) {
        setTitle(titleText);
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        rankingArea = new JTextArea();
        rankingArea.setEditable(false);

        StringBuilder builder = new StringBuilder();
        for (RankingEntry entry : entries) {
            builder.append(entry.getUsername())
                    .append(" | Score: ").append(entry.getScore())
                    .append(" | Wins: ").append(entry.getWins())
                    .append(" | Losses: ").append(entry.getLosses())
                    .append("\n");
        }

        rankingArea.setText(builder.toString());
        add(new JScrollPane(rankingArea), BorderLayout.CENTER);
    }
}