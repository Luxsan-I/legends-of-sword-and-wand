package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileRepositoryTest {

    private ProfileRepository profileRepository;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        databaseManager = DatabaseManager.getInstance();
        databaseManager.getProfiles().clear();
        databaseManager.getCampaignProgressMap().clear();
        databaseManager.getSavedPartiesMap().clear();
        databaseManager.getRankingEntries().clear();
        profileRepository = new ProfileRepository();
    }

    @Test
    public void testCreateProfileSuccessfully() {
        boolean created = profileRepository.createProfile("luxsan", "1234");

        assertTrue(created);
        assertNotNull(profileRepository.findByUsername("luxsan"));
    }

    @Test
    public void testCreateProfileFailsForDuplicateUsername() {
        profileRepository.createProfile("luxsan", "1234");

        boolean createdAgain = profileRepository.createProfile("luxsan", "abcd");

        assertFalse(createdAgain);
    }

    @Test
    public void testAuthenticateValidCredentials() {
        profileRepository.createProfile("luxsan", "1234");

        boolean authenticated = profileRepository.authenticate("luxsan", "1234");

        assertTrue(authenticated);
    }

    @Test
    public void testAuthenticateInvalidCredentials() {
        profileRepository.createProfile("luxsan", "1234");

        boolean authenticated = profileRepository.authenticate("luxsan", "wrong");

        assertFalse(authenticated);
    }

    @Test
    public void testSaveProfileUpdatesStoredProfile() {
        profileRepository.createProfile("luxsan", "1234");
        Profile profile = profileRepository.findByUsername("luxsan");
        profile.addScore(50);

        profileRepository.saveProfile(profile);

        Profile updated = profileRepository.findByUsername("luxsan");
        assertEquals(50, updated.getTotalScore());
    }
}