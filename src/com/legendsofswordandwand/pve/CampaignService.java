package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.HeroFactory;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.pve.RoomGenerator.RoomType;

public class CampaignService {

    public static final int TOTAL_ROOMS = 30;

    // Gold and experience rewards
    private static final double GOLD_LOSS_PERCENT = 0.10;
    private static final double EXP_LOSS_PERCENT  = 0.30;

    // Experience needed to level: Exp(L) = Exp(L-1) + 500 + 75*L + 20*L^2
    // We compute this dynamically in expToNextLevel()

    private final CampaignRepository campaignRepository;
    private final HeroFactory heroFactory;
    private final RoomGenerator roomGenerator;
    private final InnService innService;
    private final ScoreCalculator scoreCalculator;

    public CampaignService(CampaignRepository campaignRepository,
                           InnService innService) {
        this.campaignRepository = campaignRepository;
        this.heroFactory        = new HeroFactory();
        this.roomGenerator      = new RoomGenerator();
        this.innService         = innService;
        this.scoreCalculator    = new ScoreCalculator();
    }

    // Package-private for testing with injected dependencies
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
     * Creates a new campaign for the given profile with a starting hero of the
     * chosen class. Any existing incomplete campaign is overwritten.
     *
     * @return the newly created CampaignProgress, or null if args are invalid
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

        // Stage starts at 1 (rooms are 1-indexed), score starts at 0
        CampaignProgress progress = new CampaignProgress(1, party, 0);

        campaignRepository.saveCampaign(profile, progress);
        return progress;
    }

    // -------------------------------------------------------------------------
    // UC6 — Continue Incomplete PvE Campaign
    // -------------------------------------------------------------------------

    /**
     * Loads a saved campaign for the profile.
     *
     * @return existing CampaignProgress or null if none exists
     */
    public CampaignProgress loadCampaign(Profile profile) {
        if (profile == null) {
            return null;
        }
        return campaignRepository.loadCampaign(profile);
    }

    public boolean hasIncompleteCampaign(Profile profile) {
        if (profile == null) {
            return false;
        }
        return campaignRepository.hasIncompleteCampaign(profile);
    }

    // -------------------------------------------------------------------------
    // UC5 — Exit PvE Campaign
    // -------------------------------------------------------------------------

    /**
     * Saves campaign progress and marks it as exitable.
     *
     * NOTE: Battle-state blocking (cannot exit during battle) requires a
     * status field on CampaignProgress — pending Luxsan's update.
     * Once added, check: if (progress.getStatus() == CampaignStatus.IN_BATTLE) return false;
     *
     * @return true if save succeeded
     */
    public boolean exitCampaign(Profile profile, CampaignProgress progress) {
        if (profile == null || progress == null) {
            return false;
        }

        // TODO: uncomment once CampaignProgress.getStatus() is available
        // if (progress.getStatus() == CampaignStatus.IN_BATTLE) {
        //     return false;
        // }

        campaignRepository.saveCampaign(profile, progress);
        return true;
    }

    // -------------------------------------------------------------------------
    // Room navigation
    // -------------------------------------------------------------------------

    /**
     * Advances the campaign to the next room and determines its type.
     * If the campaign is already complete (30 rooms done), returns null.
     *
     * @return RoomType for the next room, or null if campaign is over
     */
    public RoomType nextRoom(CampaignProgress progress) {
        if (progress == null || progress.isCampaignCompleted()) {
            return null;
        }

        if (progress.getCurrentStage() > TOTAL_ROOMS) {
            progress.completeCampaign();
            return null;
        }

        int cumulativeLevel = getCumulativeLevel(progress.getCurrentParty());
        RoomType roomType = roomGenerator.generateRoom(cumulativeLevel);

        progress.advanceStage();

        if (progress.getCurrentStage() > TOTAL_ROOMS) {
            progress.completeCampaign();
        }

        return roomType;
    }

    // -------------------------------------------------------------------------
    // Battle outcome handling
    // -------------------------------------------------------------------------

    /**
     * Called after a PvE battle victory.
     * Distributes experience among surviving heroes and awards gold.
     * Heroes that can level up will do so automatically.
     *
     * @param progress     the active campaign
     * @param enemyParty   the defeated enemy party
     */
    public void onBattleVictory(CampaignProgress progress, Party enemyParty) {
        if (progress == null || enemyParty == null) {
            return;
        }

        Party party = progress.getCurrentParty();

        // Calculate total exp and gold from enemy party
        int totalExp  = 0;
        int totalGold = 0;
        for (Hero enemy : enemyParty.getHeroes()) {
            totalExp  += 50 * enemy.getLevel();
            totalGold += 75 * enemy.getLevel();
        }

        // Divide exp among surviving heroes only
        java.util.List<Hero> survivors = party.getAliveHeroes();
        if (!survivors.isEmpty()) {
            int expPerHero = totalExp / survivors.size();
            for (Hero hero : survivors) {
                awardExp(hero, expPerHero);
            }
        }

        innService.addGold(totalGold);
    }

    /**
     * Called after a PvE battle defeat.
     * Player loses 10% gold and surviving heroes lose 30% of current-level exp.
     * Returns player to the last inn (handled by the view layer).
     */
    public void onBattleDefeat(CampaignProgress progress) {
        if (progress == null) {
            return;
        }

        // Lose 10% gold
        int goldLoss = (int) (innService.getGold() * GOLD_LOSS_PERCENT);
        innService.deductGold(goldLoss);

        // Surviving heroes lose 30% of their current-level exp
        // Since Hero doesn't currently track exp-in-level, this is a no-op
        // until the Hero class exposes currentExp / expToNextLevel.
        // TODO: hero.deductCurrentLevelExp(EXP_LOSS_PERCENT) once available
    }

    // -------------------------------------------------------------------------
    // Score and completion
    // -------------------------------------------------------------------------

    /**
     * Calculates the final score at the end of a 30-room campaign.
     * Should only be called once isCampaignCompleted() is true.
     */
    public int calculateFinalScore(CampaignProgress progress) {
        return scoreCalculator.calculate(progress);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public int getCumulativeLevel(Party party) {
        if (party == null) return 0;
        int total = 0;
        for (Hero hero : party.getHeroes()) {
            total += hero.getLevel();
        }
        return total;
    }

    /**
     * Experience required to reach the next level from level L.
     * Formula: Exp(L) = Exp(L-1) + 500 + 75*L + 20*L^2
     * Cumulative from level 1.
     */
    public int expToNextLevel(int currentLevel) {
        int total = 0;
        for (int l = 1; l <= currentLevel; l++) {
            total += 500 + 75 * l + 20 * l * l;
        }
        return total;
    }

    private void awardExp(Hero hero, int exp) {
        // Hero does not currently expose an addExperience() method.
        // When Luxsan adds Hero.addExperience(int) and Hero.getExperience(),
        // this method will call levelUp() when the threshold is crossed.
        // TODO: hero.addExperience(exp); while (hero.getExperience() >= expToNextLevel(hero.getLevel())) hero.levelUp();
    }
}
