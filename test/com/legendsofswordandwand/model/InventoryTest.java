package com.legendsofswordandwand.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    @Test
    public void testAddItemIncreasesSize() {
        Inventory inventory = new Inventory();
        inventory.addItem(new Item("Potion", ItemType.HEALTH_POTION, 25));

        assertEquals(1, inventory.size());
    }

    @Test
    public void testUseHealthPotionRemovesItemAndHealsHero() {
        Inventory inventory = new Inventory();
        Item potion = new Item("Potion", ItemType.HEALTH_POTION, 25);
        Hero hero = new Hero("Luna", HeroClass.CLERIC);

        hero.takeDamage(30);
        inventory.addItem(potion);

        int hpBefore = hero.getCurrentHp();
        boolean used = inventory.useItem(potion, hero);

        assertTrue(used);
        assertTrue(hero.getCurrentHp() > hpBefore);
        assertEquals(0, inventory.size());
    }

    @Test
    public void testUseItemFailsWhenItemNotInInventory() {
        Inventory inventory = new Inventory();
        Item potion = new Item("Potion", ItemType.HEALTH_POTION, 25);
        Hero hero = new Hero("Luna", HeroClass.CLERIC);

        boolean used = inventory.useItem(potion, hero);

        assertFalse(used);
    }
}