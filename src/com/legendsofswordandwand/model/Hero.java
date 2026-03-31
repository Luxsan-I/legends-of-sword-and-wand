package com.legendsofswordandwand.model;

public class Hero {
    private String name;
    private HeroClass heroClass;
    private int level;
    private int maxHp;
    private int currentHp;
    private int maxMana;
    private int currentMana;
    private int attack;
    private int defense;
    private int magic;
    private boolean defending;

    public Hero(String name, HeroClass heroClass) {
        this.name = name;
        this.heroClass = heroClass;
        this.level = 1;
        this.maxHp = heroClass.getBaseHp();
        this.currentHp = maxHp;
        this.maxMana = heroClass.getBaseMana();
        this.currentMana = maxMana;
        this.attack = heroClass.getBaseAttack();
        this.defense = heroClass.getBaseDefense();
        this.magic = heroClass.getBaseMagic();
        this.defending = false;
    }

    public String getName() {
        return name;
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getMagic() {
        return magic;
    }

    public boolean isDefending() {
        return defending;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            damage = 0;
        }
        currentHp -= damage;
        if (currentHp < 0) {
            currentHp = 0;
        }
    }

    public void heal(int amount) {
        if (amount < 0) {
            return;
        }
        currentHp += amount;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }

    public void restoreMana(int amount) {
        if (amount < 0) {
            return;
        }
        currentMana += amount;
        if (currentMana > maxMana) {
            currentMana = maxMana;
        }
    }

    public boolean useMana(int amount) {
        if (amount < 0 || currentMana < amount) {
            return false;
        }
        currentMana -= amount;
        return true;
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    public int getEffectiveDefense() {
        if (defending) {
            return defense + 5;
        }
        return defense;
    }

    public void levelUp() {
        level++;
        maxHp += 15;
        maxMana += 10;
        attack += 4;
        defense += 3;
        magic += 4;
        currentHp = maxHp;
        currentMana = maxMana;
    }

    public void resetForBattle() {
        defending = false;
    }

    public void revive(int hpAmount) {
        if (!isAlive()) {
            currentHp = Math.min(hpAmount, maxHp);
        }
    }

    public void increaseAttack(int amount) {
        if (amount > 0) {
            attack += amount;
        }
    }

    public void increaseDefense(int amount) {
        if (amount > 0) {
            defense += amount;
        }
    }

    @Override
    public String toString() {
        return name + " [" + heroClass + "] Lvl " + level
                + " HP:" + currentHp + "/" + maxHp;
    }
}