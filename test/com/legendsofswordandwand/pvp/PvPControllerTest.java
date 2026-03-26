package com.legendsofswordandwand.pvp;

import com.legendsofswordandwand.model.*;
import com.legendsofswordandwand.persistence.DatabaseManager;
import com.legendsofswordandwand.persistence.ProfileRepository;
import com.legendsofswordandwand.persistence.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional test cases for the PvP system.
 * Tests cover: controller setup, Observer events, State transitions,
 * win/loss recording, and battle termination.
 */
public class PvPControllerTest {

    private Profile profileOne;
    private Profile profileTwo;
    private Party partyOne;
    private Party partyTwo;
    private RankingRepository rankingRepository;
    private HeroFactory heroFactory;

    @BeforeEach
    void setUp() {
        // Reset singleton between tests
        DatabaseManager.resetInstance();

        ProfileRepository profileRepository = new ProfileRepository();
        profileRepository.createProfile("Alice", "pass1");
        profileRepository.createProfile("Bob", "pass2");
        profileOne = profileRepository.findByUsername("Alice");
        profileTwo = profileRepository.findByUsername("Bob");

        rankingRepository = new RankingRepository();
        heroFactory = new HeroFactory();

        // Build two simple 1-hero parties for fast tests
        partyOne = new Party("Alice's Party");
        partyOne.addHero(heroFactory.createHero("Aldric", HeroClass.WARRIOR));

        partyTwo = new Party("Bob's Party");
        partyTwo.addHero(heroFactory.createHero("Zara", HeroClass.MAGE));
    }

