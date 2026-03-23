package com.legendsofswordandwand.model;

import java.util.ArrayList;
import java.util.List;

public class Party {
    private String partyName;
    private List<Hero> heroes;
    private static final int MAX_HEROES = 4;
    private Inventory inventory;

    public Party(String partyName) {
        this.partyName = partyName;
        this.heroes = new ArrayList<>();
        this.inventory = new Inventory();
    }

    public String getPartyName() {
        return partyName;
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean addHero(Hero hero) {
        if (hero == null || heroes.size() >= MAX_HEROES) {
            return false;
        }
        heroes.add(hero);
        return true;
    }

    public boolean removeHero(Hero hero) {
        return heroes.remove(hero);
    }

    public boolean isDefeated() {
        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public List<Hero> getAliveHeroes() {
        List<Hero> aliveHeroes = new ArrayList<>();
        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                aliveHeroes.add(hero);
            }
        }
        return aliveHeroes;
    }

    public void resetDefendingStatus() {
        for (Hero hero : heroes) {
            hero.setDefending(false);
        }
    }
}