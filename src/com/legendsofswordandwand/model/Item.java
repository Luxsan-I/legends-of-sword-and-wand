package com.legendsofswordandwand.model;

public class Item {
    private String name;
    private ItemType type;
    private int value;

    public Item(String name, ItemType type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public boolean useOn(Hero hero) {
        if (hero == null) {
            return false;
        }

        switch (type) {
            case HEALTH_POTION:
                hero.heal(value);
                return true;
            case MANA_POTION:
                hero.restoreMana(value);
                return true;
            case REVIVE_SCROLL:
                if (!hero.isAlive()) {
                    hero.revive(value);
                    return true;
                }
                return false;
            case ATTACK_BOOST:
                hero.increaseAttack(value);
                return true;
            case DEFENSE_BOOST:
                hero.increaseDefense(value);
                return true;
            default:
                return false;
        }
    }
}