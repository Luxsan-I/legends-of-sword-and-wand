package com.legendsofswordandwand.model;

import java.util.HashMap;
import java.util.Map;

public class CampaignProgress {

    public enum CampaignStatus {BETWEEN_ROOMS, IN_INN, IN_BATTLE}

    private int currentStage;
    private boolean campaignCompleted;
    private Party currentParty;
    private int currentScore;
    private String username;
    private CampaignStatus status;
    private Map<Hero, Integer> heroExperience;


    public CampaignProgress(String username, int currentStage, Party currentParty, int currentScore) {
        this.currentStage = currentStage;
        this.currentParty = currentParty;
        this.currentScore = currentScore;
        this.campaignCompleted = false;
        this.username = username;
        this.status = CampaignStatus.BETWEEN_ROOMS;
        this.heroExperience = new HashMap<>();
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

    public String getUsername() {return username;}

    public CampaignStatus getStatus() {return status;}

    public void advanceStage() {
        currentStage++;
    }

    public void completeCampaign() {
        campaignCompleted = true;
    }

    public void setCurrentParty(Party currentParty) {
        this.currentParty = currentParty;
    }

    public void setStatus(CampaignStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void addScore(int score) {
        if (score > 0) {
            currentScore += score;
        }
    }

    public void awardExperience(Hero hero, int amount) {
        if (hero == null) return;
        int current = heroExperience.getOrDefault(hero, 0);
        heroExperience.put(hero, Math.max(0, current + amount));
    }

    public int getExperience(Hero hero) {
        if (hero == null) return 0;
        return heroExperience.getOrDefault(hero, 0);
    }

    public int expToNextLevel(int level) {
        int total = 0;
        for (int l = 1; l <= level; l++) {
            total += 500 + 75 * l + 20 * l * l;
        }
        return total;
    }

    public boolean canLevelUp(Hero hero) {
        if (hero == null) return false;
        return getExperience(hero) >= expToNextLevel(hero.getLevel());
    }

    public void levelUpIfReady(Hero hero) {
        if (hero == null) return;
        while (canLevelUp(hero)) {
            int cost = expToNextLevel(hero.getLevel());
            heroExperience.put(hero, getExperience(hero) - cost);
            hero.levelUp();
        }
    }
}