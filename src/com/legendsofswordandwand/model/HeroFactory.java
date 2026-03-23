package com.legendsofswordandwand.model;

public class HeroFactory {

    public Hero createHero(String name, HeroClass heroClass) {
        if (name == null || name.isBlank() || heroClass == null) {
            return null;
        }
        return new Hero(name, heroClass);
    }

    public Hero createDefaultWarrior(String name) {
        return createHero(name, HeroClass.WARRIOR);
    }

    public Hero createDefaultMage(String name) {
        return createHero(name, HeroClass.MAGE);
    }

    public Hero createDefaultRogue(String name) {
        return createHero(name, HeroClass.ROGUE);
    }

    public Hero createDefaultCleric(String name) {
        return createHero(name, HeroClass.CLERIC);
    }
}