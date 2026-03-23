package com.legendsofswordandwand.model;

public class BattleResult {
    private Party winningParty;
    private Party losingParty;
    private int turnsPlayed;

    public BattleResult(Party winningParty, Party losingParty, int turnsPlayed) {
        this.winningParty = winningParty;
        this.losingParty = losingParty;
        this.turnsPlayed = turnsPlayed;
    }

    public Party getWinningParty() {
        return winningParty;
    }

    public Party getLosingParty() {
        return losingParty;
    }

    public int getTurnsPlayed() {
        return turnsPlayed;
    }
}