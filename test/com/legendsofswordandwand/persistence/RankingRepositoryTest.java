package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.model.RankingEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RankingRepositoryTest {

    private RankingRepository rankingRepository;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        databaseManager = DatabaseManager.getInstance();
        databaseManager.getProfiles().clear();
        databaseManager.getCampaignProgressMap().clear();
        databaseManager.getSavedPartiesMap().clear();
        databaseManager.getRankingEntries().clear();

        rankingRepository = new RankingRepository();
    }

    @Test
    public void testRecordScoreAddsRankingEntry() {
        Profile profile = new Profile("luxsan", "1234");

        rankingRepository.recordScore(profile, 150);

        List<RankingEntry> hallOfFame = rankingRepository.getHallOfFame();
        assertEquals(1, hallOfFame.size());
        assertEquals("luxsan", hallOfFame.get(0).getUsername());
        assertEquals(150, hallOfFame.get(0).getScore());
    }

    @Test
    public void testRecordPvPResultUpdatesWinsAndLosses() {
        Profile winner = new Profile("winner", "1111");
        Profile loser = new Profile("loser", "2222");

        rankingRepository.recordPvPResult(winner, loser);

        List<RankingEntry> standings = rankingRepository.getLeagueStandings();

        RankingEntry winnerEntry = standings.stream()
                .filter(entry -> entry.getUsername().equals("winner"))
                .findFirst()
                .orElse(null);

        RankingEntry loserEntry = standings.stream()
                .filter(entry -> entry.getUsername().equals("loser"))
                .findFirst()
                .orElse(null);

        assertNotNull(winnerEntry);
        assertNotNull(loserEntry);
        assertEquals(1, winnerEntry.getWins());
        assertEquals(0, winnerEntry.getLosses());
        assertEquals(0, loserEntry.getWins());
        assertEquals(1, loserEntry.getLosses());
    }

    @Test
    public void testHallOfFameSortedByScoreDescending() {
        Profile first = new Profile("first", "1111");
        Profile second = new Profile("second", "2222");

        rankingRepository.recordScore(first, 100);
        rankingRepository.recordScore(second, 200);

        List<RankingEntry> hallOfFame = rankingRepository.getHallOfFame();

        assertEquals("second", hallOfFame.get(0).getUsername());
        assertEquals("first", hallOfFame.get(1).getUsername());
    }
}