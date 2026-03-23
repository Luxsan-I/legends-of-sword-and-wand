package com.legendsofswordandwand.model;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private String username;
    private String password;
    private int totalScore;
    private int pvpWins;
    private int pvpLosses;
    private List<Party> savedParties;
    private Inventory inventory;

    public Profile(String username, String password) {
        this.username = username;
        this.password = password;
        this.totalScore = 0;
        this.pvpWins = 0;
        this.pvpLosses = 0;
        this.savedParties = new ArrayList<>();
        this.inventory = new Inventory();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getPvpWins() {
        return pvpWins;
    }

    public int getPvpLosses() {
        return pvpLosses;
    }

    public List<Party> getSavedParties() {
        return savedParties;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void addScore(int score) {
        if (score > 0) {
            totalScore += score;
        }
    }

    public void recordWin() {
        pvpWins++;
    }

    public void recordLoss() {
        pvpLosses++;
    }

    public boolean saveParty(Party party) {
        if (party == null || savedParties.size() >= 5) {
            return false;
        }
        savedParties.add(party);
        return true;
    }

    public boolean replaceParty(int index, Party party) {
        if (party == null || index < 0 || index >= savedParties.size()) {
            return false;
        }
        savedParties.set(index, party);
        return true;
    }
}