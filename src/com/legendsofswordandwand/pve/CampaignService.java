package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.CampaignProgress.CampaignStatus;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.HeroFactory;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.pve.RoomGenerator.RoomType;

import java.util.List;

public class CampaignService {

    public static final int TOTAL_ROOMS = 30;

    private static final double GOLD_LOSS_PERCENT = 0.10;
    private static final double EXP_LOSS_PERCENT  = 0.30;

    private final CampaignRepository campaignRepository;
    private final HeroFactory        heroFactory;
    private final RoomGenerator      roomGenerator;
    private final InnService         innService;
    private final ScoreCalculator    scoreCalculator;

    public CampaignService(CampaignRepository campaignRepository, InnService innService) {
        this.campaignRepository = campaignRepository;
        this.heroFactory        = new HeroFactory();
        this.roomGenerator      = new RoomGenerator();
        this.innService         = innService;
        this.scoreCalculator    = new ScoreCalculator();
    }

    // Package-private: allows test injection of mock dependencies
    CampaignService(CampaignRepository campaignRepository,
                    RoomGenerator roomGenerator,
                    InnService innService,
                    ScoreCalculator scoreCalculator) {
        this.campaignRepository = campaignRepository;
        this.heroFactory        = new HeroFactory();
        this.roomGenerator      = roomGenerator;
        this.innService         = innService;
        this.scoreCalculator    = scoreCalculator;
    }

    // -------------------------------------------------------------------------
    // UC2 — Start New PvE Campaign
    // -------------------------------------------------------------------------

    /**
     * Creates a fresh campaign for the profile with a single starting hero of
     * the chosen class. Overwrites any previously saved campaign for that profile.
     *
     * @return the new CampaignProgress, or null if arguments are invalid
     */
    public CampaignProgress startNew(Profile profile, HeroClass heroClass) {
        if (profile == null || heroClass == null) {
            return null;
        }

        Hero startingHero = heroFactory.createHero(profile.getUsername() + "'s Hero", heroClass);
        if (startingHero == null) {
            return null;
        }

        Party party = new Party(profile.getUsername() + "'s Party");
        party.addHero(startingHero);

        CampaignProgress progress = new CampaignProgress(profile.getUsername(), 1, party, 0);
        campaignRepository.saveCampaign(profile, progress);
        return progress;
    }

    // -------------------------------------------------------------------------
    // UC5 — Exit PvE Campaign
    // -------------------------------------------------------------------------

    /**
     * Saves the campaign and exits. Blocked if the player is currently IN_BATTLE.
     *
     * @return true if the campaign was saved and exit is allowed, false otherwise
     */
    public boolean exitCampaign(Profile profile, CampaignProgress progress) {
        if (profile == null || progress == null) {
            return false;
        }
        if (progress.getStatus() == CampaignStatus.IN_BATTLE) {
            return false;
        }
        campaignRepository.saveCampaign(profile, progress);
        return true;
    }

    // -------------------------------------------------------------------------
    // UC6 — Continue Incomplete PvE Campaign
    // -------------------------------------------------------------------------

    /**
     * Loads a previously saved campaign for the profile.
     *
     * @return the saved CampaignProgress, or null if none exists
     */
    public CampaignProgress loadCampaign(Profile profile) {
        if (profile == null) {
            return null;
        }
        return campaignRepository.loadCampaign(profile);
    }

    /**
     * Returns true if the profile has a saved campaign that is not yet completed.
     */
    public boolean hasIncompleteCampaign(Profile profile) {
        if (profile == null) {
            return false;
        }
        return campaignRepository.hasIncompleteCampaign(profile);
    }

    // -------------------------------------------------------------------------
    // Room navigation
    // -------------------------------------------------------------------------

