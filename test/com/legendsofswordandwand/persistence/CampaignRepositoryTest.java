package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignRepositoryTest {

    private CampaignRepository campaignRepository;
    private Profile profile;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        databaseManager = DatabaseManager.getInstance();
        databaseManager.getProfiles().clear();
        databaseManager.getCampaignProgressMap().clear();
        databaseManager.getSavedPartiesMap().clear();
        databaseManager.getRankingEntries().clear();

        campaignRepository = new CampaignRepository();
        profile = new Profile("luxsan", "1234");
    }

    @Test
    public void testSaveAndLoadCampaign() {
        Party party = new Party("PvE Team");
        party.addHero(new Hero("Hero1", HeroClass.WARRIOR));

        CampaignProgress progress = new CampaignProgress(2, party, 100);
        campaignRepository.saveCampaign(profile, progress);

        CampaignProgress loaded = campaignRepository.loadCampaign(profile);

        assertNotNull(loaded);
        assertEquals(2, loaded.getCurrentStage());
        assertEquals(100, loaded.getCurrentScore());
    }

    @Test
    public void testHasIncompleteCampaignReturnsTrueWhenCampaignExistsAndNotCompleted() {
        Party party = new Party("PvE Team");
        party.addHero(new Hero("Hero1", HeroClass.WARRIOR));

        CampaignProgress progress = new CampaignProgress(1, party, 50);
        campaignRepository.saveCampaign(profile, progress);

        assertTrue(campaignRepository.hasIncompleteCampaign(profile));
    }

    @Test
    public void testHasIncompleteCampaignReturnsFalseWhenCompleted() {
        Party party = new Party("PvE Team");
        party.addHero(new Hero("Hero1", HeroClass.WARRIOR));

        CampaignProgress progress = new CampaignProgress(3, party, 200);
        progress.completeCampaign();
        campaignRepository.saveCampaign(profile, progress);

        assertFalse(campaignRepository.hasIncompleteCampaign(profile));
    }

    @Test
    public void testDeleteCampaignRemovesSavedCampaign() {
        Party party = new Party("PvE Team");
        party.addHero(new Hero("Hero1", HeroClass.WARRIOR));

        CampaignProgress progress = new CampaignProgress(2, party, 100);
        campaignRepository.saveCampaign(profile, progress);

        campaignRepository.deleteCampaign(profile);

        assertNull(campaignRepository.loadCampaign(profile));
    }
}