    // -------------------------------------------------------------------------
    // TC-PVP-01: Controller initialises in PlayerTurnState
    // -------------------------------------------------------------------------
    @Test
    void testInitialStateIsPlayerOneTurn() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        assertFalse(controller.isBattleOver());
        assertTrue(controller.getCurrentState() instanceof PlayerTurnState);
        assertEquals("Alice's Turn", controller.getCurrentState().getStateLabel());
    }

    // -------------------------------------------------------------------------
    // TC-PVP-02: Observer is notified on attack
    // -------------------------------------------------------------------------
    @Test
    void testObserverReceivesAttackEvent() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        List<BattleEvent> received = new ArrayList<>();
        controller.addBattleEventListener(received::add);

        Hero attacker = controller.getBattleSystem().getCurrentHero();
        Hero target = controller.getBattleSystem().getPartyTwo().getAliveHeroes().get(0);
        controller.attack(attacker, target);

        assertFalse(received.isEmpty(), "Observer should have received at least one event");
        BattleEvent first = received.get(0);
        assertTrue(first.getType() == BattleEvent.Type.HERO_ATTACKED
                        || first.getType() == BattleEvent.Type.HERO_DIED
                        || first.getType() == BattleEvent.Type.BATTLE_OVER,
                "First event should be attack-related");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-03: Defend action heals the hero and fires HERO_DEFENDED event
    // -------------------------------------------------------------------------
    @Test
    void testDefendHealsAndFiresEvent() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        Hero defender = controller.getBattleSystem().getCurrentHero();
        // Manually damage the hero so healing is visible
        defender.takeDamage(30);
        int hpBefore = defender.getCurrentHp();

        List<BattleEvent> received = new ArrayList<>();
        controller.addBattleEventListener(received::add);

        controller.defend(defender);

        int hpAfter = defender.getCurrentHp();
        assertTrue(hpAfter > hpBefore, "Defending should heal the hero");

        boolean hasDefendEvent = received.stream()
                .anyMatch(e -> e.getType() == BattleEvent.Type.HERO_DEFENDED);
        assertTrue(hasDefendEvent, "A HERO_DEFENDED event should have fired");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-04: Cast Ability fails when hero has insufficient mana
    // -------------------------------------------------------------------------
    @Test
    void testCastAbilityFailsWithNoMana() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        Hero caster = controller.getBattleSystem().getCurrentHero();
        // Drain all mana
        caster.useMana(caster.getCurrentMana());

        Hero target = controller.getBattleSystem().getPartyTwo().getAliveHeroes().get(0);
        int targetHpBefore = target.getCurrentHp();

        controller.castAbility(caster, target);

        assertEquals(targetHpBefore, target.getCurrentHp(),
                "Target HP should not change when caster has no mana");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-05: Wait moves the hero to the end of the turn queue
    // -------------------------------------------------------------------------
    @Test
    void testWaitMovesHeroToEndOfQueue() {
        // Use a 2v2 so there is always a next hero
        Party p1 = new Party("A");
        p1.addHero(heroFactory.createHero("H1", HeroClass.WARRIOR));
        p1.addHero(heroFactory.createHero("H2", HeroClass.ROGUE));
        Party p2 = new Party("B");
        p2.addHero(heroFactory.createHero("H3", HeroClass.MAGE));
        p2.addHero(heroFactory.createHero("H4", HeroClass.CLERIC));

        PvPController controller = new PvPController(
                profileOne, p1, profileTwo, p2, rankingRepository);

        Hero first = controller.getBattleSystem().getCurrentHero();
        controller.waitTurn(first);
        Hero afterWait = controller.getBattleSystem().getCurrentHero();

        assertNotEquals(first, afterWait,
                "After Wait, a different hero should be acting");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-06: Battle transitions to BattleOverState when a party is wiped
    // -------------------------------------------------------------------------
    @Test
    void testBattleEndsWhenPartyDefeated() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        // Kill all heroes in party two directly
        for (Hero h : partyTwo.getHeroes()) {
            h.takeDamage(h.getCurrentHp());
        }

        // Fire one more action to trigger advanceTurn
        Hero actor = controller.getBattleSystem().getCurrentHero();
        // If actor is already in party two and dead, use party one hero
        if (actor == null || !actor.isAlive()) {
            actor = partyOne.getAliveHeroes().get(0);
        }
        controller.defend(actor);

        assertTrue(controller.isBattleOver(), "Battle should be over");
        assertTrue(controller.getCurrentState() instanceof BattleOverState,
                "State should be BattleOverState");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-07: Win/loss records are updated correctly after battle
    // -------------------------------------------------------------------------
    @Test
    void testWinLossRecordedAfterBattle() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        // Wipe party two
        for (Hero h : partyTwo.getHeroes()) {
            h.takeDamage(h.getCurrentHp());
        }

        Hero actor = partyOne.getAliveHeroes().get(0);
        controller.defend(actor); // triggers advanceTurn -> finalizeBattle

        // profileOne should have 1 win, profileTwo 1 loss
        assertEquals(1, profileOne.getPvpWins(), "Winner should have 1 win");
        assertEquals(1, profileTwo.getPvpLosses(), "Loser should have 1 loss");
        assertEquals(0, profileOne.getPvpLosses(), "Winner should have 0 losses");
        assertEquals(0, profileTwo.getPvpWins(), "Loser should have 0 wins");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-08: Actions in BattleOverState are silently ignored
    // -------------------------------------------------------------------------
    @Test
    void testNoActionsAllowedAfterBattleOver() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        // Force battle over
        for (Hero h : partyTwo.getHeroes()) {
            h.takeDamage(h.getCurrentHp());
        }
        controller.defend(partyOne.getAliveHeroes().get(0));
        assertTrue(controller.isBattleOver());

        // Register observer to ensure no new events fire
        List<BattleEvent> received = new ArrayList<>();
        controller.addBattleEventListener(received::add);

        // Attempt actions — all should be silently ignored
        Hero anyHero = partyOne.getAliveHeroes().get(0);
        controller.attack(anyHero, anyHero);
        controller.defend(anyHero);
        controller.waitTurn(anyHero);

        assertTrue(received.isEmpty(),
                "No events should fire once the battle is over");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-09: Observer can be removed and no longer receives events
    // -------------------------------------------------------------------------
    @Test
    void testRemoveObserverStopsNotifications() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        List<BattleEvent> received = new ArrayList<>();
        BattleEventListener listener = received::add;
        controller.addBattleEventListener(listener);

        // Remove before any action
        controller.removeBattleEventListener(listener);

        Hero actor = controller.getBattleSystem().getCurrentHero();
        controller.defend(actor);

        assertTrue(received.isEmpty(),
                "Removed observer should not receive any events");
    }

    // -------------------------------------------------------------------------
    // TC-PVP-10: Battle result identifies the correct winning party
    // -------------------------------------------------------------------------
    @Test
    void testBattleResultCorrectWinner() {
        PvPController controller = new PvPController(
                profileOne, partyOne, profileTwo, partyTwo, rankingRepository);

        // Wipe party two — partyOne should win
        for (Hero h : partyTwo.getHeroes()) {
            h.takeDamage(h.getCurrentHp());
        }
        controller.defend(partyOne.getAliveHeroes().get(0));

        BattleResult result = controller.getBattleResult();
        assertNotNull(result, "BattleResult should not be null after battle ends");
        assertEquals(partyOne, result.getWinningParty(),
                "Party one should be the winner");
        assertEquals(partyTwo, result.getLosingParty(),
                "Party two should be the loser");
    }
}
