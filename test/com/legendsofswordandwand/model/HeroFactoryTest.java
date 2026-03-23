package com.legendsofswordandwand.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HeroFactoryTest {

    @Test
    public void testCreateHeroReturnsCorrectHero() {
        HeroFactory factory = new HeroFactory();
        Hero hero = factory.createHero("Ares", HeroClass.WARRIOR);

        assertNotNull(hero);
        assertEquals("Ares", hero.getName());
        assertEquals(HeroClass.WARRIOR, hero.getHeroClass());
    }

    @Test
    public void testCreateHeroReturnsNullForInvalidInput() {
        HeroFactory factory = new HeroFactory();

        assertNull(factory.createHero("", HeroClass.MAGE));
        assertNull(factory.createHero("Mage", null));
    }
}