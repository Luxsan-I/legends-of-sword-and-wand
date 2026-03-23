package com.legendsofswordandwand.model;

public enum HeroClass {
    WARRIOR(140, 30, 25, 10, 8),
    MAGE(90, 100, 12, 8, 30),
    ROGUE(100, 50, 20, 18, 15),
    CLERIC(110, 90, 14, 10, 22);

    private final int baseHp;
    private final int baseMana;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseMagic;

    HeroClass(int baseHp, int baseMana, int baseAttack, int baseDefense, int baseMagic) {
        this.baseHp = baseHp;
        this.baseMana = baseMana;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseMagic = baseMagic;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getBaseMana() {
        return baseMana;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public int getBaseMagic() {
        return baseMagic;
    }
}