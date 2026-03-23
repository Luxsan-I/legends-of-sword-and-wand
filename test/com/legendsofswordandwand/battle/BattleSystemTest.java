package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.BattleResult;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Party;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BattleSystemTest {

    @Test
    public void testPerformAttackReducesTargetHp() {
        Hero attacker = new Hero("Ares", HeroClass.WARRIOR);
        Hero target = new Hero("Merlin", HeroClass.MAGE);

        Party partyOne = new Party("Alpha");
        Party partyTwo = new Party("Beta");

        partyOne.addHero(attacker);
        partyTwo.addHero(target);

        BattleSystem battleSystem = new BattleSystem(partyOne, partyTwo);

        int hpBefore = target.getCurrentHp();
        battleSystem.performAttack(attacker, target);

        assertTrue(target.getCurrentHp() < hpBefore);
    }

    @Test
    public void testPerformDefendRestoresHpAndMana() {
        Hero hero = new Hero("Luna", HeroClass.CLERIC);
        hero.takeDamage(30);
        hero.useMana(20);

        Party partyOne = new Party("Alpha");
        Party partyTwo = new Party("Beta");

        partyOne.addHero(hero);
        partyTwo.addHero(new Hero("Enemy", HeroClass.ROGUE));

        BattleSystem battleSystem = new BattleSystem(partyOne, partyTwo);

        int hpBefore = hero.getCurrentHp();
        int manaBefore = hero.getCurrentMana();

        battleSystem.performDefend(hero);

        assertEquals(hpBefore + 10, hero.getCurrentHp());
        assertEquals(manaBefore + 5, hero.getCurrentMana());
        assertFalse(hero.isDefending());
    }

    @Test
    public void testPerformWaitEndsTurn() {
        Hero heroOne = new Hero("HeroOne", HeroClass.WARRIOR);
        Hero heroTwo = new Hero("HeroTwo", HeroClass.MAGE);

        Party partyOne = new Party("Alpha");
        Party partyTwo = new Party("Beta");

        partyOne.addHero(heroOne);
        partyTwo.addHero(heroTwo);

        BattleSystem battleSystem = new BattleSystem(partyOne, partyTwo);

        Hero currentHero = battleSystem.getCurrentHero();
        battleSystem.performWait(currentHero);
        Hero nextHero = battleSystem.getCurrentHero();

        assertNotNull(nextHero);
        assertNotEquals(currentHero, nextHero);
    }

    @Test
    public void testPerformCastAbilityUsesManaAndDamagesTarget() {
        Hero caster = new Hero("Caster", HeroClass.MAGE);
        Hero target = new Hero("Target", HeroClass.WARRIOR);

        Party partyOne = new Party("Alpha");
        Party partyTwo = new Party("Beta");

        partyOne.addHero(caster);
        partyTwo.addHero(target);

        BattleSystem battleSystem = new BattleSystem(partyOne, partyTwo);

        int manaBefore = caster.getCurrentMana();
        int hpBefore = target.getCurrentHp();

        battleSystem.performCastAbility(caster, target);

        assertEquals(manaBefore - 20, caster.getCurrentMana());
        assertTrue(target.getCurrentHp() < hpBefore);
    }

    @Test
    public void testBattleEndsWhenOnePartyIsDefeated() {
        Hero strongHero = new Hero("Strong", HeroClass.WARRIOR);
        Hero weakHero = new Hero("Weak", HeroClass.MAGE);

        Party partyOne = new Party("Alpha");
        Party partyTwo = new Party("Beta");

        partyOne.addHero(strongHero);
        partyTwo.addHero(weakHero);

        BattleSystem battleSystem = new BattleSystem(partyOne, partyTwo);

        while (!battleSystem.isBattleOver()) {
            Hero currentHero = battleSystem.getCurrentHero();
            if (currentHero == strongHero) {
                battleSystem.performAttack(strongHero, weakHero);
            } else {
                battleSystem.performWait(weakHero);
            }
        }

        BattleResult result = battleSystem.getBattleResult();

        assertNotNull(result);
        assertEquals(partyOne, result.getWinningParty());
        assertEquals(partyTwo, result.getLosingParty());
    }
}