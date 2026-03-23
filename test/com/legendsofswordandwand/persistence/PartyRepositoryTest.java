package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PartyRepositoryTest {

    private PartyRepository partyRepository;
    private Profile profile;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        databaseManager = DatabaseManager.getInstance();
        databaseManager.getProfiles().clear();
        databaseManager.getCampaignProgressMap().clear();
        databaseManager.getSavedPartiesMap().clear();
        databaseManager.getRankingEntries().clear();

        partyRepository = new PartyRepository();
        profile = new Profile("luxsan", "1234");
    }

    @Test
    public void testSavePartySuccessfully() {
        Party party = new Party("TeamOne");
        party.addHero(new Hero("Hero1", HeroClass.WARRIOR));

        boolean saved = partyRepository.saveParty(profile, party);

        assertTrue(saved);
        assertEquals(1, partyRepository.loadSavedParties(profile).size());
    }

    @Test
    public void testSavePartyFailsWhenMoreThanFiveSaved() {
        for (int i = 0; i < 5; i++) {
            Party party = new Party("Team" + i);
            party.addHero(new Hero("Hero" + i, HeroClass.WARRIOR));
            partyRepository.saveParty(profile, party);
        }

        Party extraParty = new Party("Extra");
        extraParty.addHero(new Hero("ExtraHero", HeroClass.MAGE));

        boolean saved = partyRepository.saveParty(profile, extraParty);

        assertFalse(saved);
    }

    @Test
    public void testReplacePartySuccessfully() {
        Party firstParty = new Party("OldParty");
        firstParty.addHero(new Hero("Hero1", HeroClass.WARRIOR));
        partyRepository.saveParty(profile, firstParty);

        Party newParty = new Party("NewParty");
        newParty.addHero(new Hero("Hero2", HeroClass.MAGE));

        boolean replaced = partyRepository.replaceParty(profile, 0, newParty);
        List<Party> parties = partyRepository.loadSavedParties(profile);

        assertTrue(replaced);
        assertEquals("NewParty", parties.get(0).getPartyName());
    }

    @Test
    public void testLoadSavedPartiesReturnsEmptyListWhenNoneExist() {
        List<Party> parties = partyRepository.loadSavedParties(profile);

        assertNotNull(parties);
        assertTrue(parties.isEmpty());
    }
}