    /**
     * Determines the next room type and advances the stage counter.
     * Sets campaign status to IN_BATTLE or IN_INN accordingly.
     * Marks the campaign completed and returns null after room 30.
     *
     * @return RoomType.BATTLE, RoomType.INN, or null if campaign is over
     */
    public RoomType nextRoom(CampaignProgress progress) {
        if (progress == null || progress.isCampaignCompleted()) {
            return null;
        }

        progress.advanceStage();

        if (progress.getCurrentStage() > TOTAL_ROOMS) {
            progress.completeCampaign();
            return null;
        }

        int cumulativeLevel = getCumulativeLevel(progress.getCurrentParty());
        RoomType roomType   = roomGenerator.generateRoom(cumulativeLevel);

        if (roomType == RoomType.BATTLE) {
            progress.setStatus(CampaignStatus.IN_BATTLE);
        } else {
            progress.setStatus(CampaignStatus.IN_INN);
        }

        return roomType;
    }

    // -------------------------------------------------------------------------
    // Battle outcome handling
    // -------------------------------------------------------------------------

    /**
     * Called by BattleView when the player wins a PvE battle.
     *
     * Distributes experience equally among surviving heroes using:
     *   Exp per enemy = 50 * enemyLevel
     *   Gold per enemy = 75 * enemyLevel
     *
     * After awarding exp, each surviving hero levels up as many times as
     * their accumulated XP allows. Status is reset to BETWEEN_ROOMS.
     *
     * @param progress   the active campaign
     * @param enemyParty the defeated enemy party
     */
    public void onBattleVictory(CampaignProgress progress, Party enemyParty) {
        if (progress == null || enemyParty == null) {
            return;
        }

        int totalExp  = 0;
        int totalGold = 0;
        for (Hero enemy : enemyParty.getHeroes()) {
            totalExp  += 50 * enemy.getLevel();
            totalGold += 75 * enemy.getLevel();
        }

        List<Hero> survivors = progress.getCurrentParty().getAliveHeroes();
        if (!survivors.isEmpty()) {
            int expPerHero = totalExp / survivors.size();
            for (Hero hero : survivors) {
                progress.awardExperience(hero, expPerHero);
                progress.levelUpIfReady(hero);
            }
        }

        innService.addGold(totalGold);
        progress.setStatus(CampaignStatus.BETWEEN_ROOMS);
    }

    /**
     * Called by BattleView when the player loses a PvE battle.
     *
     * Penalties applied:
     *   - 10% of current gold is lost
     *   - Surviving heroes lose 30% of their accumulated current-level XP
     *     (heroes cannot lose levels, XP is clamped at 0)
     *
     * Status is reset to BETWEEN_ROOMS so the view can route back to the inn.
     *
     * @param progress the active campaign
     */
    public void onBattleDefeat(CampaignProgress progress) {
        if (progress == null) {
            return;
        }

        // Deduct 10% gold
        int goldLoss = (int) (innService.getGold() * GOLD_LOSS_PERCENT);
        innService.deductGold(goldLoss);

        // Deduct 30% of current-level XP from each surviving hero
        for (Hero hero : progress.getCurrentParty().getAliveHeroes()) {
            int currentExp = progress.getExperience(hero);
            int penalty    = (int) (currentExp * EXP_LOSS_PERCENT);
            progress.awardExperience(hero, -penalty);
        }

        progress.setStatus(CampaignStatus.BETWEEN_ROOMS);
    }

    // -------------------------------------------------------------------------
    // Score and completion
    // -------------------------------------------------------------------------

    /**
     * Calculates the final campaign score once all 30 rooms are completed.
     * Should only be called after isCampaignCompleted() returns true.
     */
    public int calculateFinalScore(CampaignProgress progress) {
        return scoreCalculator.calculate(progress);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Sums the levels of all heroes in the party.
     * Used by RoomGenerator to determine encounter probability.
     */
    public int getCumulativeLevel(Party party) {
        if (party == null) return 0;
        int total = 0;
        for (Hero hero : party.getHeroes()) {
            total += hero.getLevel();
        }
        return total;
    }
}