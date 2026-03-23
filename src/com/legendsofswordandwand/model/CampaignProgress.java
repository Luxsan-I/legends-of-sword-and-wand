package com.legendsofswordandwand.model;

public class CampaignProgress {
    private int currentStage;
    private boolean campaignCompleted;
    private Party currentParty;
    private int currentScore;

    public CampaignProgress(int currentStage, Party currentParty, int currentScore) {
        this.currentStage = currentStage;
        this.currentParty = currentParty;
        this.currentScore = currentScore;
        this.campaignCompleted = false;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public boolean isCampaignCompleted() {
        return campaignCompleted;
    }

    public Party getCurrentParty() {
        return currentParty;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void advanceStage() {
        currentStage++;
    }

    public void completeCampaign() {
        campaignCompleted = true;
    }

    public void setCurrentParty(Party currentParty) {
        this.currentParty = currentParty;
    }

    public void addScore(int score) {
        if (score > 0) {
            currentScore += score;
        }
    }
}