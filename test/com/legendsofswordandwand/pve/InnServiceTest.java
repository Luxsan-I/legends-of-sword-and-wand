package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.ItemType;
import com.legendsofswordandwand.model.Party;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class InnServiceTest {

    private InnService innService;
    private Party party;
    private Hero hero;

    @BeforeEach
    void setUp() {
        innService = new InnService(1000);
        party      = new Party("Test Party");
        hero       = new Hero("Hero", HeroClass.WARRIOR);
        party.addHero(hero);
    }

    // -------------------------------------------------------------------------
    // TC-08: Inn restoration — heal, mana, revive
    // -------------------------------------------------------------------------

    @Test
    void applyRestoration_fullHealsDamagedHero() {
        hero.takeDamage(50);
        int hpBefore = hero.getCurrentHp();

        innService.applyRestoration(party);

        assertTrue(hero.getCurrentHp() > hpBefore);
        assertEquals(hero.getMaxHp(), hero.getCurrentHp());
    }

    @Test
    void applyRestoration_fullRestoresMana() {
        hero.useMana(20);

        innService.applyRestoration(party);

        assertEquals(hero.getMaxMana(), hero.getCurrentMana());
    }

    @Test
    void applyRestoration_revivesDeadHero() {
        hero.takeDamage(hero.getMaxHp()); // kill
        assertFalse(hero.isAlive());

        innService.applyRestoration(party);

        assertTrue(hero.isAlive());
    }

    @Test
    void applyRestoration_nullPartyDoesNotThrow() {
        assertDoesNotThrow(() -> innService.applyRestoration(null));
    }

    @Test
    void applyRestoration_allHeroesInPartyAreHealed() {
        Hero second = new Hero("Second", HeroClass.MAGE);
        second.takeDamage(30);
        party.addHero(second);

        innService.applyRestoration(party);

        assertEquals(hero.getMaxHp(), hero.getCurrentHp());
        assertEquals(second.getMaxHp(), second.getCurrentHp());
    }

    // -------------------------------------------------------------------------
    // TC-09: Item purchase deducts gold and adds item to inventory
    // -------------------------------------------------------------------------

    @Test
    void buyItem_deductsCorrectGold() {
        int before = innService.getGold();

        innService.buyItem(party, ItemType.HEALTH_POTION);

        assertEquals(before - InnService.HEALTH_POTION_COST, innService.getGold());
    }

    @Test
    void buyItem_addsItemToInventory() {
        innService.buyItem(party, ItemType.HEALTH_POTION);

        assertEquals(1, party.getInventory().size());
    }

    @Test
    void buyItem_returnsTrueOnSuccess() {
        assertTrue(innService.buyItem(party, ItemType.HEALTH_POTION));
    }

    @Test
    void buyItem_returnsFalseWhenInsufficientGold() {
        InnService broke = new InnService(0);

        assertFalse(broke.buyItem(party, ItemType.HEALTH_POTION));
    }

    @Test
    void buyItem_doesNotDeductGoldOnFailure() {
        InnService broke = new InnService(100); // less than any item cost
        broke.buyItem(party, ItemType.HEALTH_POTION);

        assertEquals(100, broke.getGold());
    }

    @Test
    void buyItem_doesNotAddItemOnFailure() {
        InnService broke = new InnService(0);
        broke.buyItem(party, ItemType.HEALTH_POTION);

        assertEquals(0, party.getInventory().size());
    }

    @Test
    void buyItem_nullTypeReturnsFalse() {
        assertFalse(innService.buyItem(party, null));
    }

    @Test
    void buyItem_nullPartyReturnsFalse() {
        assertFalse(innService.buyItem(null, ItemType.HEALTH_POTION));
    }

    @Test
    void buyItem_reviveScrollCostsCorrectAmount() {
        InnService rich = new InnService(5000);
        int before = rich.getGold();

        rich.buyItem(party, ItemType.REVIVE_SCROLL);

        assertEquals(before - InnService.REVIVE_SCROLL_COST, rich.getGold());
    }

    // -------------------------------------------------------------------------
    // Recruitment — room guard, party full guard, gold guard
    // -------------------------------------------------------------------------

    @Test
    void recruitHero_succeedsWithinFirstTenRooms() {
        Hero recruit = new Hero("Recruit", HeroClass.MAGE); // level 1, free

        boolean result = innService.recruitHero(party, recruit, 5);

        assertTrue(result);
        assertEquals(2, party.getHeroes().size());
    }

    @Test
    void recruitHero_blockedAfterRoomTen() {
        Hero recruit = new Hero("Recruit", HeroClass.MAGE);

        boolean result = innService.recruitHero(party, recruit, 11);

        assertFalse(result);
        assertEquals(1, party.getHeroes().size());
    }

    @Test
    void recruitHero_blockedWhenPartyFull() {
        // Fill party to max (4)
        party.addHero(new Hero("H2", HeroClass.MAGE));
        party.addHero(new Hero("H3", HeroClass.ROGUE));
        party.addHero(new Hero("H4", HeroClass.CLERIC));

        Hero recruit = new Hero("Recruit", HeroClass.WARRIOR);
        boolean result = innService.recruitHero(party, recruit, 5);

        assertFalse(result);
        assertEquals(4, party.getHeroes().size());
    }

    @Test
    void recruitHero_levelOneCostIsZero() {
        Hero recruit = new Hero("Recruit", HeroClass.MAGE); // level 1
        int goldBefore = innService.getGold();

        innService.recruitHero(party, recruit, 5);

        assertEquals(goldBefore, innService.getGold());
    }

    @Test
    void recruitHero_levelTwoChargesCorrectAmount() {
        Hero recruit = new Hero("Recruit", HeroClass.MAGE);
        recruit.levelUp(); // level 2, cost = 200 * 2 = 400

        int goldBefore = innService.getGold();
        innService.recruitHero(party, recruit, 5);

        assertEquals(goldBefore - 400, innService.getGold());
    }

    @Test
    void recruitHero_blockedWhenInsufficientGold() {
        InnService broke = new InnService(0);
        Hero recruit = new Hero("Recruit", HeroClass.MAGE);
        recruit.levelUp(); // costs 400g

        boolean result = broke.recruitHero(party, recruit, 5);

        assertFalse(result);
    }

    @Test
    void recruitHero_nullHeroReturnsFalse() {
        assertFalse(innService.recruitHero(party, null, 5));
    }

    @Test
    void recruitHero_nullPartyReturnsFalse() {
        Hero recruit = new Hero("Recruit", HeroClass.MAGE);
        assertFalse(innService.recruitHero(null, recruit, 5));
    }

    // -------------------------------------------------------------------------
    // getAvailableRecruits — deterministic via seeded Random
    // -------------------------------------------------------------------------

    @Test
    void getAvailableRecruits_emptyAfterRoomTen() {
        List<Hero> recruits = innService.getAvailableRecruits(party, 11);
        assertTrue(recruits.isEmpty());
    }

    @Test
    void getAvailableRecruits_emptyWhenPartyFull() {
        party.addHero(new Hero("H2", HeroClass.MAGE));
        party.addHero(new Hero("H3", HeroClass.ROGUE));
        party.addHero(new Hero("H4", HeroClass.CLERIC));

        List<Hero> recruits = innService.getAvailableRecruits(party, 5);
        assertTrue(recruits.isEmpty());
    }

    @Test
    void getAvailableRecruits_returnsHeroesWithinFirstTenRooms() {
        // Seeded random for determinism: always picks 1 hero
        InnService seeded = new InnService(1000, new Random(42));
        List<Hero> recruits = seeded.getAvailableRecruits(party, 5);

        assertFalse(recruits.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Gold management
    // -------------------------------------------------------------------------

    @Test
    void addGold_increasesBalance() {
        innService.addGold(500);
        assertEquals(1500, innService.getGold());
    }

    @Test
    void deductGold_decreasesBalance() {
        innService.deductGold(200);
        assertEquals(800, innService.getGold());
    }

    @Test
    void deductGold_doesNotGoBelowZero() {
        innService.deductGold(5000);
        assertEquals(1000, innService.getGold()); // unchanged — guard prevents over-deduction
    }
}
