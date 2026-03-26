package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Item;
import com.legendsofswordandwand.model.Party;

public class ScoreCalculator {

    // Item costs by ItemType — used for the "half price x10" scoring rule.
    // These match the values defined in InnService's shop.
    private static final int HEALTH_POTION_COST = 200;
    private static final int MANA_POTION_COST   = 150;
    private static final int REVIVE_SCROLL_COST = 2000;
    private static final int ATTACK_BOOST_COST  = 500;
    private static final int DEFENSE_BOOST_COST = 500;

    /**
     * Calculates the final campaign score.
     *
     * Scoring rules:
     *   - 100 points per hero level (all heroes, alive or dead)
     *   - 10 points per gold remaining in inventory
     *   - (item purchase price / 2) * 10 per item held in inventory
     *
     * @param progress the completed CampaignProgress
     * @return total score, never negative
     */
    public int calculate(CampaignProgress progress) {
        if (progress == null) {
            return 0;
        }

        Party party = progress.getCurrentParty();
        if (party == null) {
            return 0;
        }

        int score = 0;

        // 100 points per hero level
        for (Hero hero : party.getHeroes()) {
            score += hero.getLevel() * 100;
        }

        // 10 points per gold — gold tracked separately via InnService gold field
        // Party does not hold gold directly; CampaignProgress tracks it via currentScore
        // accumulation during play. We add the raw gold value here if available.
        // NOTE: if Party gains a getGold() method later, replace currentScore usage below.
        score += progress.getCurrentScore() * 10;

        // (cost / 2) * 10 per item in inventory
        for (Item item : party.getInventory().getItems()) {
            int cost = getCostForItem(item);
            score += (cost / 2) * 10;
        }

        return Math.max(score, 0);
    }

    private int getCostForItem(Item item) {
        if (item == null || item.getType() == null) {
            return 0;
        }
        switch (item.getType()) {
            case HEALTH_POTION:  return HEALTH_POTION_COST;
            case MANA_POTION:    return MANA_POTION_COST;
            case REVIVE_SCROLL:  return REVIVE_SCROLL_COST;
            case ATTACK_BOOST:   return ATTACK_BOOST_COST;
            case DEFENSE_BOOST:  return DEFENSE_BOOST_COST;
            default:             return 0;
        }
    }
}
