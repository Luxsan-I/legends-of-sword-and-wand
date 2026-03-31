package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.CampaignProgress.CampaignStatus;
import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Party;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignProgressTest {

    private CampaignProgress progress;
    private Party party;
    private Hero hero;

    @BeforeEach
    void setUp() {
        party    = new Party("Test Party");
        hero     = new Hero("Hero", HeroClass.WARRIOR);
        party.addHero(hero);
        progress = new CampaignProgress(hero.getName(), 1, party, 0);
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void initialStatus_isBetweenRooms() {
        assertEquals(CampaignStatus.BETWEEN_ROOMS, progress.getStatus());
    }

    @Test
    void initialStage_isOne() {
        assertEquals(1, progress.getCurrentStage());
    }

    @Test
    void initialScore_isZero() {
        assertEquals(0, progress.getCurrentScore());
    }

    @Test
    void initialCampaignCompleted_isFalse() {
        assertFalse(progress.isCampaignCompleted());
    }

    @Test
    void initialExperience_isZeroForHero() {
        assertEquals(0, progress.getExperience(hero));
    }

    // -------------------------------------------------------------------------
    // Status transitions
    // -------------------------------------------------------------------------

    @Test
    void setStatus_updatesCorrectly() {
        progress.setStatus(CampaignStatus.IN_BATTLE);
        assertEquals(CampaignStatus.IN_BATTLE, progress.getStatus());
    }

    @Test
    void setStatus_nullDoesNotChange() {
        progress.setStatus(CampaignStatus.IN_INN);
        progress.setStatus(null);
        assertEquals(CampaignStatus.IN_INN, progress.getStatus());
    }

    // -------------------------------------------------------------------------
    // Stage + completion
    // -------------------------------------------------------------------------

    @Test
    void advanceStage_incrementsByOne() {
        progress.advanceStage();
        assertEquals(2, progress.getCurrentStage());
    }

    @Test
    void completeCampaign_setsFlag() {
        progress.completeCampaign();
        assertTrue(progress.isCampaignCompleted());
    }

    @Test
    void addScore_increasesCurrentScore() {
        progress.addScore(100);
        assertEquals(100, progress.getCurrentScore());
    }

    @Test
    void addScore_negativeValueIgnored() {
        progress.addScore(-50);
        assertEquals(0, progress.getCurrentScore());
    }

    // -------------------------------------------------------------------------
    // Experience — awardExperience, getExperience
    // -------------------------------------------------------------------------

    @Test
    void awardExperience_positiveAmountAdds() {
        progress.awardExperience(hero, 200);
        assertEquals(200, progress.getExperience(hero));
    }

    @Test
    void awardExperience_accumulatesAcrossCalls() {
        progress.awardExperience(hero, 100);
        progress.awardExperience(hero, 150);
        assertEquals(250, progress.getExperience(hero));
    }

    @Test
    void awardExperience_negativeAmountDeducts() {
        progress.awardExperience(hero, 100);
        progress.awardExperience(hero, -30);
        assertEquals(70, progress.getExperience(hero));
    }

    @Test
    void awardExperience_clampedToZero() {
        progress.awardExperience(hero, 50);
        progress.awardExperience(hero, -200); // would go negative
        assertEquals(0, progress.getExperience(hero));
    }

    @Test
    void awardExperience_nullHeroDoesNotThrow() {
        assertDoesNotThrow(() -> progress.awardExperience(null, 100));
    }

    @Test
    void getExperience_nullHeroReturnsZero() {
        assertEquals(0, progress.getExperience(null));
    }

    @Test
    void getExperience_unknownHeroReturnsZero() {
        Hero stranger = new Hero("Stranger", HeroClass.MAGE);
        assertEquals(0, progress.getExperience(stranger));
    }

    // -------------------------------------------------------------------------
    // expToNextLevel — formula verification
    // -------------------------------------------------------------------------

    @Test
    void expToNextLevel_level1() {
        // 500 + 75*1 + 20*1 = 595
        assertEquals(595, progress.expToNextLevel(1));
    }

    @Test
    void expToNextLevel_level2() {
        // cumulative: level1 + (500 + 75*2 + 20*4) = 595 + 730 = 1325
        assertEquals(1325, progress.expToNextLevel(2));
    }

    @Test
    void expToNextLevel_increasesWithLevel() {
        assertTrue(progress.expToNextLevel(5) > progress.expToNextLevel(4));
        assertTrue(progress.expToNextLevel(10) > progress.expToNextLevel(9));
    }

    // -------------------------------------------------------------------------
    // canLevelUp
    // -------------------------------------------------------------------------

    @Test
    void canLevelUp_falseWithNoExp() {
        assertFalse(progress.canLevelUp(hero));
    }

    @Test
    void canLevelUp_falseWithInsufficientExp() {
        progress.awardExperience(hero, progress.expToNextLevel(1) - 1);
        assertFalse(progress.canLevelUp(hero));
    }

    @Test
    void canLevelUp_trueAtExactThreshold() {
        progress.awardExperience(hero, progress.expToNextLevel(1));
        assertTrue(progress.canLevelUp(hero));
    }

    @Test
    void canLevelUp_nullHeroReturnsFalse() {
        assertFalse(progress.canLevelUp(null));
    }

    // -------------------------------------------------------------------------
    // levelUpIfReady
    // -------------------------------------------------------------------------

    @Test
    void levelUpIfReady_levelsHeroUp() {
        progress.awardExperience(hero, progress.expToNextLevel(1));
        int levelBefore = hero.getLevel();

        progress.levelUpIfReady(hero);

        assertEquals(levelBefore + 1, hero.getLevel());
    }

    @Test
    void levelUpIfReady_deductsThresholdExpAfterLevelUp() {
        int threshold = progress.expToNextLevel(1);
        progress.awardExperience(hero, threshold + 50); // 50 extra

        progress.levelUpIfReady(hero);

        assertEquals(50, progress.getExperience(hero));
    }

    @Test
    void levelUpIfReady_doesNothingWithInsufficientExp() {
        int levelBefore = hero.getLevel();
        progress.awardExperience(hero, 10);

        progress.levelUpIfReady(hero);

        assertEquals(levelBefore, hero.getLevel());
    }

    @Test
    void levelUpIfReady_multipleLeveUpsIfExpAllows() {
        // Award enough exp to level up twice
        int thresholdFor2 = progress.expToNextLevel(2); // cumulative for level 1 and 2
        progress.awardExperience(hero, thresholdFor2);

        progress.levelUpIfReady(hero);

        assertEquals(2, hero.getLevel()); // started at 1, levelled up twice
    }

    @Test
    void levelUpIfReady_nullHeroDoesNotThrow() {
        assertDoesNotThrow(() -> progress.levelUpIfReady(null));
    }

    @Test
    void levelUpIfReady_heroStatsIncreaseAfterLevelUp() {
        int attackBefore  = hero.getAttack();
        int defenseBefore = hero.getDefense();

        progress.awardExperience(hero, progress.expToNextLevel(1));
        progress.levelUpIfReady(hero);

        assertTrue(hero.getAttack()  > attackBefore);
        assertTrue(hero.getDefense() > defenseBefore);
    }
}
