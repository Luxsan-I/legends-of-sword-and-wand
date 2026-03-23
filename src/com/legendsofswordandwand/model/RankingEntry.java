package com.legendsofswordandwand.model;

public class RankingEntry {
    private String username;
    private int score;
    private int wins;
    private int losses;

    public RankingEntry(String username, int score, int wins, int losses) {
        this.username = username;
        this.score = score;
        this.wins = wins;
        this.losses = losses;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }
}