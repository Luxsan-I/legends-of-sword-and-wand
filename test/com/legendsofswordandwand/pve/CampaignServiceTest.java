package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.CampaignProgress.CampaignStatus;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.persistence.DatabaseManager;
import com.legendsofswordandwand.pve.RoomGenerator.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignServiceTest {

    private CampaignRepository campaignRepository;
    private InnService innService;
    private CampaignService campaignService;
    private Profile profile;

    @BeforeEach
    void setUp() {
        DatabaseManager.resetInstance();
        campaignRepository = new CampaignRepository();
        innService         = new InnService(1000);
        campaignService    = new CampaignService(campaignRepository, innService);
        profile            = new Profile("testUser", "password");
    }

    // -------------------------------------------------------------------------
    // TC-03: New campaign creates party with exactly 1 hero (UC2)
    // -------------------------------------------------------------------------

    @Test
    void startNew_returnsProgressWithOneHero() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);

        assertNotNull(progress);
        assertEquals(1, progress.getCurrentParty().getHeroes().size());
    }

    @Test
    void startNew_heroHasCorrectClass() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.MAGE);

        Hero hero = progress.getCurrentParty().getHeroes().get(0);
        assertEquals(HeroClass.MAGE, hero.getHeroClass());
    }

    @Test
    void startNew_campaignBeginsAtStageOne() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);

        assertEquals(1, progress.getCurrentStage());
    }

    @Test
    void startNew_statusIsBetweenRooms() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);

        assertEquals(CampaignStatus.BETWEEN_ROOMS, progress.getStatus());
    }

    @Test
    void startNew_nullProfileReturnsNull() {
        assertNull(campaignService.startNew(null, HeroClass.WARRIOR));
    }

    @Test
    void startNew_nullClassReturnsNull() {
        assertNull(campaignService.startNew(profile, null));
    }

    @Test
    void startNew_campaignIsSavedToRepository() {
        campaignService.startNew(profile, HeroClass.WARRIOR);

        assertTrue(campaignService.hasIncompleteCampaign(profile));
    }

    // -------------------------------------------------------------------------
    // TC-10: Exit saves campaign; blocks during battle (UC5)
    // -------------------------------------------------------------------------

    @Test
    void exitCampaign_savesAndReturnsTrueWhenBetweenRooms() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        progress.setStatus(CampaignStatus.BETWEEN_ROOMS);

        assertTrue(campaignService.exitCampaign(profile, progress));
        assertNotNull(campaignService.loadCampaign(profile));
    }

    @Test
    void exitCampaign_savesAndReturnsTrueWhenInInn() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        progress.setStatus(CampaignStatus.IN_INN);

        assertTrue(campaignService.exitCampaign(profile, progress));
    }

    @Test
    void exitCampaign_blockedWhenInBattle() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        progress.setStatus(CampaignStatus.IN_BATTLE);

        assertFalse(campaignService.exitCampaign(profile, progress));
    }

    @Test
    void exitCampaign_nullProfileReturnsFalse() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        assertFalse(campaignService.exitCampaign(null, progress));
    }

    @Test
    void exitCampaign_nullProgressReturnsFalse() {
        assertFalse(campaignService.exitCampaign(profile, null));
    }

    // -------------------------------------------------------------------------
    // UC6: Load campaign
    // -------------------------------------------------------------------------

    @Test
    void loadCampaign_returnsNullWhenNoneSaved() {
        assertNull(campaignService.loadCampaign(profile));
    }

    @Test
    void loadCampaign_returnsSavedProgress() {
        CampaignProgress saved = campaignService.startNew(profile, HeroClass.WARRIOR);

        CampaignProgress loaded = campaignService.loadCampaign(profile);
        assertSame(saved, loaded);
    }

    @Test
    void hasIncompleteCampaign_falseBeforeStart() {
        assertFalse(campaignService.hasIncompleteCampaign(profile));
    }

    @Test
    void hasIncompleteCampaign_trueAfterStart() {
        campaignService.startNew(profile, HeroClass.WARRIOR);
        assertTrue(campaignService.hasIncompleteCampaign(profile));
    }

    // -------------------------------------------------------------------------
    // Room navigation — nextRoom advances stage and sets status
    // -------------------------------------------------------------------------

    @Test
    void nextRoom_advancesStage() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        int before = progress.getCurrentStage();

        // Force a deterministic room type using seeded RNG
        InnService seededInn = new InnService(1000);
        RoomGenerator seededGen = new RoomGenerator(new Random(0));
        CampaignService seeded = new CampaignService(campaignRepository, seededGen, seededInn, new ScoreCalculator());
        seeded.nextRoom(progress);

        assertEquals(before + 1, progress.getCurrentStage());
    }

    @Test
    void nextRoom_setsStatusInBattle() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        // Seed so first roll is always BATTLE (nextDouble < 0.60)
        RoomGenerator battleGen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 0.0; }
        });
        CampaignService cs = new CampaignService(campaignRepository, battleGen, innService, new ScoreCalculator());
        RoomType type = cs.nextRoom(progress);

        assertEquals(RoomType.BATTLE, type);
        assertEquals(CampaignStatus.IN_BATTLE, progress.getStatus());
    }

    @Test
    void nextRoom_setsStatusInInn() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        // Seed so first roll is always INN (nextDouble > 0.90)
        RoomGenerator innGen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 1.0; }
        });
        CampaignService cs = new CampaignService(campaignRepository, innGen, innService, new ScoreCalculator());
        RoomType type = cs.nextRoom(progress);

        assertEquals(RoomType.INN, type);
        assertEquals(CampaignStatus.IN_INN, progress.getStatus());
    }

    @Test
    void nextRoom_returnsNullAndCompletesAfter30Rooms() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        // Advance to room 30 manually
        for (int i = 0; i < 30; i++) {
            progress.advanceStage();
        }

        RoomType type = campaignService.nextRoom(progress);

        assertNull(type);
        assertTrue(progress.isCampaignCompleted());
    }

    @Test
    void nextRoom_returnsNullIfAlreadyCompleted() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        progress.completeCampaign();

        assertNull(campaignService.nextRoom(progress));
    }

    // -------------------------------------------------------------------------
    // Battle victory — exp, gold, level-up, status reset
    // -------------------------------------------------------------------------

    @Test
    void onBattleVictory_awardsGoldToInnService() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        int goldBefore = innService.getGold();

        Party enemyParty = new Party("Enemies");
        enemyParty.addHero(new Hero("Enemy", HeroClass.WARRIOR)); // level 1 = 75g

        campaignService.onBattleVictory(progress, enemyParty);

        assertEquals(goldBefore + 75, innService.getGold());
    }

    @Test
    void onBattleVictory_awardsExpToSurvivors() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        Hero hero = progress.getCurrentParty().getHeroes().get(0);

        Party enemyParty = new Party("Enemies");
        enemyParty.addHero(new Hero("Enemy", HeroClass.WARRIOR)); // level 1 = 50 exp

        campaignService.onBattleVictory(progress, enemyParty);

        assertEquals(50, progress.getExperience(hero));
    }

    @Test
    void onBattleVictory_expDividedAmongSurvivors() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        Hero second = new Hero("Second", HeroClass.MAGE);
        progress.getCurrentParty().addHero(second);

        Party enemyParty = new Party("Enemies");
        enemyParty.addHero(new Hero("Enemy", HeroClass.WARRIOR)); // 50 exp / 2 heroes = 25 each

        campaignService.onBattleVictory(progress, enemyParty);

        Hero first = progress.getCurrentParty().getHeroes().get(0);
        assertEquals(25, progress.getExperience(first));
        assertEquals(25, progress.getExperience(second));
    }

    @Test
    void onBattleVictory_deadHeroesReceiveNoExp() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        Hero dead = new Hero("Dead", HeroClass.MAGE);
        dead.takeDamage(dead.getMaxHp()); // kill the hero
        progress.getCurrentParty().addHero(dead);

        Party enemyParty = new Party("Enemies");
        enemyParty.addHero(new Hero("Enemy", HeroClass.WARRIOR));

        campaignService.onBattleVictory(progress, enemyParty);

        assertEquals(0, progress.getExperience(dead));
    }

    @Test
    void onBattleVictory_resetsStatusToBetweenRooms() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        progress.setStatus(CampaignStatus.IN_BATTLE);

        campaignService.onBattleVictory(progress, new Party("Enemies"));

        assertEquals(CampaignStatus.BETWEEN_ROOMS, progress.getStatus());
    }

    // -------------------------------------------------------------------------
    // Battle defeat — 10% gold loss, 30% exp loss, status reset
    // -------------------------------------------------------------------------

    @Test
    void onBattleDefeat_deducts10PercentGold() {
        InnService inn = new InnService(1000);
        CampaignService cs = new CampaignService(campaignRepository, inn);
        CampaignProgress progress = cs.startNew(profile, HeroClass.WARRIOR);

        cs.onBattleDefeat(progress);

        assertEquals(900, inn.getGold()); // 10% of 1000 = 100
    }

    @Test
    void onBattleDefeat_deducts30PercentExpFromSurvivors() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        Hero hero = progress.getCurrentParty().getHeroes().get(0);
        progress.awardExperience(hero, 100);

        campaignService.onBattleDefeat(progress);

        assertEquals(70, progress.getExperience(hero)); // 30% of 100 = 30 deducted
    }

    @Test
    void onBattleDefeat_expCannotGoBelowZero() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        Hero hero = progress.getCurrentParty().getHeroes().get(0);
        // No exp awarded — deducting 30% of 0 should stay 0

        campaignService.onBattleDefeat(progress);

        assertEquals(0, progress.getExperience(hero));
    }

    @Test
    void onBattleDefeat_resetsStatusToBetweenRooms() {
        CampaignProgress progress = campaignService.startNew(profile, HeroClass.WARRIOR);
        progress.setStatus(CampaignStatus.IN_BATTLE);

        campaignService.onBattleDefeat(progress);

        assertEquals(CampaignStatus.BETWEEN_ROOMS, progress.getStatus());
    }

    // -------------------------------------------------------------------------
    // getCumulativeLevel helper
    // -------------------------------------------------------------------------

    @Test
    void getCumulativeLevel_singleHeroLevelOne() {
        Party party = new Party("Test");
        party.addHero(new Hero("H", HeroClass.WARRIOR));

        assertEquals(1, campaignService.getCumulativeLevel(party));
    }

    @Test
    void getCumulativeLevel_sumsAllHeroLevels() {
        Party party = new Party("Test");
        Hero h1 = new Hero("H1", HeroClass.WARRIOR);
        Hero h2 = new Hero("H2", HeroClass.MAGE);
        h1.levelUp(); // level 2
        h2.levelUp(); h2.levelUp(); // level 3
        party.addHero(h1);
        party.addHero(h2);

        assertEquals(5, campaignService.getCumulativeLevel(party));
    }

    @Test
    void getCumulativeLevel_nullPartyReturnsZero() {
        assertEquals(0, campaignService.getCumulativeLevel(null));
    }
}
